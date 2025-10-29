package io.medatarun.ext.frictionlessdata

import io.medatarun.model.model.MedatarunException
import kotlinx.serialization.json.JsonElement

class TableSchemaFieldTypeUnknown(e: JsonElement): MedatarunException("Unknown field type: $e")
class TableSchemaFormatUnknownException(e: String): MedatarunException("Unknown table schema format: $e")
class TableSchemaStringOrStringArrayException(): MedatarunException("string or array expected")
class DatapackageResourceSchemaAsPathNotSupported(): MedatarunException("In a datapackage, resources's schema expressed by a string (a path) are not supported.")
class FrictionlessConverterUnsupportedFileFormatException(name: String): MedatarunException("File format isn't supported. Could not guess how to read it with Frictionless tools")