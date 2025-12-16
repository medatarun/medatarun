package io.medatarun.kernel

interface MedatarunExtensionCtxConfig {
    fun getConfigProperty(key: String): String?
    fun getConfigProperty(key: String, defaultValue: String): String

}