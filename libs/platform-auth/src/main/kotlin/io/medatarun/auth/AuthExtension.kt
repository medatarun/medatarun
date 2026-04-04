package io.medatarun.auth

import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.auth.actions.AuthEmbeddedActionsProvider
import io.medatarun.auth.adapters.ActorRoleAdapters.toAppPermission
import io.medatarun.auth.adapters.AppActorResolverAuth
import io.medatarun.auth.domain.ActorRole
import io.medatarun.auth.domain.ConfigProperties
import io.medatarun.auth.domain.actor.ActorId
import io.medatarun.auth.domain.jwt.JwtConfig
import io.medatarun.auth.domain.jwt.JwtKeyMaterial
import io.medatarun.auth.domain.user.Fullname
import io.medatarun.auth.domain.user.PasswordClear
import io.medatarun.auth.domain.user.Username
import io.medatarun.auth.infra.db.*
import io.medatarun.auth.internal.actors.ActorClaimsAdapter
import io.medatarun.auth.internal.actors.ActorServiceImpl
import io.medatarun.auth.internal.bootstrap.BootstrapSecretLifecycleImpl
import io.medatarun.auth.internal.jwk.JwkExternalProvidersImpl
import io.medatarun.auth.internal.jwk.JwkExternalProvidersImpl.Companion.createJwtExternalProvidersFromConfigProperties
import io.medatarun.auth.internal.jwk.JwtInternalInternalSigninKeyRegistryImpl
import io.medatarun.auth.internal.oauth.OAuthServiceImpl
import io.medatarun.auth.internal.oidc.AuthClientRegistry
import io.medatarun.auth.internal.oidc.AuthClientStorage
import io.medatarun.auth.internal.oidc.AuthClientStorageInternal
import io.medatarun.auth.internal.oidc.OidcServiceImpl
import io.medatarun.auth.internal.users.UserPasswordEncrypter
import io.medatarun.auth.internal.users.UserServiceEventsActorProvisioning
import io.medatarun.auth.internal.users.UserServiceImpl
import io.medatarun.auth.ports.exposed.*
import io.medatarun.auth.ports.exposed.BootstrapSecretLifecycle.Companion.DEFAULT_BOOTSTRAP_SECRET_PATH_NAME
import io.medatarun.auth.ports.exposed.JwtInternalSigninKeyRegistry.Companion.DEFAULT_KEYSTORE_PATH_NAME
import io.medatarun.auth.ports.needs.*
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.platform.db.DbMigration
import io.medatarun.platform.kernel.*
import io.medatarun.security.AppActorResolver
import io.medatarun.security.AppPermission
import io.medatarun.security.SecurityPermissionsProvider
import io.medatarun.security.SecurityRolesRegistry
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonEquiv
import java.time.Instant
import kotlin.reflect.KClass

