package io.medatarun.lang.exceptions

import io.medatarun.lang.http.StatusCode

/**
 * Root of all exceptions
 */
open class MedatarunException(
    /** Exception user message */
    val msg: String,
    /** HTTP status code to send with exception when exception goes through network */
    val httpStatusCode: StatusCode = StatusCode.INTERNAL_SERVER_ERROR,
    /** Indicates if the user can see the message (not the details) or if the message must be hidden */
    val userVisible: Boolean = true
) : Exception(msg)

/**
 * Internal technical exception where the message must be hidden to users
 */
open class MedatarunTechnicalException(msg: String, httpStatusCode: StatusCode = StatusCode.INTERNAL_SERVER_ERROR) :
    MedatarunException(msg, httpStatusCode, userVisible = false)

/**
 * Exception where the message should be displayed to users
 */
open class MedatarunUserException(msg: String, httpStatusCode: StatusCode = StatusCode.UNPROCESSABLE_CONTENT) :
    MedatarunException(msg, httpStatusCode, userVisible = true)