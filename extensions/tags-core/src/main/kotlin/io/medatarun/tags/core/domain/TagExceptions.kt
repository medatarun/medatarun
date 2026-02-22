package io.medatarun.tags.core.domain

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.http.StatusCode

class TagFreeDuplicateKeyException(): MedatarunException("Free tag with same key already exists", StatusCode.BAD_REQUEST)
class TagFreeNotFoundException(ref:String): MedatarunException("Free tag [${ref}] was not found", StatusCode.NOT_FOUND)
class TagNotFoundException(ref:String): MedatarunException("Tag [${ref}] was not found", StatusCode.NOT_FOUND)
class TagFreeCommandIncompatibleTagRefException(ref:String): MedatarunException("Tag ref [$ref] does not point to a free tag", StatusCode.BAD_REQUEST)
class TagFreeCommandIncompatibleTagScopeRefException(ref:String): MedatarunException("Tag scope ref [$ref] can not be used for a free tag", StatusCode.BAD_REQUEST)
class TagManagedCommandIncompatibleTagRefException(ref:String): MedatarunException("Tag ref [$ref] does not point to a managed tag", StatusCode.BAD_REQUEST)
class TagGroupNotFoundException(ref:String): MedatarunException("Tag group [${ref}] was not found", StatusCode.NOT_FOUND)
class TagGroupDuplicateKeyException(): MedatarunException("Tag group with same key already exists", StatusCode.BAD_REQUEST)
class TagManagedDuplicateKeyException(): MedatarunException("Managed tag with same key in same group already exists", StatusCode.BAD_REQUEST)
class TagManagedNotFoundException(tagRef: String): MedatarunException("Managed tag [$tagRef] was not found", StatusCode.NOT_FOUND)
class TagDuplicateScopeManagerException(scopeType: String): MedatarunException("Multiple TagScopeManager registered for scope type [$scopeType]", StatusCode.INTERNAL_SERVER_ERROR)
