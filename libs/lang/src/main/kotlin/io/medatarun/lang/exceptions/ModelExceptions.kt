package io.medatarun.lang.exceptions

import io.medatarun.lang.http.StatusCode

open class MedatarunException(message: String, val httpStatusCode: StatusCode = StatusCode.INTERNAL_SERVER_ERROR) : Exception(message)