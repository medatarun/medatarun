package io.medatarun.auth.domain

import io.medatarun.auth.domain.actor.ActorId
import io.medatarun.auth.domain.role.RoleId
import io.medatarun.auth.domain.role.RoleKey
import io.medatarun.auth.internal.users.UserPasswordEncrypter
import io.medatarun.auth.ports.exposed.BootstrapSecretLifecycle.Companion.SECRET_MIN_SIZE
import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.http.StatusCode

class BootstrapSecretNotReadyException :
    MedatarunException("Auth embedded service is not ready. Bootstrap has not been done yet")

class BootstrapSecretPrefilledToShortException :
    MedatarunException("Bootstrap secret, when prefilled, shall have a minimum size of $SECRET_MIN_SIZE chars.")

class BootstrapSecretAlreadyConsumedException :
    MedatarunException("Bootstrap already consumed.", StatusCode.GONE)

class BootstrapSecretBadSecretException :
    MedatarunException("Bad bootstrap secret.", StatusCode.UNAUTHORIZED)

class UserCreatePasswordFailException(reason: UserPasswordEncrypter.PasswordPolicyFailReason) :
    MedatarunException("Bad password: " + reason.label, StatusCode.BAD_REQUEST)

class AuthNotAuthenticatedException :
    MedatarunException("Bad credentials.", StatusCode.UNAUTHORIZED)

class AuthNotAuthorizedException :
    MedatarunException("Bad credentials.", StatusCode.FORBIDDEN)

class UserNotFoundException :
    MedatarunException("User not found.", StatusCode.NOT_FOUND)

class UserAlreadyExistsException :
    MedatarunException("User already exists.", StatusCode.CONFLICT)

class ActorNotFoundException :
    MedatarunException("Actor not found.", StatusCode.NOT_FOUND)

class ActorAddRoleAlreadyExistException(actorId: ActorId, roleId: RoleId) :
    MedatarunException("Can not add to actor. Actor [${actorId.value}] already has role [$roleId]", StatusCode.BAD_REQUEST)

class ActorDeleteRoleNotFoundException(actorId: ActorId, roleId: RoleId) :
    MedatarunException("Can not remove role from actor. Actor [${actorId.value}] doesn't have role [$roleId]", StatusCode.BAD_REQUEST)

class ActorCreateFailedWithNotFoundException :
    MedatarunException("Create failed. Can not find actor after creation.")

class ActorDisabledException : MedatarunException("Actor is disabled.", StatusCode.FORBIDDEN)

class AuthUnknownPermissionException(val key: String) : MedatarunException("Unknown role: $key")
class RoleNotFoundByIdException(roleId: RoleId) : MedatarunException("Role [$roleId] not found.", StatusCode.NOT_FOUND)
class RoleNotFoundByKeyException(roleKey: RoleKey) :
    MedatarunException("Role [$roleKey] not found.", StatusCode.NOT_FOUND)

class RoleAlreadyExistsException(val key: String) : MedatarunException("Role already exists: $key", StatusCode.CONFLICT)
class RolePermissionAlreadyExistsException(roleId: String, permission: String) :
    MedatarunException(
        "Role permission already exists for role=$roleId and permission=$permission",
        StatusCode.CONFLICT
    )

class RolePermissionNotFoundException(roleId: String, permission: String) :
    MedatarunException("Role permission not found for role=$roleId and permission=$permission", StatusCode.NOT_FOUND)

class UsernameEmptyException : MedatarunException("Username can not be empty", StatusCode.BAD_REQUEST)
class UsernameTooShortException(minsize: Int) :
    MedatarunException("Username shall be at least $minsize characters.", StatusCode.BAD_REQUEST)

class UsernameTooLongException(maxsize: Int) :
    MedatarunException("Username shall be at most $maxsize characters.", StatusCode.BAD_REQUEST)

class UsernameInvalidFormatException(minSize: Int, maxSize:Int) : MedatarunException("Usernames must be of $minSize to $maxSize characters, and contain only lowercase letters, numbers, dot, underscore or minus symbols.", StatusCode.BAD_REQUEST)


open class UserFullnameException(msg: String) :
    MedatarunException(msg, StatusCode.BAD_REQUEST)

class UserFullnameEmptyException :
    UserFullnameException("fullname is empty")

class UserFullnameTooLongException :
    UserFullnameException("fullname is too long")

class UserFullnameInvalidFormatException :
    UserFullnameException("invalid fullname format")

class UserDisableSelfException: MedatarunException("You can not disable yourself", StatusCode.BAD_REQUEST)
class UserEnableSelfException: MedatarunException("You can not disable yourself", StatusCode.BAD_REQUEST)
class ActorDisableSelfException: MedatarunException("You can not disable yourself", StatusCode.BAD_REQUEST)
class ActorEnableSelfException: MedatarunException("You can not disable yourself", StatusCode.BAD_REQUEST)

class RoleDeleteManagedForbiddenException(key: RoleKey): MedatarunException("Can not delete role [$key] because it is managed by the application.", StatusCode.BAD_REQUEST)
class RoleCreateConflictsWithManagedKeyException(key: RoleKey): MedatarunException("Can not create a new role with [$key] because the application already manages roles with the same key.", StatusCode.BAD_REQUEST)
class RoleUpdateKeyConflictsWithManagedKeyException(key : RoleKey): MedatarunException("Can not change the [$key] role key as the application manages this role itself.", StatusCode.BAD_REQUEST)
class RoleUpdatePermissionsManagedRoleException(key : RoleKey): MedatarunException("Can not change the permissions on [$key] role, as this role is managed by the application", StatusCode.BAD_REQUEST)