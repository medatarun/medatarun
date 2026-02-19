package io.medatarun.tags.core.actions

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.actions.ports.needs.getService
import io.medatarun.tags.core.domain.TagCmd
import io.medatarun.tags.core.domain.TagCmds
import kotlin.reflect.KClass

class TagActionProvider() : ActionProvider<TagAction> {
    override val actionGroupKey: String = "tag"


    override fun findCommandClass(): KClass<TagAction> = TagAction::class

    override fun dispatch(
        cmd: TagAction,
        actionCtx: ActionCtx
    ): Any {
        val tagCmds = actionCtx.getService<TagCmds>()
        val handler = TagActionHander(tagCmds)

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
        }
        return result
    }
}

class TagActionHander(private val tagCmds: TagCmds) {


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
        tagCmds.dispatch(TagCmd.TagManagedUpdateDescription(cmd.groupRef, cmd.tagRef, cmd.value))
    }

    fun tagManagedUpdateKey(cmd: TagAction.TagManagedUpdateKey) {
        tagCmds.dispatch(TagCmd.TagManagedUpdateKey(cmd.groupRef, cmd.tagRef, cmd.value))
    }

    fun tagManagedUpdateName(cmd: TagAction.TagManagedUpdateName) {
        tagCmds.dispatch(TagCmd.TagManagedUpdateName(cmd.groupRef, cmd.tagRef, cmd.value))
    }

    fun tagManagedDelete(cmd: TagAction.TagManagedDelete) {
        tagCmds.dispatch(TagCmd.TagManagedDelete(cmd.groupRef, cmd.tagRef))
    }


    fun tagFreeCreate(cmd: TagAction.TagFreeCreate) {
        tagCmds.dispatch(TagCmd.TagFreeCreate(cmd.key, cmd.name, cmd.description))
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
        tagCmds.dispatch(TagCmd.TagFreeUpdateDescription(cmd.tagRef, cmd.value))
    }

}