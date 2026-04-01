package io.medatarun.platform.kernel

import io.medatarun.lang.config.ConfigPropertyDescription

enum class MedatarunKernelConfigProperties(
    override val key: String,
    override val type: String,
    override val defaultValue: String,
    override val description: String

): ConfigPropertyDescription {
    BaseUrl(
        "medatarun.public.base.url",
        "String",
        "<generated>",
        """Public base URL of the Medatarun instance.
This is the externally visible URL used for generated links and redirects.
Override it when Medatarun is deployed behind a reverse proxy or accessed via a different hostname.
If not set, it is derived from the server host and port (http://<host>:<port>)."""
    )
}