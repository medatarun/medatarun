package io.medatarun.tags.core.domain

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.http.StatusCode

class TagFreeDuplicateKeyException(): MedatarunException("Free tag with same key already exists", StatusCode.BAD_REQUEST)
class TagFreeNotFoundException(ref:String): MedatarunException("Free tag [${ref}] was not found", StatusCode.NOT_FOUND)
class TagNotFoundException(ref:String): MedatarunException("Tag [${ref}] was not found", StatusCode.NOT_FOUND)
class TagGroupNotFoundException(ref:String): MedatarunException("Tag group [${ref}] was not found", StatusCode.NOT_FOUND)
class TagGroupDuplicateKeyException(): MedatarunException("Tag group with same key already exists", StatusCode.BAD_REQUEST)
class TagManagedDuplicateKeyException(): MedatarunException("Managed tag with same key in same group already exists", StatusCode.BAD_REQUEST)
class TagManagedNotFoundException(groupRef: String, tagRef: String): MedatarunException("Managed tag [$tagRef] not found in tag group [$groupRef]", StatusCode.BAD_REQUEST)
