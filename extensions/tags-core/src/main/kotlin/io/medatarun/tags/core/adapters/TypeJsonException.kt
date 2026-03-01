package io.medatarun.tags.core.adapters

import io.medatarun.types.TypeJsonConverterBadFormatException
import kotlinx.serialization.json.JsonElement

class TypeJsonInvalidTagSearchFiltersSyntaxException(filters: JsonElement) :
    TypeJsonConverterBadFormatException("Invalid tag search filters syntax: '$filters'")
