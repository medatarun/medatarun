package io.medatarun.model.adapters

import io.medatarun.types.TypeJsonConverterBadFormatException
import kotlinx.serialization.json.JsonElement

class TypeJsonJsonObjectExpectedException() : TypeJsonConverterBadFormatException("expected a JsonObject")
class TypeJsonJsonStringExpectedException() : TypeJsonConverterBadFormatException("expected a JsonString")
class TypeJsonInvalidRefException(ref: String) : TypeJsonConverterBadFormatException("Invalid ref: $ref")
class TypeJsonInvalidRefSchemeException(ref: String) : TypeJsonConverterBadFormatException("Unsupported ref scheme: $ref")
class TypeJsonInvalidSearchFiltersSyntaxException(filters: JsonElement) : TypeJsonConverterBadFormatException("Invalid search filters syntax: '$filters'")
class TypeJsonInvalidSearchFieldSyntaxException(filters: JsonElement) : TypeJsonConverterBadFormatException("Invalid search fields syntax: '$filters'")