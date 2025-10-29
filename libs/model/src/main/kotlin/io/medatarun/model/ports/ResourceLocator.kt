package io.medatarun.model.ports

interface ResourceLocator {
    fun getRootContent(): String
    fun getContent(path: String): String
    fun withPath(path: String): ResourceLocator
}