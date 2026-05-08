package io.medatarun.tags.core.domain

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.exceptions.MedatarunTechnicalException
import io.medatarun.lang.exceptions.MedatarunUserException
import io.medatarun.lang.http.StatusCode

class TagLocalDuplicateKeyException : MedatarunUserException("Local tag with same key already exists", StatusCode.BAD_REQUEST)
class TagLocalNotFoundException(ref:String): MedatarunUserException("Local tag [${ref}] was not found", StatusCode.NOT_FOUND)
class TagNotFoundException(ref:String): MedatarunUserException("Tag [${ref}] was not found", StatusCode.NOT_FOUND)
class TagActionNotAuthenticatedException : MedatarunUserException("Not authenticated", StatusCode.UNAUTHORIZED)
class TagRefGlobalByKeyMissingGroupKeyException : MedatarunUserException("Global tag ref by key requires a group key", StatusCode.BAD_REQUEST)
class TagRefLocalByKeyUnexpectedGroupKeyException : MedatarunUserException("Local tag ref by key can not provide a group key", StatusCode.BAD_REQUEST)
class TagRefGlobalByKeySerializationMissingGroupKeyException : MedatarunUserException("Global tag ref by key requires a group key to serialize", StatusCode.INTERNAL_SERVER_ERROR)
class TagScopeRefLocalUsesGlobalTypeException : MedatarunUserException("Local scope ref can not use the global scope name", StatusCode.BAD_REQUEST)
class TagLocalCommandIncompatibleTagRefException(ref:String): MedatarunUserException("Tag ref [$ref] does not point to a local tag", StatusCode.BAD_REQUEST)
class TagLocalCommandIncompatibleTagScopeRefException(ref:String): MedatarunUserException("Tag scope ref [$ref] can not be used for a local tag", StatusCode.BAD_REQUEST)
class TagGlobalCommandIncompatibleTagRefException(ref:String): MedatarunUserException("Tag ref [$ref] does not point to a global tag", StatusCode.BAD_REQUEST)
class TagGroupNotFoundException(ref:String): MedatarunUserException("Tag group [${ref}] was not found", StatusCode.NOT_FOUND)
class TagGroupDuplicateKeyException : MedatarunUserException("Tag group with same key already exists", StatusCode.BAD_REQUEST)
class TagGlobalDuplicateKeyException : MedatarunUserException("Global tag with same key in same group already exists", StatusCode.BAD_REQUEST)
class TagGlobalNotFoundException(tagRef: String): MedatarunUserException("Global tag [$tagRef] was not found", StatusCode.NOT_FOUND)
class TagDuplicateScopeManagerException(scopeType: String): MedatarunTechnicalException("Multiple TagScopeManager registered for scope type [$scopeType]", StatusCode.INTERNAL_SERVER_ERROR)
class TagScopeManagerNotFoundException(scopeType: String): MedatarunUserException("No TagScopeManager registered for scope type [$scopeType]", StatusCode.BAD_REQUEST)
class TagScopeNotFoundException(scopeRef: String): MedatarunUserException("Tag scope [$scopeRef] was not found", StatusCode.BAD_REQUEST)
class TagLocalScopeDeleteGlobalScopeException(scopeRef: String): MedatarunUserException("Tag local scope delete can not target global scope [$scopeRef]", StatusCode.BAD_REQUEST)
class TagAttachScopeMismatchException(targetScope: String, tagScope: String, tagRef: String): MedatarunUserException("Tag [$tagRef] belongs to scope [$tagScope] and can not be attached to target scope [$targetScope]", StatusCode.BAD_REQUEST)
