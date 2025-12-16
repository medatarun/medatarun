package io.metadatarun.ext.config

import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.kernel.MedatarunExtension
import io.medatarun.kernel.MedatarunExtensionCtx
import io.metadatarun.ext.config.actions.ConfigActionProvider
import org.slf4j.LoggerFactory

class ConfigExtension : MedatarunExtension {

    override val id = "db"
    override fun init(ctx: MedatarunExtensionCtx) {
        ctx.register(ActionProvider::class, ConfigActionProvider())
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ConfigExtension::class.java)
    }
}