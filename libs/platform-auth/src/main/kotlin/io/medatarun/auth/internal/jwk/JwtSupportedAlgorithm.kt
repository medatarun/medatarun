package io.medatarun.auth.internal.jwk

import io.medatarun.auth.domain.JwtUnsupportedAlgException

enum class JwtSupportedAlgorithm(val key: String) {
    RS256("RS256"), ES256("ES256");

    companion object {
        private val map = entries.associateBy(JwtSupportedAlgorithm::key)
        fun valueOfKey(key: String): JwtSupportedAlgorithm {
            return map[key] ?: throw JwtUnsupportedAlgException(key)
        }
    }
}