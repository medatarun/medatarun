package io.medatarun.auth.ports.needs

import io.medatarun.lang.strings.trimToNull
import java.net.URI

class OidcProviderConfig(val authority: URI,val  clientId: String) {
    companion object {
        fun valueOf(authorityStr: String?, clientIdStr: String?): OidcProviderConfig? {
            val authority = authorityStr?.trimToNull()?.let { URI(it) }
            val clientId = clientIdStr?.trimToNull()
            return if (authority != null && clientId != null) OidcProviderConfig(authority, clientId) else null
        }
    }
}
