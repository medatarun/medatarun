package io.medatarun.model.adapters

import io.medatarun.types.TypeJsonConverterBadFormatException

class TypeJsonJsonObjectExpectedException() : TypeJsonConverterBadFormatException("expected a JsonObject")
class TypeJsonJsonStringExpectedException() : TypeJsonConverterBadFormatException("expected a JsonString")
class TypeJsonInvalidRefMissingKeyException(name: String) : TypeJsonConverterBadFormatException("Invalid ref, missing $name in key parts.")
class TypeJsonInvalidRefException(ref: String) : TypeJsonConverterBadFormatException("Invalid ref: $ref")
class TypeJsonInvalidRefSchemeException(ref: String) : TypeJsonConverterBadFormatException("Unsupported ref scheme: $ref")
class TypeJsonInvalidRefQuerySegmentException(segment: String) : TypeJsonConverterBadFormatException("Invalid query segment: '$segment'")
class TypeJsonInvalidRefDuplicateQueryParamException(key: String) : TypeJsonConverterBadFormatException("Duplicate query param: '$key'")