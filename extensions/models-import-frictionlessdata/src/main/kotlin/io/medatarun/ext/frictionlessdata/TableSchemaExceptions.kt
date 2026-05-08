package io.medatarun.ext.frictionlessdata

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.exceptions.MedatarunTechnicalException
import io.medatarun.lang.exceptions.MedatarunUserException
import io.medatarun.lang.http.StatusCode
import kotlinx.serialization.json.JsonElement

class TableSchemaFieldTypeUnknown(e: JsonElement): MedatarunUserException("Unknown field type: $e", StatusCode.UNPROCESSABLE_CONTENT)
class TableSchemaFormatUnknownException(e: String): MedatarunUserException("Unknown table schema format: $e", StatusCode.UNPROCESSABLE_CONTENT)
class TableSchemaStringOrStringArrayException: MedatarunUserException("string or array expected", StatusCode.UNPROCESSABLE_CONTENT)
class FrictionlessConverterUnsupportedFileFormatException(name: String): MedatarunUserException("File format isn't supported. Could not guess how to read it with Frictionless tools. $name", StatusCode.UNPROCESSABLE_CONTENT)
class StringOrTableSchemaDecodeException: MedatarunUserException("Can not decode jsonObject, could not find a string or a Json representing a table schema", StatusCode.UNPROCESSABLE_CONTENT)
class FrictionlessConverterEntityIdentifierNotFound(entityName:String, pk:String): MedatarunUserException("Could not find a way to determine entity [$entityName] identifier. Either schema references a wrong primary, or uses composite primary keys, or schema has no fields. Primary key was resolved as [$pk]", StatusCode.UNPROCESSABLE_CONTENT)
class FrictionlessConverterTypeNotFound(entityName:String, type:String): MedatarunUserException("Could not find matching type $entityName for $type", StatusCode.UNPROCESSABLE_CONTENT)