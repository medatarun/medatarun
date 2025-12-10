package io.medatarun.model.domain

@JvmInline
value class TypeKey(val value : String ) {
    fun validated(): TypeKey {
        if (value.length==0) throw InvalidModelTypeIdException()
        if (value.length>20) throw InvalidModelTypeIdException()
        return this
    }
}

class InvalidModelTypeIdException : MedatarunException("ModelTypeId is invalid")

/**
 * Defines one of the types known by the model, that can be used as [AttributeDef]
 */
interface ModelType {
    /**
     * Unique type identifier in the model
     */
    val id: TypeKey

    /**
     * Display name of the type
     */
    val name: LocalizedText?
    /**
     * Display description of the type
     */
    val description: LocalizedText?
}