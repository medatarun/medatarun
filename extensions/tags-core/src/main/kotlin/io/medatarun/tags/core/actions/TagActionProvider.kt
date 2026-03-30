package io.medatarun.tags.core.actions

import io.medatarun.actions.adapters.ActionTraceabilityRecord
import io.medatarun.actions.ports.needs.ActionCtx
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

            is TagAction.TagGlobalCreate -> handler.tagGlobalCreate(action)
            is TagAction.TagGlobalDelete -> handler.tagGlobalDelete(action)
            is TagAction.TagGlobalUpdateKey -> handler.tagGlobalUpdateKey(action)
            is TagAction.TagGlobalUpdateName -> handler.tagGlobalUpdateName(action)
            is TagAction.TagGlobalUpdateDescription -> handler.tagGlobalUpdateDescription(action)

            is TagAction.TagLocalCreate -> handler.tagLocalCreate(action)
            is TagAction.TagLocalDelete -> handler.tagLocalDelete(action)
            is TagAction.TagLocalUpdateKey -> handler.tagLocalUpdateKey(action)
            is TagAction.TagLocalUpdateName -> handler.tagLocalUpdateName(action)
            is TagAction.TagLocalUpdateDescription -> handler.tagLocalUpdateDescription(action)

            is TagAction.TagGroupCreate -> handler.tagGroupCreate(action)
            is TagAction.TagGroupDelete -> handler.tagGroupDelete(action)
            is TagAction.TagGroupUpdateKey -> handler.tagGroupUpdateKey(action)
            is TagAction.TagGroupUpdateName -> handler.tagGroupUpdateName(action)
            is TagAction.TagGroupUpdateDescription -> handler.tagGroupUpdateDescription(action)

            is TagAction.TagSearch -> handler.tagSearch(action)
            is TagAction.TagGroupList -> handler.tagGroupList(action)
            is TagAction.MaintenanceRebuildCaches -> handler.maintenanceRebuildCaches()
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


    fun tagGlobalCreate(cmd: TagAction.TagGlobalCreate) {
        dispatch(TagCmd.TagGlobalCreate(cmd.groupRef, cmd.key, cmd.name, cmd.description))
    }

    fun tagGlobalUpdateDescription(cmd: TagAction.TagGlobalUpdateDescription) {
        dispatch(TagCmd.TagGlobalUpdateDescription(cmd.tagRef, cmd.value))
    }

    fun tagGlobalUpdateKey(cmd: TagAction.TagGlobalUpdateKey) {
        dispatch(TagCmd.TagGlobalUpdateKey(cmd.tagRef, cmd.value))
    }

    fun tagGlobalUpdateName(cmd: TagAction.TagGlobalUpdateName) {
        dispatch(TagCmd.TagGlobalUpdateName(cmd.tagRef, cmd.value))
    }

    fun tagGlobalDelete(cmd: TagAction.TagGlobalDelete) {
        dispatch(TagCmd.TagGlobalDelete(cmd.tagRef))
    }


    fun tagLocalCreate(cmd: TagAction.TagLocalCreate) {
        dispatch(TagCmd.TagLocalCreate(cmd.scopeRef, cmd.key, cmd.name, cmd.description))
    }

    fun tagLocalDelete(cmd: TagAction.TagLocalDelete) {
        dispatch(TagCmd.TagLocalDelete(cmd.tagRef))
    }

    fun tagLocalUpdateDescription(cmd: TagAction.TagLocalUpdateDescription) {
        dispatch(TagCmd.TagLocalUpdateDescription(cmd.tagRef, cmd.value))
    }

    fun tagLocalUpdateKey(cmd: TagAction.TagLocalUpdateKey) {
        dispatch(TagCmd.TagLocalUpdateKey(cmd.tagRef, cmd.value))
    }

    fun tagLocalUpdateName(cmd: TagAction.TagLocalUpdateName) {
        dispatch(TagCmd.TagLocalUpdateName(cmd.tagRef, cmd.value))
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

    fun maintenanceRebuildCaches() {
        tagCmds.maintenanceRebuildCaches()
    }
}
