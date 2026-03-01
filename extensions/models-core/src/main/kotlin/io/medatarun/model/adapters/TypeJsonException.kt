package io.medatarun.model.adapters

import io.medatarun.types.TypeJsonConverterBadFormatException
import kotlinx.serialization.json.JsonElement

class TypeJsonInvalidSearchFiltersSyntaxException(filters: JsonElement) : TypeJsonConverterBadFormatException("Invalid search filters syntax: '$filters'")
class TypeJsonInvalidSearchFieldSyntaxException(filters: JsonElement) : TypeJsonConverterBadFormatException("Invalid search fields syntax: '$filters'")