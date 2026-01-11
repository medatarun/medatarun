package io.medatarun.auth.domain

import io.medatarun.auth.internal.UserPasswordEncrypter
import io.medatarun.auth.ports.exposed.BootstrapSecretLifecycle.Companion.SECRET_MIN_SIZE
import io.medatarun.lang.http.StatusCode
import io.medatarun.model.domain.MedatarunException

class BootstrapSecretNotReadyException() :
    MedatarunException("Auth embedded service is not ready. Bootstrap has not been done yet")
class BootstrapSecretPrefilledToShortException():
        MedatarunException("Bootstrap secret, when prefilled, shall have a minimum size of $SECRET_MIN_SIZE chars.")
class BootstrapSecretAlreadyConsumedException() :
    MedatarunException("Bootstrap already consumed.", StatusCode.GONE)

class BootstrapSecretBadSecretException() :
    MedatarunException("Bad bootstrap secret.", StatusCode.UNAUTHORIZED)

class UserCreatePasswordFailException(val reason: UserPasswordEncrypter.PasswordPolicyFailReason) :
    MedatarunException("Bad password: " + reason.label, StatusCode.BAD_REQUEST)

class AuthUnauthorizedException :
    MedatarunException("Bad credentials.", StatusCode.UNAUTHORIZED)

class UserNotFoundException :
    MedatarunException("User not found.", StatusCode.NOT_FOUND)

class UserAlreadyExistsException :
    MedatarunException("User already exists.", StatusCode.CONFLICT)

class ActorNotFoundException :
    MedatarunException("Actor not found.", StatusCode.NOT_FOUND)

class ActorCreateFailedWithNotFoundException:
    MedatarunException("Create failed. Can not find actor after creation.")

class ActorDisabledException() :MedatarunException("Actor is disabled.", StatusCode.FORBIDDEN)

class AuthUnknownRoleException(val key: String)
    : MedatarunException("Unknown role: $key")