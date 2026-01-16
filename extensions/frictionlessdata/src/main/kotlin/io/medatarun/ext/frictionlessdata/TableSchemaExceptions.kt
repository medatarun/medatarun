package io.medatarun.ext.frictionlessdata

import io.medatarun.lang.exceptions.MedatarunException
import kotlinx.serialization.json.JsonElement

class TableSchemaFieldTypeUnknown(e: JsonElement): MedatarunException("Unknown field type: $e")
class TableSchemaFormatUnknownException(e: String): MedatarunException("Unknown table schema format: $e")
class TableSchemaStringOrStringArrayException: MedatarunException("string or array expected")
class FrictionlessConverterUnsupportedFileFormatException(name: String): MedatarunException("File format isn't supported. Could not guess how to read it with Frictionless tools. $name")
class StringOrTableSchemaDecodeException: MedatarunException("Can not decode jsonObject, could not find a string or a Json representing a table schema")