package io.medatarun.auth

import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.auth.actions.AuthEmbeddedActionsProvider
import io.medatarun.auth.adapters.ActorRoleAdapters.toAppPrincipalRole
import io.medatarun.auth.domain.ActorRole
import io.medatarun.auth.domain.ConfigProperties
import io.medatarun.auth.domain.actor.ActorId
import io.medatarun.auth.domain.jwt.JwtConfig
import io.medatarun.auth.domain.user.Fullname
import io.medatarun.auth.domain.user.PasswordClear
import io.medatarun.auth.domain.user.Username
import io.medatarun.auth.infra.ActorStorageSQLite
import io.medatarun.auth.infra.DbConnectionFactoryImpl
import io.medatarun.auth.infra.OidcStorageSQLite
import io.medatarun.auth.infra.UserStorageSQLite
import io.medatarun.auth.internal.actors.ActorClaimsAdapter
import io.medatarun.auth.internal.actors.ActorServiceImpl
import io.medatarun.auth.internal.bootstrap.BootstrapSecretLifecycleImpl
import io.medatarun.auth.internal.jwk.JwkExternalProvidersImpl
import io.medatarun.auth.internal.jwk.JwkExternalProvidersImpl.Companion.createJwtExternalProvidersFromConfigProperties
import io.medatarun.auth.internal.jwk.JwtInternalInternalSigninKeyRegistryImpl
import io.medatarun.auth.internal.oauth.OAuthServiceImpl
import io.medatarun.auth.internal.oidc.OidcServiceImpl
import io.medatarun.auth.internal.users.UserPasswordEncrypter
import io.medatarun.auth.internal.users.UserServiceEventsActorProvisioning
import io.medatarun.auth.internal.users.UserServiceImpl
import io.medatarun.auth.ports.exposed.ActorService
import io.medatarun.auth.ports.exposed.BootstrapSecretLifecycle.Companion.DEFAULT_BOOTSTRAP_SECRET_PATH_NAME
import io.medatarun.auth.ports.exposed.JwtInternalSigninKeyRegistry.Companion.DEFAULT_KEYSTORE_PATH_NAME
import io.medatarun.auth.ports.exposed.OAuthService
import io.medatarun.auth.ports.exposed.OidcService
import io.medatarun.auth.ports.exposed.UserService
import io.medatarun.auth.ports.needs.*
import io.medatarun.platform.kernel.ExtensionId
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx
import io.medatarun.platform.kernel.MedatarunServiceCtx
import io.medatarun.security.AppPrincipalRole
import io.medatarun.security.SecurityRolesProvider
import io.medatarun.security.SecurityRolesRegistry
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonEquiv
import java.time.Instant
import kotlin.reflect.KClass

class AuthExtension : MedatarunExtension {
    override val id: ExtensionId = "auth"
    override fun init(ctx: MedatarunExtensionCtx) {
        val actionProvider = AuthEmbeddedActionsProvider()
        val rolesProvider = object : SecurityRolesProvider {
            override fun getRoles(): List<AppPrincipalRole> {
                return listOf(toAppPrincipalRole(ActorRole.ADMIN))
            }
        }
        ctx.register(ActionProvider::class, actionProvider)
        ctx.register(SecurityRolesProvider::class, rolesProvider)
        ctx.register(TypeDescriptor::class, UsernameTypeDescriptor())
        ctx.register(TypeDescriptor::class, FullnameTypeDescriptor())
        ctx.register(TypeDescriptor::class, PasswordClearTypeDescriptor())
        ctx.register(TypeDescriptor::class, ActorIdDescriptor())
    }

    class UsernameTypeDescriptor : TypeDescriptor<Username> {
        override val target: KClass<Username> = Username::class
        override val equivMultiplatorm: String = "Username"
        override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
        override fun validate(value: Username): Username {
            return value.validate()
        }
    }

    class FullnameTypeDescriptor : TypeDescriptor<Fullname> {
        override val target: KClass<Fullname> = Fullname::class
        override val equivMultiplatorm: String = "Fullname"
        override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
        override fun validate(value: Fullname): Fullname {
            return value.validate()
        }
    }

    class PasswordClearTypeDescriptor : TypeDescriptor<PasswordClear> {
        override val target: KClass<PasswordClear> = PasswordClear::class
        override val equivMultiplatorm: String = "PasswordClear"
        override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
        override fun validate(value: PasswordClear): PasswordClear {
            // No validation in entrance because the rules are too specific
            // Business will do it
            return value
        }

    }