class AuthExtension(
    val config: AuthExtensionConfig = AuthExtensionConfigProd()
) : MedatarunExtension {
    override val id: ExtensionId = "platform-auth"
    override fun initContributions(ctx: MedatarunExtensionCtx) {
        val userService = ctx.getService<UserService>()
        val oidcService = ctx.getService<OidcService>()
        val oauthService = ctx.getService<OAuthService>()
        val actorService = ctx.getService<ActorService>()
        val securityRolesRegistry = ctx.getService<SecurityRolesRegistry>()
        val actorStorage = ctx.getService<ActorStorageSQLite>()

        val actionProvider = AuthEmbeddedActionsProvider(
            userService, oidcService, oauthService, actorService, config.authClock
        )
        val rolesProvider = object : SecurityPermissionsProvider {
            override fun getPermissions(): List<AppPermission> {
                return listOf(toAppPermission(ActorRole.ADMIN))
            }
        }
        ctx.registerContribution(ActionProvider::class, actionProvider)
        ctx.registerContribution(SecurityPermissionsProvider::class, rolesProvider)
        ctx.registerContribution(TypeDescriptor::class, UsernameTypeDescriptor())
        ctx.registerContribution(TypeDescriptor::class, FullnameTypeDescriptor())
        ctx.registerContribution(TypeDescriptor::class, PasswordClearTypeDescriptor())
        ctx.registerContribution(TypeDescriptor::class, ActorIdDescriptor())
        ctx.registerContribution(DbMigration::class, AuthDbMigration(securityRolesRegistry, actorStorage, config.authClock))
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
        val dbConnectionFactory = ctx.getService(DbConnectionFactory::class)


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

        val jwtIssuer = ctx.getConfigProperty(
            ConfigProperties.JwtDefaultIssuer.key,
            "urn:medatarun:${authEmbeddedKeys.kid}"
        )
        val jwtAudience = ctx.getConfigProperty(
            ConfigProperties.JwtDefaultAudience.key,
            ConfigProperties.JwtDefaultAudience.defaultValue
        )
        val jwtTtlSeconds = ctx.getConfigProperty(
            ConfigProperties.JwtDefaultTtlSeconds.key,
            ConfigProperties.JwtDefaultTtlSeconds.defaultValue
        ).toLong()
        val clientRegistrationRetentionDays = ctx.getConfigProperty(
            ConfigProperties.ClientRegistrationRetentionDays.key,
            ConfigProperties.ClientRegistrationRetentionDays.defaultValue
        ).toLong()

        val jwtCfg = JwtConfig(
            issuer = jwtIssuer,
            audience = jwtAudience,
            ttlSeconds = jwtTtlSeconds
        )

        val bootstrapper = BootstrapSecretLifecycleImpl(cfgBootstrapSecretPath, cfgBootstrapSecret)

        val actorService: ActorService = ActorServiceImpl(actorStorage, config.authClock, actorRolesRegistry)
        val userEvents: UserServiceEvents = UserServiceEventsActorProvisioning(actorService, jwtCfg.issuer)

        val userService: UserService = UserServiceImpl(
            userStorage = userStorage,
            clock = config.authClock,
            passwordEncryptionIterations = config.passwordEncryptionDefaultIterations,
            bootstrapper = bootstrapper,
            userEvents = userEvents
        )

        val oauthService = OAuthServiceImpl(
            userService = userService,
            keys = authEmbeddedKeys,
            jwtConfig = jwtCfg,
            actorClaimsAdapter = actorClaimsAdapter,
            actorService = actorService,
            clock = config.authClock
        )

        val internalClientStorage: AuthClientStorage = AuthClientStorageInternal(
            ctx.publicBaseURL(),
            config.authClock
        )
        val inMemoryClientStorage: AuthClientStorage = AuthClientStorageDb(dbConnectionFactory)

        val clientStorages: List<AuthClientStorage> = listOf(internalClientStorage, inMemoryClientStorage)

        val authClientRegistry = AuthClientRegistry(
            clientStorages, config.authClock, clientRegistrationRetentionDays
        )

        val oidcService: OidcService = OidcServiceImpl(
            oidcAuthCodeStorage = authStorage,
            actorClaimsAdapter = actorClaimsAdapter,
            oauthService = oauthService,
            authEmbeddedKeys = authEmbeddedKeys,
            jwtCfg = jwtCfg,
            clock = config.authClock,
            actorService = actorService,
            authCtxDurationSeconds = DEFAULT_AUTH_CTX_DURATION_SECONDS,
            authClientRegistry = authClientRegistry,
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
            ),
            publicBaseUrl = ctx.publicBaseURL()

        )

        val appActorResolver = AppActorResolverAuth(actorService)

        ctx.register(UserService::class, userService)
        ctx.register(OidcService::class, oidcService)
        ctx.register(OAuthService::class, oauthService)
        ctx.register(ActorService::class, actorService)
        ctx.register(AppActorResolver::class, appActorResolver)

        // Because migrations need it
        ctx.register(ActorStorageSQLite::class, actorStorage)

        // For testing only
        ctx.register(BootstrapSecretLifecycle::class, bootstrapper)
        ctx.register(JwtConfig::class, jwtCfg)
        ctx.register(JwtKeyMaterial::class, authEmbeddedKeys)

    }

    companion object {
        const val DEFAULT_AUTH_CTX_DURATION_SECONDS: Long = 60 * 15
    }


}

interface AuthExtensionConfig {
    val authClock: AuthClock
    val passwordEncryptionDefaultIterations: Int
}

class AuthExtensionConfigProd : AuthExtensionConfig {
    override val authClock = object : AuthClock {
        override fun now(): Instant = Instant.now()
    }
    override val passwordEncryptionDefaultIterations = UserPasswordEncrypter.DEFAULT_ITERATIONS
}
