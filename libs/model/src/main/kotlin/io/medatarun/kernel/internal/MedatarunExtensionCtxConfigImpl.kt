package io.medatarun.kernel.internal

import io.medatarun.kernel.MedatarunConfig
import io.medatarun.kernel.MedatarunExtensionCtxConfig

class MedatarunExtensionCtxConfigImpl(val config: MedatarunConfig): MedatarunExtensionCtxConfig {
    override fun getConfigProperty(key: String): String? {
        return config.getProperty(key)
    }

    override fun getConfigProperty(key: String, defaultValue: String): String {
        return config.getProperty(key, defaultValue)
    }
}