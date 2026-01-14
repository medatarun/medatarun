package io.medatarun.auth.internal.jwk

class MapConfigResolver(
    private val config: Map<String, String>
) : JwkExternalProvidersImpl.Companion.ConfigResolver {
    override fun getConfigProperty(key: String, defaultValue: String): String {
        val value = config[key]
        return value ?: defaultValue
    }

    override fun getConfigProperty(key: String): String? {
        return config[key]
    }
}
