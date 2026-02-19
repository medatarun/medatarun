package io.medatarun.tags.core.domain

sealed interface TagCmd {

    class TagFreeCreate(val key: TagFreeKey, val name: String?, val description: String?): TagCmd
    class TagFreeUpdateName(val ref: FreeTagRef, val value: String?): TagCmd
    class TagFreeUpdateDescription(val ref: FreeTagRef, val value: String?): TagCmd
    class TagFreeUpdateKey(val ref: FreeTagRef, val value: TagFreeKey): TagCmd
    class TagFreeDelete(val ref: FreeTagRef): TagCmd

    class TagGroupCreate(val key: TagGroupKey, val name: String?, val description: String?): TagCmd
    class TagGroupUpdateName(val ref: TagGroupRef, val value: String): TagCmd
    class TagGroupUpdateDescription(val ref: TagGroupRef, val value: String): TagCmd
    class TagGroupUpdateKey(val ref: TagGroupRef, val value: TagGroupKey): TagCmd
    class TagGroupDelete(val ref: TagGroupRef): TagCmd

    class TagManagedCreate(val groupRef: TagGroupRef, val key: TagManagedKey, val name: String?, val description: String?): TagCmd
    class TagManagedUpdateName(val groupRef: TagGroupRef, val tagRef: TagManagedRef, val value: String): TagCmd
    class TagManagedUpdateDescription(val groupRef: TagGroupRef, val tagRef: TagManagedRef, val value: String): TagCmd
    class TagManagedUpdateKey(val groupRef: TagGroupRef, val tagRef: TagManagedRef, val value: TagManagedKey): TagCmd
    class TagManagedDelete(val groupRef: TagGroupRef, val tagRef: TagManagedRef): TagCmd
}
