package io.medatarun.auth

import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.auth.actions.AuthEmbeddedActionsProvider
import io.medatarun.auth.domain.ConfigProperties
import io.medatarun.auth.domain.JwtConfig
import io.medatarun.auth.infra.DbConnectionFactoryImpl
import io.medatarun.auth.infra.OidcStorageSQLite
import io.medatarun.auth.infra.UserStorageSQLite
import io.medatarun.auth.internal.*
import io.medatarun.auth.ports.exposed.BootstrapSecretLifecycle.Companion.DEFAULT_BOOTSTRAP_SECRET_PATH_NAME
import io.medatarun.auth.ports.exposed.JwtSigninKeyRegistry.Companion.DEFAULT_KEYSTORE_PATH_NAME
import io.medatarun.auth.ports.exposed.OAuthService
import io.medatarun.auth.ports.exposed.OidcService
import io.medatarun.auth.ports.exposed.UserService
import io.medatarun.auth.ports.needs.AuthClock
import io.medatarun.auth.ports.needs.OidcStorage
import io.medatarun.kernel.ExtensionId
import io.medatarun.kernel.MedatarunExtension
import io.medatarun.kernel.MedatarunExtensionCtx
import io.medatarun.kernel.MedatarunServiceCtx
import java.time.Instant

class AuthExtension() : MedatarunExtension {
    override val id: ExtensionId = "authEmbedded"
    override fun init(ctx: MedatarunExtensionCtx) {
        val actionProvider = AuthEmbeddedActionsProvider()
        ctx.register(ActionProvider::class, actionProvider)
    }

    override fun initServices(ctx: MedatarunServiceCtx) {
        val cfgBootstrapSecretPath = ctx.resolveApplicationHomePath(DEFAULT_BOOTSTRAP_SECRET_PATH_NAME)
        val cfgKeyStorePath = ctx.resolveApplicationHomePath(DEFAULT_KEYSTORE_PATH_NAME)
        val dbConnectionFactory = DbConnectionFactoryImpl(
            ctx.resolveApplicationHomePath("data/users.db").toAbsolutePath().toString()
        )
        val authClock = object : AuthClock {
            override fun now(): Instant = Instant.now()
        }
        val passwordEncryptionDefaultIterations = UserPasswordEncrypter.DEFAULT_ITERATIONS
        val cfgBootstrapSecret = ctx.getConfigProperty(ConfigProperties.BootstrapSecret.key)

        // ------------------------------------------
        // Should be the same in test initializations
        // ------------------------------------------

        val userStorage = UserStorageSQLite(dbConnectionFactory)
        val authStorage: OidcStorage = OidcStorageSQLite(dbConnectionFactory)

        val userClaimsService = UserClaimsService()
        val authEmbeddedKeyRegistry = JwtSigninKeyRegistryImpl(cfgKeyStorePath)
        val authEmbeddedKeys = authEmbeddedKeyRegistry.loadOrCreateKeys()

        val jwtCfg = JwtConfig(
            issuer = "urn:medatarun:${authEmbeddedKeys.kid}",  // stable tant que tes fichiers sont là
            audience = "medatarun",
            ttlSeconds = 3600
        )

        val bootstrapper = BootstrapSecretLifecycleImpl(cfgBootstrapSecretPath, cfgBootstrapSecret)

        val userService: UserService = UserServiceImpl(
            bootstrapDirPath = cfgBootstrapSecretPath,
            userStorage = userStorage,
            clock = authClock,
            passwordEncryptionIterations = passwordEncryptionDefaultIterations,
            bootstrapper = bootstrapper
        )

        val oauthService = OAuthServiceImpl(
            userService = userService,
            keys = authEmbeddedKeys,
            jwtConfig = jwtCfg,
            userClaimsService = userClaimsService
        )

        val oidcService: OidcService = OidcServiceImpl(
            userStorage = userStorage,
            oidcAuthCodeStorage = authStorage,
            userClaimsService = userClaimsService,
            oauthService = oauthService,
            authEmbeddedKeys = authEmbeddedKeys,
            jwtCfg = jwtCfg,
            clock = authClock,
            authCtxDurationSeconds = DEFAULT_AUTH_CTX_DURATION_SECONDS
        )

        ctx.register(UserService::class, userService)
        ctx.register(OidcService::class, oidcService)
        ctx.register(OAuthService::class, oauthService)
    }

    companion object {
        const val DEFAULT_AUTH_CTX_DURATION_SECONDS: Long = 60 * 15
    }
}