    class ActorIdDescriptor : TypeDescriptor<ActorId> {
        override val target: KClass<ActorId> = ActorId::class
        override val equivMultiplatorm: String = "ActorId"
        override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
        override fun validate(value: ActorId): ActorId {
            // No validation in entrance because the rules are too specific
            // Business will do it
            return value
        }
    }

    override fun initServices(ctx: MedatarunServiceCtx) {
        val cfgBootstrapSecretPath = ctx.resolveApplicationHomePath(DEFAULT_BOOTSTRAP_SECRET_PATH_NAME)
        val cfgKeyStorePath = ctx.resolveApplicationHomePath(DEFAULT_KEYSTORE_PATH_NAME)
        val dbConnectionFactory = DbConnectionFactoryImpl(
            ctx.resolveApplicationHomePath("data/database.db").toAbsolutePath().toString()
        )
        val authClock = object : AuthClock {
            override fun now(): Instant = Instant.now()
        }
        val passwordEncryptionDefaultIterations = UserPasswordEncrypter.DEFAULT_ITERATIONS
        val cfgBootstrapSecret = ctx.getConfigProperty(ConfigProperties.BootstrapSecret.key)
        val actorRolesRegistry = object : ActorRolesRegistry {
            override fun isKnownRole(key: String): Boolean {
                val ext = ctx.getService(SecurityRolesRegistry::class)
                return ext.findAllRoles().any { it.key == key }
            }
        }

        // ------------------------------------------
        // Should be the same in test initializations
        // ------------------------------------------

        val userStorage = UserStorageSQLite(dbConnectionFactory)
        val authStorage: OidcStorage = OidcStorageSQLite(dbConnectionFactory)
        val actorStorage = ActorStorageSQLite(dbConnectionFactory)

        val actorClaimsAdapter = ActorClaimsAdapter()
        val authEmbeddedKeyRegistry = JwtInternalInternalSigninKeyRegistryImpl(cfgKeyStorePath)
        val authEmbeddedKeys = authEmbeddedKeyRegistry.loadOrCreateKeys()

        val jwtCfg = JwtConfig(
            issuer = "urn:medatarun:${authEmbeddedKeys.kid}",  // stable tant que tes fichiers sont là
            audience = "medatarun",
            ttlSeconds = 3600
        )

        val bootstrapper = BootstrapSecretLifecycleImpl(cfgBootstrapSecretPath, cfgBootstrapSecret)

        val actorService: ActorService = ActorServiceImpl(actorStorage, authClock, actorRolesRegistry)
        val userEvents: UserServiceEvents = UserServiceEventsActorProvisioning(actorService, jwtCfg.issuer)

        val userService: UserService = UserServiceImpl(
            userStorage = userStorage,
            clock = authClock,
            passwordEncryptionIterations = passwordEncryptionDefaultIterations,
            bootstrapper = bootstrapper,
            userEvents = userEvents
        )

        val oauthService = OAuthServiceImpl(
            userService = userService,
            keys = authEmbeddedKeys,
            jwtConfig = jwtCfg,
            actorClaimsAdapter = actorClaimsAdapter,
            actorService = actorService
        )

        val oidcService: OidcService = OidcServiceImpl(
            oidcAuthCodeStorage = authStorage,
            actorClaimsAdapter = actorClaimsAdapter,
            oauthService = oauthService,
            authEmbeddedKeys = authEmbeddedKeys,
            jwtCfg = jwtCfg,
            clock = authClock,
            actorService = actorService,
            authCtxDurationSeconds = DEFAULT_AUTH_CTX_DURATION_SECONDS,
            externalProviders = createJwtExternalProvidersFromConfigProperties(
                object : JwkExternalProvidersImpl.Companion.ConfigResolver {
                    override fun getConfigProperty(key: String, defaultValue: String): String {
                        return ctx.getConfigProperty(key, defaultValue = defaultValue)
                    }

                    override fun getConfigProperty(key: String): String? {
                        return ctx.getConfigProperty(key)
                    }

                }, jwtCfg.issuer
            ),
            oidcProviderConfig = OidcProviderConfig.valueOf(
                ctx.getConfigProperty(ConfigProperties.UIOidcAuthority.key),
                ctx.getConfigProperty(ConfigProperties.UiOidcClientId.key)
            )

        )

        ctx.register(UserService::class, userService)
        ctx.register(OidcService::class, oidcService)
        ctx.register(OAuthService::class, oauthService)
        ctx.register(ActorService::class, actorService)


    }

    companion object {
        const val DEFAULT_AUTH_CTX_DURATION_SECONDS: Long = 60 * 15
    }


}
