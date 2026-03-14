package io.medatarun.actions.domain

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.http.StatusCode

class ActionExceptionInterpreter(
    val exception: Throwable
) {

    val statusCode: Int
        get() = when (exception) {
            is MedatarunException -> exception.httpStatusCode.httpStatusCode
            else -> StatusCode.INTERNAL_SERVER_ERROR.httpStatusCode
        }

    val statusName: String
        get() = when (exception) {
            is MedatarunException -> exception.httpStatusCode.message
            else -> StatusCode.INTERNAL_SERVER_ERROR.message
        }

    fun isInternal(): Boolean {
        return when(exception) {
            is MedatarunException -> exception.httpStatusCode.httpStatusCode  >= StatusCode.INTERNAL_SERVER_ERROR.httpStatusCode
            else -> true
        }
    }

    fun publicErrorMessage(): String {
        return when(exception) {
            is MedatarunException -> if (isInternal()) "Invocation failed" else exception.msg
            else -> exception.message ?: "Invocation error"
        }
    }
    fun privateErrorMessage(): String {
        return when(exception) {
            is MedatarunException -> exception.msg
            else -> exception.message ?: "Invocation error"
        }
    }

    fun details(): Map<String, String> {
        return when(exception) {
            is ActionInvocationException -> exception.payload
            is MedatarunException -> mapOf("details" to publicErrorMessage())
            else -> mapOf("details" to (exception.message ?: exception::class.simpleName).toString())
        }
    }


}