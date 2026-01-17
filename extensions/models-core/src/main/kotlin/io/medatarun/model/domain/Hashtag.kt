package io.medatarun.model.domain

@JvmInline
value class Hashtag(val value:String) {
    fun validated(): Hashtag {
        return this
    }
}
