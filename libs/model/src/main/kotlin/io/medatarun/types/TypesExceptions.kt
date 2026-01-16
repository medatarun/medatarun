package io.medatarun.types

import io.medatarun.lang.exceptions.MedatarunException

class TypeJsonEquivUnknownException(code: String) : MedatarunException("Unknown TypeJsonEquiv [$code]")