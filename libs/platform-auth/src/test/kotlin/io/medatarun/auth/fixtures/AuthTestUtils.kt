package io.medatarun.auth.fixtures

import io.medatarun.auth.ports.exposed.AuthJwtExternalPrincipal
import java.net.URI
import java.net.URLDecoder
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64

object AuthTestUtils {
    fun createJwtExternalPrincipal(
        issuer: String,
        subject: String,
        name: String? = null,
        fullname: String? = null,
        preferredUsername: String? = null,
        email: String? = null
    ): AuthJwtExternalPrincipal {
        return object : AuthJwtExternalPrincipal {
            override val issuer: String = issuer
            override val subject: String = subject
            override val issuedAt: Instant? = null
            override val expiresAt: Instant? = null
            override val audience: List<String> = emptyList()
            override val name: String? = name
            override val fullname: String? = fullname
            override val preferredUsername: String? = preferredUsername
            override val email: String? = email
        }
    }

    fun pkceChallengeForTest(verifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(verifier.toByteArray(Charsets.US_ASCII))
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(hash)
    }

    fun parseQueryParams(uri: String): Map<String, String> {
        val query = URI(uri).rawQuery ?: return emptyMap()
        val params = mutableMapOf<String, String>()
        val pairs = query.split("&")
        for (pair in pairs) {
            if (pair.isEmpty()) {
                continue
            }
            val splitIndex = pair.indexOf("=")
            if (splitIndex < 0) {
                val key = URLDecoder.decode(pair, Charsets.UTF_8)
                params[key] = ""
            } else {
                val key = URLDecoder.decode(pair.substring(0, splitIndex), Charsets.UTF_8)
                val value = URLDecoder.decode(pair.substring(splitIndex + 1), Charsets.UTF_8)
                params[key] = value
            }
        }
        return params
    }

}