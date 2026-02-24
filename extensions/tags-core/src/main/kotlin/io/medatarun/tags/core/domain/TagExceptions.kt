package io.medatarun.tags.core.domain

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.http.StatusCode

class TagFreeDuplicateKeyException(): MedatarunException("Free tag with same key already exists", StatusCode.BAD_REQUEST)
class TagFreeNotFoundException(ref:String): MedatarunException("Free tag [${ref}] was not found", StatusCode.NOT_FOUND)
class TagNotFoundException(ref:String): MedatarunException("Tag [${ref}] was not found", StatusCode.NOT_FOUND)
class TagRefGlobalByKeyMissingGroupKeyException(): MedatarunException("Global tag ref by key requires a group key", StatusCode.BAD_REQUEST)
class TagRefLocalByKeyUnexpectedGroupKeyException(): MedatarunException("Local tag ref by key can not provide a group key", StatusCode.BAD_REQUEST)
class TagRefGlobalByKeySerializationMissingGroupKeyException(): MedatarunException("Global tag ref by key requires a group key to serialize", StatusCode.INTERNAL_SERVER_ERROR)
class TagScopeRefLocalUsesGlobalTypeException(): MedatarunException("Local scope ref can not use the global scope name", StatusCode.BAD_REQUEST)
class TagFreeCommandIncompatibleTagRefException(ref:String): MedatarunException("Tag ref [$ref] does not point to a free tag", StatusCode.BAD_REQUEST)
class TagFreeCommandIncompatibleTagScopeRefException(ref:String): MedatarunException("Tag scope ref [$ref] can not be used for a free tag", StatusCode.BAD_REQUEST)
class TagManagedCommandIncompatibleTagRefException(ref:String): MedatarunException("Tag ref [$ref] does not point to a managed tag", StatusCode.BAD_REQUEST)
class TagGroupNotFoundException(ref:String): MedatarunException("Tag group [${ref}] was not found", StatusCode.NOT_FOUND)
class TagGroupDuplicateKeyException(): MedatarunException("Tag group with same key already exists", StatusCode.BAD_REQUEST)
class TagManagedDuplicateKeyException(): MedatarunException("Managed tag with same key in same group already exists", StatusCode.BAD_REQUEST)
class TagManagedNotFoundException(tagRef: String): MedatarunException("Managed tag [$tagRef] was not found", StatusCode.NOT_FOUND)
class TagDuplicateScopeManagerException(scopeType: String): MedatarunException("Multiple TagScopeManager registered for scope type [$scopeType]", StatusCode.INTERNAL_SERVER_ERROR)
class TagScopeManagerNotFoundException(scopeType: String): MedatarunException("No TagScopeManager registered for scope type [$scopeType]", StatusCode.BAD_REQUEST)
class TagScopeNotFoundException(scopeRef: String): MedatarunException("Tag scope [$scopeRef] was not found", StatusCode.BAD_REQUEST)
class TagAttachScopeMismatchException(targetScope: String, tagScope: String, tagRef: String): MedatarunException("Tag [$tagRef] belongs to scope [$tagScope] and can not be attached to target scope [$targetScope]", StatusCode.BAD_REQUEST)
