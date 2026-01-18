package io.medatarun.types

import kotlinx.serialization.json.JsonElement

interface TypeJsonConverter<T> {
    /**
     * Converts a JSON element into a value
     *
     * You should not receive [kotlinx.serialization.json.JsonNull], if so throw [TypeJsonConverterIllegalNullException]
     *
     * If the JSON format is incorrect (typically you expect a String and you get Number),
     * throw a subclass of [TypeJsonConverterBadFormatException]
     *
     * Do NOT validate data itself, just build the object. Syntax validation will be done in a second step,
     * on the object you return (typically with the [TypeDescriptor.validate] function)
     */
    fun deserialize(json: JsonElement): T
}

