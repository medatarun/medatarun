package io.medatarun.tags.core.actions

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.actions.ports.needs.getService
import io.medatarun.tags.core.domain.Tag
import io.medatarun.tags.core.domain.TagCmd
import io.medatarun.tags.core.domain.TagCmds
import io.medatarun.tags.core.domain.TagQueries
import io.medatarun.tags.core.domain.TagSearchFilters
import io.medatarun.tags.core.domain.TagSearchFiltersLogicalOperator
import io.medatarun.tags.core.domain.TagScopeRef
import kotlinx.serialization.json.*
import kotlin.reflect.KClass

class TagActionProvider : ActionProvider<TagAction> {
    override val actionGroupKey: String = "tag"


    override fun findCommandClass(): KClass<TagAction> = TagAction::class

    override fun dispatch(
        cmd: TagAction,
        actionCtx: ActionCtx
    ): Any {
        val tagCmds = actionCtx.getService<TagCmds>()
        val tagQueries = actionCtx.getService<TagQueries>()
        val handler = TagActionHandler(tagCmds, tagQueries)

        val result = when (cmd) {

            is TagAction.TagManagedCreate -> handler.tagManagedCreate(cmd)
            is TagAction.TagManagedDelete -> handler.tagManagedDelete(cmd)
            is TagAction.TagManagedUpdateKey -> handler.tagManagedUpdateKey(cmd)
            is TagAction.TagManagedUpdateName -> handler.tagManagedUpdateName(cmd)
            is TagAction.TagManagedUpdateDescription -> handler.tagManagedUpdateDescription(cmd)

            is TagAction.TagFreeCreate -> handler.tagFreeCreate(cmd)
            is TagAction.TagFreeDelete -> handler.tagFreeDelete(cmd)
            is TagAction.TagFreeUpdateKey -> handler.tagFreeUpdateKey(cmd)
            is TagAction.TagFreeUpdateName -> handler.tagFreeUpdateName(cmd)
            is TagAction.TagFreeUpdateDescription -> handler.tagFreeUpdateDescription(cmd)

            is TagAction.TagGroupCreate -> handler.tagGroupCreate(cmd)
            is TagAction.TagGroupDelete -> handler.tagGroupDelete(cmd)
            is TagAction.TagGroupUpdateKey -> handler.tagGroupUpdateKey(cmd)
            is TagAction.TagGroupUpdateName -> handler.tagGroupUpdateName(cmd)
            is TagAction.TagGroupUpdateDescription -> handler.tagGroupUpdateDescription(cmd)

            is TagAction.TagSearch -> handler.tagSearch(cmd)
            is TagAction.TagGroupList -> handler.tagGroupList(cmd)
        }
        return result
    }
}

class TagActionHandler(private val tagCmds: TagCmds, private val tagQueries: TagQueries) {


    fun tagGroupCreate(cmd: TagAction.TagGroupCreate) {
        tagCmds.dispatch(TagCmd.TagGroupCreate(cmd.key, cmd.name, cmd.description))
    }

    fun tagGroupUpdateDescription(cmd: TagAction.TagGroupUpdateDescription) {
        tagCmds.dispatch(TagCmd.TagGroupUpdateDescription(cmd.tagGroupRef, cmd.value))
    }

    fun tagGroupUpdateKey(cmd: TagAction.TagGroupUpdateKey) {
        tagCmds.dispatch(TagCmd.TagGroupUpdateKey(cmd.tagGroupRef, cmd.value))
    }

    fun tagGroupUpdateName(cmd: TagAction.TagGroupUpdateName) {
        tagCmds.dispatch(TagCmd.TagGroupUpdateName(cmd.tagGroupRef, cmd.value))
    }


    fun tagGroupDelete(cmd: TagAction.TagGroupDelete) {
        tagCmds.dispatch(TagCmd.TagGroupDelete(cmd.tagGroupRef))
    }


