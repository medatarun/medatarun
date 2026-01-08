package io.medatarun.auth

import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.auth.actions.AuthEmbeddedActionsProvider
import io.medatarun.auth.infra.DbConnectionFactoryImpl
import io.medatarun.auth.internal.AuthEmbeddedPwd
import io.medatarun.auth.internal.AuthEmbeddedServiceImpl
import io.medatarun.auth.internal.UserStoreSQLite
import io.medatarun.auth.ports.exposed.AuthEmbeddedBootstrapSecret.Companion.DEFAULT_BOOTSTRAP_SECRET_PATH_NAME
import io.medatarun.auth.ports.exposed.AuthEmbeddedKeyRegistry.Companion.DEFAULT_KEYSTORE_PATH_NAME
import io.medatarun.auth.ports.exposed.AuthEmbeddedService
import io.medatarun.auth.ports.needs.AuthClock
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
        val userStorage = UserStoreSQLite(dbConnectionFactory)
        val authClock = object : AuthClock {
            override fun now(): Instant = Instant.now()
        }
        val authEmbeddedService: AuthEmbeddedService = AuthEmbeddedServiceImpl(
            bootstrapDirPath = cfgBootstrapSecretPath,
            keyStorePath = cfgKeyStorePath,
            userStorage = userStorage,
            clock = authClock,
            passwordEncryptionIterations = AuthEmbeddedPwd.DEFAULT_ITERATIONS
        )
        ctx.register(AuthEmbeddedService::class, authEmbeddedService)
    }
}