package io.medatarun.types

import io.medatarun.model.domain.MedatarunException

class ActionParamJsonTypeUnknownException(code: String) : MedatarunException("Unknown ActionParamJsonType [$code]")