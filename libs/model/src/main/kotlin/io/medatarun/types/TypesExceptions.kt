package io.medatarun.types

import io.medatarun.lang.exceptions.MedatarunException

class ActionParamJsonTypeUnknownException(code: String) : MedatarunException("Unknown ActionParamJsonType [$code]")