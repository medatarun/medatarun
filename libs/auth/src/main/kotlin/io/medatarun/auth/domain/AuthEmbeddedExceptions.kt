package io.medatarun.auth.domain

import io.medatarun.auth.internal.AuthEmbeddedPwd
import io.medatarun.lang.http.StatusCode
import io.medatarun.model.domain.MedatarunException

class AuthEmbeddedServiceBootstrapNotReadyException() :
    MedatarunException("Auth embedded service is not ready. Bootstrap has not been done yet")

class AuthEmbeddedBootstrapAlreadyConsumedException() :
    MedatarunException("Bootstrap already consumed.", StatusCode.GONE)

class AuthEmbeddedBootstrapBadSecretException() :
    MedatarunException("Bad bootstrap secret.", StatusCode.UNAUTHORIZED)

class AuthEmbeddedCreateUserPasswordFailException(val reason: AuthEmbeddedPwd.PasswordPolicyFailReason) :
    MedatarunException("Bad password: " + reason.label, StatusCode.BAD_REQUEST)
class AuthEmbeddedBadCredentialsException():
        MedatarunException("Bad credentials.", StatusCode.UNAUTHORIZED)
class AuthEmbeddedUserNotFoundException():
        MedatarunException("User not found.", StatusCode.NOT_FOUND)
class AuthEmbeddedUserAlreadyExistsException:
        MedatarunException("User already exists.", StatusCode.CONFLICT)