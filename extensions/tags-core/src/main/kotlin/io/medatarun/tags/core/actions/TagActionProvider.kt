package io.medatarun.tags.core.actions

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.adapters.ActionTraceabilityRecord
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.tags.core.domain.*
import kotlinx.serialization.json.*
import kotlin.reflect.KClass

class TagActionProvider(
    val tagCmds: TagCmds,
    val tagQueries: TagQueries
) : ActionProvider<TagAction> {
    override val actionGroupKey: String = ACTION_GROUP_KEY


    override fun findCommandClass(): KClass<TagAction> = TagAction::class

    override fun dispatch(
        action: TagAction,
        actionCtx: ActionCtx
    ): Any {
        val handler = TagActionHandler(tagCmds, tagQueries, actionCtx)

        val result = when (action) {

            is TagAction.TagGlobalCreate -> handler.tagManagedCreate(action)
            is TagAction.TagGlobalDelete -> handler.tagManagedDelete(action)
            is TagAction.TagGlobalUpdateKey -> handler.tagManagedUpdateKey(action)
            is TagAction.TagGlobalUpdateName -> handler.tagManagedUpdateName(action)
            is TagAction.TagGlobalUpdateDescription -> handler.tagManagedUpdateDescription(action)

            is TagAction.TagLocalCreate -> handler.tagFreeCreate(action)
            is TagAction.TagLocalDelete -> handler.tagFreeDelete(action)
            is TagAction.TagLocalUpdateKey -> handler.tagFreeUpdateKey(action)
            is TagAction.TagLocalUpdateName -> handler.tagFreeUpdateName(action)
            is TagAction.TagLocalUpdateDescription -> handler.tagFreeUpdateDescription(action)

            is TagAction.TagGroupCreate -> handler.tagGroupCreate(action)
            is TagAction.TagGroupDelete -> handler.tagGroupDelete(action)
            is TagAction.TagGroupUpdateKey -> handler.tagGroupUpdateKey(action)
            is TagAction.TagGroupUpdateName -> handler.tagGroupUpdateName(action)
            is TagAction.TagGroupUpdateDescription -> handler.tagGroupUpdateDescription(action)

            is TagAction.TagSearch -> handler.tagSearch(action)
            is TagAction.TagGroupList -> handler.tagGroupList(action)
        }
        return result
    }

    companion object {
        const val ACTION_GROUP_KEY = "tag"
    }
}

class TagActionHandler(
    private val tagCmds: TagCmds,
    private val tagQueries: TagQueries,
    private val actionCtx: ActionCtx
) {

    fun dispatch(businessCmd: TagCmd) {
        val principal = actionCtx.principal.principal ?: throw TagActionNotAuthenticatedException()
        tagCmds.dispatch(
            TagCmdEnveloppe(
                traceabilityRecord = ActionTraceabilityRecord(actionCtx.actionInstanceId, principal.id),
                cmd = businessCmd
            )
        )
    }


    fun tagGroupCreate(cmd: TagAction.TagGroupCreate) {
        dispatch(TagCmd.TagGroupCreate(cmd.key, cmd.name, cmd.description))
    }

    fun tagGroupUpdateDescription(cmd: TagAction.TagGroupUpdateDescription) {
        dispatch(TagCmd.TagGroupUpdateDescription(cmd.tagGroupRef, cmd.value))
    }

    fun tagGroupUpdateKey(cmd: TagAction.TagGroupUpdateKey) {
        dispatch(TagCmd.TagGroupUpdateKey(cmd.tagGroupRef, cmd.value))
    }

    fun tagGroupUpdateName(cmd: TagAction.TagGroupUpdateName) {
        dispatch(TagCmd.TagGroupUpdateName(cmd.tagGroupRef, cmd.value))
    }


    fun tagGroupDelete(cmd: TagAction.TagGroupDelete) {
        dispatch(TagCmd.TagGroupDelete(cmd.tagGroupRef))
    }


    fun tagManagedCreate(cmd: TagAction.TagGlobalCreate) {
        dispatch(TagCmd.TagManagedCreate(cmd.groupRef, cmd.key, cmd.name, cmd.description))
    }

    fun tagManagedUpdateDescription(cmd: TagAction.TagGlobalUpdateDescription) {
        dispatch(TagCmd.TagManagedUpdateDescription(cmd.tagRef, cmd.value))
    }

    fun tagManagedUpdateKey(cmd: TagAction.TagGlobalUpdateKey) {
        dispatch(TagCmd.TagManagedUpdateKey(cmd.tagRef, cmd.value))
    }

    fun tagManagedUpdateName(cmd: TagAction.TagGlobalUpdateName) {
        dispatch(TagCmd.TagManagedUpdateName(cmd.tagRef, cmd.value))
    }

    fun tagManagedDelete(cmd: TagAction.TagGlobalDelete) {
        dispatch(TagCmd.TagManagedDelete(cmd.tagRef))
    }


    fun tagFreeCreate(cmd: TagAction.TagLocalCreate) {
        dispatch(TagCmd.TagFreeCreate(cmd.scopeRef, cmd.key, cmd.name, cmd.description))
    }

    fun tagFreeDelete(cmd: TagAction.TagLocalDelete) {
        dispatch(TagCmd.TagFreeDelete(cmd.tagRef))
    }

    fun tagFreeUpdateDescription(cmd: TagAction.TagLocalUpdateDescription) {
        dispatch(TagCmd.TagFreeUpdateDescription(cmd.tagRef, cmd.value))
    }

    fun tagFreeUpdateKey(cmd: TagAction.TagLocalUpdateKey) {
        dispatch(TagCmd.TagFreeUpdateKey(cmd.tagRef, cmd.value))
    }

    fun tagFreeUpdateName(cmd: TagAction.TagLocalUpdateName) {
        dispatch(TagCmd.TagFreeUpdateName(cmd.tagRef, cmd.value))
    }

    fun tagSearch(cmd: TagAction.TagSearch): JsonObject {
        val filters = cmd.filters ?: TagSearchFilters(
            operator = TagSearchFiltersLogicalOperator.AND,
            items = emptyList()
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
