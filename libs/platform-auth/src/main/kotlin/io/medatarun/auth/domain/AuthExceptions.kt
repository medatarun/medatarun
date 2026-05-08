package io.medatarun.auth.domain

import io.medatarun.auth.domain.actor.ActorId
import io.medatarun.auth.domain.role.RoleId
import io.medatarun.auth.domain.role.RoleKey
import io.medatarun.auth.internal.users.UserPasswordEncrypter
import io.medatarun.auth.ports.exposed.BootstrapSecretLifecycle.Companion.SECRET_MIN_SIZE
import io.medatarun.lang.exceptions.MedatarunTechnicalException
import io.medatarun.lang.exceptions.MedatarunUserException
import io.medatarun.lang.http.StatusCode

class BootstrapSecretNotReadyException :
    MedatarunTechnicalException("Auth embedded service is not ready. Bootstrap has not been done yet")

class BootstrapSecretPrefilledToShortException :
    MedatarunTechnicalException("Bootstrap secret, when prefilled, shall have a minimum size of $SECRET_MIN_SIZE chars.")

class BootstrapSecretAlreadyConsumedException :
    MedatarunTechnicalException("Bootstrap already consumed.", StatusCode.GONE)

class BootstrapSecretBadSecretException :
    MedatarunTechnicalException("Bad bootstrap secret.", StatusCode.UNAUTHORIZED)

class UserCreatePasswordFailException(reason: UserPasswordEncrypter.PasswordPolicyFailReason) :
    MedatarunUserException("Bad password: " + reason.label, StatusCode.BAD_REQUEST)

class AuthNotAuthenticatedException :
    MedatarunUserException("Bad credentials.", StatusCode.UNAUTHORIZED)

class AuthNotAuthorizedException :
    MedatarunUserException("Bad credentials.", StatusCode.FORBIDDEN)

class UserNotFoundException :
    MedatarunUserException("User not found.", StatusCode.NOT_FOUND)

class UserAlreadyExistsException :
    MedatarunUserException("User already exists.", StatusCode.CONFLICT)

class ActorNotFoundException :
    MedatarunUserException("Actor not found.", StatusCode.NOT_FOUND)

class ActorAddRoleAlreadyExistException(actorId: ActorId, roleId: RoleId) :
    MedatarunUserException(
        "Can not add to actor. Actor [${actorId.asString()}] already has role [${roleId.asString()}]",
        StatusCode.BAD_REQUEST
    )

class ActorDeleteRoleNotFoundException(actorId: ActorId, roleId: RoleId) :
    MedatarunUserException(
        "Can not remove role from actor. Actor [${actorId.asString()}] doesn't have role [${roleId.asString()}]",
        StatusCode.BAD_REQUEST
    )

class ActorCreateFailedWithNotFoundException :
    MedatarunTechnicalException("Create failed. Can not find actor after creation.")

class ActorDisabledException : MedatarunUserException("Actor is disabled.", StatusCode.FORBIDDEN)

class AuthUnknownPermissionException(val key: String) :
    MedatarunUserException("Unknown permission [$key].", StatusCode.BAD_REQUEST)

class RoleNotFoundByIdException(roleId: RoleId) :
    MedatarunUserException("Role [$roleId] not found.", StatusCode.NOT_FOUND)

class RoleNotFoundByKeyException(roleKey: RoleKey) :
    MedatarunUserException("Role [${roleKey.asString()}] not found.", StatusCode.NOT_FOUND)

class RoleAlreadyExistsException(val key: String) :
    MedatarunUserException("Role [$key] already exists.", StatusCode.CONFLICT)

class RolePermissionAlreadyExistsException(roleId: String, permission: String) :
    MedatarunUserException(
        "Role permission already exists for role=$roleId and permission=$permission",
        StatusCode.CONFLICT
    )

class RolePermissionNotFoundException(roleId: String, permission: String) :
    MedatarunUserException(
        "Role permission not found for role [$roleId] and permission [$permission]",
        StatusCode.NOT_FOUND
    )

class UsernameEmptyException : MedatarunUserException("Username can not be empty", StatusCode.BAD_REQUEST)
class UsernameTooShortException(minsize: Int) :
    MedatarunUserException("Username shall be at least $minsize characters.", StatusCode.BAD_REQUEST)

class UsernameTooLongException(maxsize: Int) :
    MedatarunUserException("Username shall be at most $maxsize characters.", StatusCode.BAD_REQUEST)

class UsernameInvalidFormatException(minSize: Int, maxSize: Int) : MedatarunUserException(
    "Usernames must be of $minSize to $maxSize characters, and contain only lowercase letters, numbers, dot, underscore or minus symbols.",
    StatusCode.BAD_REQUEST
)


open class UserFullnameException(msg: String) :
    MedatarunUserException(msg, StatusCode.BAD_REQUEST)

class UserFullnameEmptyException :
    UserFullnameException("fullname is empty")

class UserFullnameTooLongException :
    UserFullnameException("fullname is too long")

class UserFullnameInvalidFormatException :
    UserFullnameException("invalid fullname format")

class UserDisableSelfException : MedatarunUserException("You can not disable yourself", StatusCode.BAD_REQUEST)
class UserEnableSelfException : MedatarunUserException("You can not disable yourself", StatusCode.BAD_REQUEST)
class ActorDisableSelfException : MedatarunUserException("You can not disable yourself", StatusCode.BAD_REQUEST)
class ActorEnableSelfException : MedatarunUserException("You can not disable yourself", StatusCode.BAD_REQUEST)

class RoleDeleteManagedForbiddenException(key: RoleKey) : MedatarunUserException(
    "Can not delete role [${key.asString()}] because it is managed by the application.",
    StatusCode.BAD_REQUEST
)

class RoleCreateConflictsWithManagedKeyException(key: RoleKey) : MedatarunUserException(
    "Can not create a new role with [${key.asString()}] because the application already manages roles with the same key.",
    StatusCode.BAD_REQUEST
)

class RoleUpdateKeyConflictsWithManagedKeyException(key: RoleKey) : MedatarunUserException(
    "Can not change this role key to [${key.asString()}] because the application manages another role with this key.",
    StatusCode.BAD_REQUEST
)

class RoleUpdatePermissionsManagedRoleException(key: RoleKey) : MedatarunUserException(
    "Can not change the permissions on [${key.asString()}] role, as this role is managed by the application",
    StatusCode.BAD_REQUEST
)

class RoleUpdateAutoAssignAdminRoleForbiddenException() :
    MedatarunUserException("Can not automatically assign admin role. Too dangerous.", StatusCode.BAD_REQUEST)
