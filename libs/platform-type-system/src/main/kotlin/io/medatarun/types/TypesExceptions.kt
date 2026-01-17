package io.medatarun.types

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.http.StatusCode

class TypeJsonEquivUnknownException(code: String) : MedatarunException("Unknown TypeJsonEquiv [$code]")
class TypeJsonConverterIllegalNullException : MedatarunException("Unexpected null value in TypeJsonConverter. Should have been handled before.")
open class TypeJsonConverterBadFormatException(msg: String) : MedatarunException(msg, StatusCode.BAD_REQUEST)