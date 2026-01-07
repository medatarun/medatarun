package io.medatarun.auth.embedded

import io.medatarun.auth.embedded.internal.AuthEmbeddedPwd
import io.medatarun.lang.http.StatusCode
import io.medatarun.model.domain.MedatarunException

class AuthEmbeddedServiceBootstrapNotReadyException() :
    MedatarunException("Auth embedded service is not ready. Bootstrap has not been done yet")

class AuthEmbeddedBoostrapAlreadyConsumedException() :
    MedatarunException("Boostrap already consumed.", StatusCode.GONE)

class AuthEmbeddedBoostrapBadSecretException() :
    MedatarunException("Bad bootstrap secret.", StatusCode.UNAUTHORIZED)

class AuthEmbeddedCreateUserPasswordFailException(val reason: AuthEmbeddedPwd.PasswordPolicyFailReason) :
    MedatarunException("Bad password: " + reason.label, StatusCode.BAD_REQUEST)
class AuthEmbeddedBadCredentialsException():
        MedatarunException("Bad credentials.", StatusCode.UNAUTHORIZED)