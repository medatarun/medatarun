package io.medatarun.auth

import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.auth.actions.AuthEmbeddedActionsProvider
import io.medatarun.auth.domain.AuthEmbeddedJwtConfig
import io.medatarun.auth.infra.AuthorizeStorageSQLite
import io.medatarun.auth.infra.DbConnectionFactoryImpl
import io.medatarun.auth.infra.UserStoreSQLite
import io.medatarun.auth.internal.*
import io.medatarun.auth.ports.exposed.AuthEmbeddedBootstrapSecret.Companion.DEFAULT_BOOTSTRAP_SECRET_PATH_NAME
import io.medatarun.auth.ports.exposed.AuthEmbeddedKeyRegistry.Companion.DEFAULT_KEYSTORE_PATH_NAME
import io.medatarun.auth.ports.exposed.AuthEmbeddedOIDCService
import io.medatarun.auth.ports.exposed.AuthEmbeddedUserService
import io.medatarun.auth.ports.exposed.OAuthService
import io.medatarun.auth.ports.needs.AuthClock
import io.medatarun.auth.ports.needs.AuthorizeStorage
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
        val passwordEncryptionDefaultIterations = AuthEmbeddedPwd.DEFAULT_ITERATIONS

        // ------------------------------------------
        // Should be the same in test initializations
        // ------------------------------------------

        val userStorage = UserStoreSQLite(dbConnectionFactory)
        val authStorage: AuthorizeStorage = AuthorizeStorageSQLite(dbConnectionFactory)

        val userClaimsService = UserClaimsService()
        val authEmbeddedKeyRegistry = AuthEmbeddedKeyRegistryImpl(cfgKeyStorePath)
        val authEmbeddedKeys = authEmbeddedKeyRegistry.loadOrCreateKeys()

        val jwtCfg = AuthEmbeddedJwtConfig(
            issuer = "urn:medatarun:${authEmbeddedKeys.kid}",  // stable tant que tes fichiers sont là
            audience = "medatarun",
            ttlSeconds = 3600
        )

        val userService: AuthEmbeddedUserService = AuthEmbeddedUserServiceImpl(
            bootstrapDirPath = cfgBootstrapSecretPath,
            userStorage = userStorage,
            clock = authClock,
            passwordEncryptionIterations = passwordEncryptionDefaultIterations
        )

        val oauthService = OAuthServiceImpl(
            userService = userService,
            keys = authEmbeddedKeys,
            jwtConfig = jwtCfg,
            userClaimsService = userClaimsService
        )

        val oidcService: AuthEmbeddedOIDCService = AuthEmbeddedOIDCServiceImpl(
            oidcAuthorizeService = AuthEmbeddedOIDCAuthorizeService(
                storage = authStorage,
                clock = authClock,
                authCtxDurationSeconds = DEFAULT_AUTH_CTX_DURATION_SECONDS
            ),
            userStorage = userStorage,
            oidcAuthCodeStorage = authStorage,
            userClaimsService = userClaimsService,
            oauthService = oauthService,
            authEmbeddedKeys = authEmbeddedKeys,
            jwtCfg = jwtCfg
        )

        ctx.register(AuthEmbeddedUserService::class, userService)
        ctx.register(AuthEmbeddedOIDCService::class, oidcService)
        ctx.register(OAuthService::class, oauthService)
    }

    companion object {
        const val DEFAULT_AUTH_CTX_DURATION_SECONDS: Long = 60 * 15
    }
}