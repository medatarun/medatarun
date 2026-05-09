package io.medatarun.tags.core.domain

import io.medatarun.type.commons.text.TextMarkdown
import io.medatarun.type.commons.text.TextSingleLine

sealed interface TagCmd {

    class TagLocalCreate(val scopeRef: TagScopeRef, val key: TagKey, val name: TextSingleLine?, val description: TextMarkdown?): TagCmd
    class TagLocalUpdateName(val ref: TagRef, val value: TextSingleLine?): TagCmd
    class TagLocalUpdateDescription(val ref: TagRef, val value: TextMarkdown?): TagCmd
    class TagLocalUpdateKey(val ref: TagRef, val value: TagKey): TagCmd
    class TagLocalDelete(val ref: TagRef): TagCmd

    class TagGroupCreate(val key: TagGroupKey, val name: TextSingleLine?, val description: TextMarkdown?): TagCmd
    class TagGroupUpdateName(val ref: TagGroupRef, val value: TextSingleLine?): TagCmd
    class TagGroupUpdateDescription(val ref: TagGroupRef, val value: TextMarkdown?): TagCmd
    class TagGroupUpdateKey(val ref: TagGroupRef, val value: TagGroupKey): TagCmd
    class TagGroupDelete(val ref: TagGroupRef): TagCmd

    class TagGlobalCreate(val groupRef: TagGroupRef, val key: TagKey, val name: TextSingleLine?, val description: TextMarkdown?): TagCmd
    class TagGlobalUpdateName(val tagRef: TagRef, val value: TextSingleLine): TagCmd
    class TagGlobalUpdateDescription(val tagRef: TagRef, val value: TextMarkdown?): TagCmd
    class TagGlobalUpdateKey(val tagRef: TagRef, val value: TagKey): TagCmd
    class TagGlobalDelete(val tagRef: TagRef): TagCmd

    class TagLocalScopeDelete(val scopeRef: TagScopeRef): TagCmd
}
