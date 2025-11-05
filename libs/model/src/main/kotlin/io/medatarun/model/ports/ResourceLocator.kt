package io.medatarun.model.ports

import java.net.URI

interface ResourceLocator {
    fun getRootContent(): String
    fun getContent(path: String): String
    fun withPath(path: String): ResourceLocator
    fun resolveUri(path: String): URI
}