    fun tagManagedCreate(cmd: TagAction.TagManagedCreate) {
        tagCmds.dispatch(TagCmd.TagManagedCreate(cmd.groupRef, cmd.key, cmd.name, cmd.description))
    }

    fun tagManagedUpdateDescription(cmd: TagAction.TagManagedUpdateDescription) {
        tagCmds.dispatch(TagCmd.TagManagedUpdateDescription(cmd.tagRef, cmd.value))
    }

    fun tagManagedUpdateKey(cmd: TagAction.TagManagedUpdateKey) {
        tagCmds.dispatch(TagCmd.TagManagedUpdateKey(cmd.tagRef, cmd.value))
    }

    fun tagManagedUpdateName(cmd: TagAction.TagManagedUpdateName) {
        tagCmds.dispatch(TagCmd.TagManagedUpdateName(cmd.tagRef, cmd.value))
    }

    fun tagManagedDelete(cmd: TagAction.TagManagedDelete) {
        tagCmds.dispatch(TagCmd.TagManagedDelete(cmd.tagRef))
    }


    fun tagFreeCreate(cmd: TagAction.TagFreeCreate) {
        tagCmds.dispatch(TagCmd.TagFreeCreate(cmd.scopeRef, cmd.key, cmd.name, cmd.description))
    }

    fun tagFreeDelete(cmd: TagAction.TagFreeDelete) {
        tagCmds.dispatch(TagCmd.TagFreeDelete(cmd.tagRef))
    }

    fun tagFreeUpdateDescription(cmd: TagAction.TagFreeUpdateDescription) {
        tagCmds.dispatch(TagCmd.TagFreeUpdateDescription(cmd.tagRef, cmd.value))
    }

    fun tagFreeUpdateKey(cmd: TagAction.TagFreeUpdateKey) {
        tagCmds.dispatch(TagCmd.TagFreeUpdateKey(cmd.tagRef, cmd.value))
    }

    fun tagFreeUpdateName(cmd: TagAction.TagFreeUpdateName) {
        tagCmds.dispatch(TagCmd.TagFreeUpdateName(cmd.tagRef, cmd.value))
    }

    fun tagSearch(cmd: TagAction.TagSearch): JsonObject {
        val filters = cmd.filters ?: TagSearchFilters(
            operator = TagSearchFiltersLogicalOperator.AND,
            filters = emptyList()
        )
        val items = tagQueries.search(filters)
        return buildJsonObject {
            putJsonArray("items") {
                items.sortedWith(
                    compareBy<Tag> { it.groupId != null }
                        .thenBy { it.key.value }
                ).forEach {
                    val groupId = it.groupId
                    addJsonObject {
                        put("id", it.id.asString())
                        put("key", it.key.asString())
                        if (groupId == null) {
                            put("groupId", JsonNull)
                        } else {
                            put("groupId", groupId.asString())
                        }
                        put("tagScopeRef", tagScopeRefJson(it.scope))
                        put("name", it.name)
                        put("description", it.description)
                    }
                }
            }
        }
    }

    private fun tagScopeRefJson(scopeRef: TagScopeRef): JsonObject {
        return when (scopeRef) {
            is TagScopeRef.Global -> buildJsonObject {
                put("type", scopeRef.type.value)
                put("id", JsonNull)
            }
            is TagScopeRef.Local -> buildJsonObject {
                put("type", scopeRef.type.value)
                put("id", scopeRef.localScopeId.asString())
            }
        }
    }


    fun tagGroupList(cmd: TagAction.TagGroupList): JsonObject {
        val items = tagQueries.findAllTagGroup()
        return buildJsonObject {
            putJsonArray("items") {
                items.forEach {
                    addJsonObject {
                        put("id", it.id.asString())
                        put("key", it.key.asString())
                        put("name", it.name)
                        put("description", it.description)
                    }
                }
            }
        }

    }
}
