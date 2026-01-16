package io.medatarun.platform.kernel

import java.net.URI

interface ResourceLocator {
    fun getRootContent(): String
    fun getContent(path: String): String
    fun withPath(path: String): ResourceLocator
    fun resolveUri(path: String): URI

    /**
     * Resolves this URI against the root URI
     */
    fun resolveUri(candidate: URI): URI
}