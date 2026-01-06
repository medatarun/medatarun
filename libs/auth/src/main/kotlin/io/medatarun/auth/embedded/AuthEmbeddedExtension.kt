package io.medatarun.auth.embedded

import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.auth.embedded.AuthEmbeddedBootstrapSecret.Companion.DEFAULT_BOOTSTRAP_SECRET_PATH_NAME
import io.medatarun.auth.embedded.AuthEmbeddedKeyRegistry.Companion.DEFAULT_KEYSTORE_PATH_NAME
import io.medatarun.auth.embedded.internal.AuthEmbeddedServiceImpl
import io.medatarun.kernel.ExtensionId
import io.medatarun.kernel.MedatarunExtension
import io.medatarun.kernel.MedatarunExtensionCtx
import io.medatarun.kernel.MedatarunServiceCtx

class AuthEmbeddedExtension() : MedatarunExtension {
    override val id: ExtensionId = "authEmbedded"
    override fun init(ctx: MedatarunExtensionCtx) {
        val actionProvider = AuthEmbeddedActionsProvider()
        ctx.register(ActionProvider::class, actionProvider)
    }

    override fun initServices(ctx: MedatarunServiceCtx) {
        val cfgBootstrapSecretPath = ctx.resolveApplicationHomePath(DEFAULT_BOOTSTRAP_SECRET_PATH_NAME)
        val cfgKeyStorePath = ctx.resolveApplicationHomePath(DEFAULT_KEYSTORE_PATH_NAME)
        val authEmbeddedService: AuthEmbeddedService = AuthEmbeddedServiceImpl(
            bootstrapDirPath = cfgBootstrapSecretPath,
            keyStorePath = cfgKeyStorePath
        )
        ctx.register(AuthEmbeddedService::class, authEmbeddedService)
    }
}