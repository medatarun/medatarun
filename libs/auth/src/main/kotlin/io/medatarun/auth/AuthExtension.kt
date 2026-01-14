package io.medatarun.auth

import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.auth.actions.AuthEmbeddedActionsProvider
import io.medatarun.auth.adapters.ActorRoleAdapters.toAppPrincipalRole
import io.medatarun.auth.domain.*
import io.medatarun.auth.domain.actor.ActorId
import io.medatarun.auth.domain.jwt.JwtConfig
import io.medatarun.auth.domain.oidc.ExternalOidcProviderConfig
import io.medatarun.auth.domain.oidc.ExternalOidcProvidersConfig
import io.medatarun.auth.domain.user.Fullname
import io.medatarun.auth.domain.user.PasswordClear
import io.medatarun.auth.domain.user.Username
import io.medatarun.auth.infra.ActorStorageSQLite
import io.medatarun.auth.infra.DbConnectionFactoryImpl
import io.medatarun.auth.infra.OidcStorageSQLite
import io.medatarun.auth.infra.UserStorageSQLite
import io.medatarun.auth.internal.*
import io.medatarun.auth.ports.exposed.ActorService
import io.medatarun.auth.ports.exposed.BootstrapSecretLifecycle.Companion.DEFAULT_BOOTSTRAP_SECRET_PATH_NAME
import io.medatarun.auth.ports.exposed.JwtSigninKeyRegistry.Companion.DEFAULT_KEYSTORE_PATH_NAME
import io.medatarun.auth.ports.exposed.OAuthService
import io.medatarun.auth.ports.exposed.OidcService
import io.medatarun.auth.ports.exposed.UserService
import io.medatarun.auth.ports.needs.ActorRolesRegistry
import io.medatarun.auth.ports.needs.AuthClock
import io.medatarun.auth.ports.needs.OidcStorage
import io.medatarun.auth.ports.needs.UserServiceEvents
import io.medatarun.kernel.ExtensionId
import io.medatarun.kernel.MedatarunExtension
import io.medatarun.kernel.MedatarunExtensionCtx
import io.medatarun.kernel.MedatarunServiceCtx
import io.medatarun.security.AppPrincipalRole
import io.medatarun.security.SecurityRolesProvider
import io.medatarun.security.SecurityRolesRegistry
import io.medatarun.types.JsonTypeEquiv
import io.medatarun.types.TypeDescriptor
import java.time.Instant
import kotlin.reflect.KClass

class AuthExtension() : MedatarunExtension {
    override val id: ExtensionId = "auth"
    override fun init(ctx: MedatarunExtensionCtx) {
        val actionProvider = AuthEmbeddedActionsProvider()
        val rolesProvider = object: SecurityRolesProvider {
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

    class UsernameTypeDescriptor: TypeDescriptor<Username> {
        override val target: KClass<Username> = Username::class
        override val equivMultiplatorm: String = "Username"
        override val equivJson: JsonTypeEquiv = JsonTypeEquiv.STRING
        override fun validate(value: Username): Username {
            return value.validate()
        }
    }
    class FullnameTypeDescriptor: TypeDescriptor<Fullname> {
        override val target: KClass<Fullname> = Fullname::class
        override val equivMultiplatorm: String = "Fullname"
        override val equivJson: JsonTypeEquiv = JsonTypeEquiv.STRING
        override fun validate(value: Fullname): Fullname {
            return value.validate()
        }
    }

    class PasswordClearTypeDescriptor : TypeDescriptor<PasswordClear> {
        override val target: KClass<PasswordClear> = PasswordClear::class
        override val equivMultiplatorm: String = "PasswordClear"
        override val equivJson: JsonTypeEquiv = JsonTypeEquiv.STRING
        override fun validate(value: PasswordClear): PasswordClear {
            // No validation in entrance because the rules are too specific
            // Business will do it
            return value
        }

    }

    class ActorIdDescriptor: TypeDescriptor<ActorId> {
        override val target: KClass<ActorId> = ActorId::class
        override val equivMultiplatorm: String = "ActorId"
        override val equivJson: JsonTypeEquiv = JsonTypeEquiv.STRING
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
        val actorRolesRegistry = object: ActorRolesRegistry {
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
        val authEmbeddedKeyRegistry = JwtSigninKeyRegistryImpl(cfgKeyStorePath)
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
            externalOidcProviders = loadExternalOidcProviders(ctx, jwtCfg.issuer)
        )

        ctx.register(UserService::class, userService)
        ctx.register(OidcService::class, oidcService)
        ctx.register(OAuthService::class, oauthService)
        ctx.register(ActorService::class, actorService)


    }

    companion object {
        const val DEFAULT_AUTH_CTX_DURATION_SECONDS: Long = 60 * 15
    }

    private fun loadExternalOidcProviders(
        ctx: MedatarunServiceCtx,
        internalIssuer: String
    ): ExternalOidcProvidersConfig {
        // The list of providers is explicit to keep configuration simple and avoid parsing JSON.
        val rawNames = ctx.getConfigProperty("medatarun.auth.oidc.external.names") ?: return ExternalOidcProvidersConfig.empty()
        val names = parseCsv(rawNames)
        if (names.isEmpty()) {
            return ExternalOidcProvidersConfig.empty()
        }
        val durationRaw = ctx.getConfigProperty(
            "medatarun.auth.oidc.jwkscache.duration",
            ExternalOidcProvidersConfig.DEFAULT_CACHE_DURATION_SECONDS.toString()
        )
        val durationSeconds = durationRaw.toLongOrNull()
            ?: throw ExternalOidcProviderCacheDurationInvalidException(durationRaw)
        val providers = mutableListOf<ExternalOidcProviderConfig>()
        for (name in names) {
            val prefix = "medatarun.auth.oidc.external.$name"
            val issuer = ctx.getConfigProperty("$prefix.issuer")
                ?: throw ExternalOidcProviderMissingConfigException(name, "issuer")
            if (issuer == internalIssuer) {
                throw ExternalOidcProviderIssuerConflictException(issuer)
            }
            val jwksUri = ctx.getConfigProperty("$prefix.jwksUri")
                ?: throw ExternalOidcProviderMissingConfigException(name, "jwksUri")
            val audienceRaw = ctx.getConfigProperty("$prefix.audience")
                ?: throw ExternalOidcProviderMissingConfigException(name, "audience")
            val audiences = parseCsv(audienceRaw)
            if (audiences.isEmpty()) {
                throw ExternalOidcProviderMissingConfigException(name, "audience")
            }
            val algsRaw = ctx.getConfigProperty("$prefix.algs", "RS256")
            val algs = parseCsv(algsRaw)
            val allowedAlgs = if (algs.isEmpty()) listOf("RS256") else algs
            providers.add(
                ExternalOidcProviderConfig(
                    name = name,
                    issuer = issuer,
                    jwksUri = jwksUri,
                    audiences = audiences,
                    allowedAlgs = allowedAlgs
                )
            )
        }
        return ExternalOidcProvidersConfig(providers, durationSeconds)
    }

    private fun parseCsv(value: String): List<String> {
        if (value.isBlank()) {
            return emptyList()
        }
        val parts = value.split(",")
        val trimmed = mutableListOf<String>()
        for (part in parts) {
            val token = part.trim()
            if (token.isNotEmpty()) {
                trimmed.add(token)
            }
        }
        return trimmed
    }
}
