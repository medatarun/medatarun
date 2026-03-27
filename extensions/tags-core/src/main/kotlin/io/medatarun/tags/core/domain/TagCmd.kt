package io.medatarun.tags.core.domain

sealed interface TagCmd {

    class TagLocalCreate(val scopeRef: TagScopeRef, val key: TagKey, val name: String?, val description: String?): TagCmd
    class TagLocalUpdateName(val ref: TagRef, val value: String?): TagCmd
    class TagLocalUpdateDescription(val ref: TagRef, val value: String?): TagCmd
    class TagLocalUpdateKey(val ref: TagRef, val value: TagKey): TagCmd
    class TagLocalDelete(val ref: TagRef): TagCmd

    class TagGroupCreate(val key: TagGroupKey, val name: String?, val description: String?): TagCmd
    class TagGroupUpdateName(val ref: TagGroupRef, val value: String): TagCmd
    class TagGroupUpdateDescription(val ref: TagGroupRef, val value: String): TagCmd
    class TagGroupUpdateKey(val ref: TagGroupRef, val value: TagGroupKey): TagCmd
    class TagGroupDelete(val ref: TagGroupRef): TagCmd

    class TagGlobalCreate(val groupRef: TagGroupRef, val key: TagKey, val name: String?, val description: String?): TagCmd
    class TagGlobalUpdateName(val tagRef: TagRef, val value: String): TagCmd
    class TagGlobalUpdateDescription(val tagRef: TagRef, val value: String): TagCmd
    class TagGlobalUpdateKey(val tagRef: TagRef, val value: TagKey): TagCmd
    class TagGlobalDelete(val tagRef: TagRef): TagCmd

    class TagLocalScopeDelete(val scopeRef: TagScopeRef): TagCmd
}
