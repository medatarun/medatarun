package io.medatarun.tags.core.domain

sealed interface TagCmd {

    class TagFreeCreate(val scopeRef: TagScopeRef, val key: TagKey, val name: String?, val description: String?): TagCmd
    class TagFreeUpdateName(val ref: TagRef, val value: String?): TagCmd
    class TagFreeUpdateDescription(val ref: TagRef, val value: String?): TagCmd
    class TagFreeUpdateKey(val ref: TagRef, val value: TagKey): TagCmd
    class TagFreeDelete(val ref: TagRef): TagCmd

    class TagGroupCreate(val key: TagGroupKey, val name: String?, val description: String?): TagCmd
    class TagGroupUpdateName(val ref: TagGroupRef, val value: String): TagCmd
    class TagGroupUpdateDescription(val ref: TagGroupRef, val value: String): TagCmd
    class TagGroupUpdateKey(val ref: TagGroupRef, val value: TagGroupKey): TagCmd
    class TagGroupDelete(val ref: TagGroupRef): TagCmd

    class TagManagedCreate(val groupRef: TagGroupRef, val key: TagKey, val name: String?, val description: String?): TagCmd
    class TagManagedUpdateName(val tagRef: TagRef, val value: String): TagCmd
    class TagManagedUpdateDescription(val tagRef: TagRef, val value: String): TagCmd
    class TagManagedUpdateKey(val tagRef: TagRef, val value: TagKey): TagCmd
    class TagManagedDelete(val tagRef: TagRef): TagCmd

    class TagScopeDelete(val scopeRef: TagScopeRef): TagCmd
}
