package io.medatarun.tags.core.internal

import io.medatarun.platform.db.DbTransactionManager
import io.medatarun.tags.core.domain.*
import io.medatarun.tags.core.ports.needs.TagCmdsEvents
import io.medatarun.tags.core.ports.needs.TagRepoCmd
import io.medatarun.tags.core.ports.needs.TagStorage
import io.medatarun.type.commons.id.Id

class TagCmdsImpl(
    private val storage: TagStorage,
    private val tagScopes: TagScopes,
    private val evts: TagCmdsEvents,
    private val txManager: DbTransactionManager
) : TagCmds {
    private val tagRefResolver = TagRefResolver(storage)

    override fun dispatch(cmdEnv: TagCmdEnveloppe) {
        return txManager.runInTransaction {
            when (val cmd = cmdEnv.cmd) {
                is TagCmd.TagFreeCreate -> tagFreeCreate(cmdEnv, cmd)
                is TagCmd.TagFreeDelete -> tagFreeDelete(cmdEnv, cmd)
                is TagCmd.TagFreeUpdateDescription -> tagFreeUpdateDescription(cmdEnv, cmd)
                is TagCmd.TagFreeUpdateKey -> tagFreeUpdateKey(cmdEnv, cmd)
                is TagCmd.TagFreeUpdateName -> tagFreeUpdateName(cmdEnv, cmd)

                is TagCmd.TagGroupCreate -> tagGroupCreate(cmdEnv, cmd)
                is TagCmd.TagGroupDelete -> tagGroupDelete(cmdEnv, cmd)
                is TagCmd.TagGroupUpdateDescription -> tagGroupUpdateDescription(cmdEnv, cmd)
                is TagCmd.TagGroupUpdateKey -> tagGroupUpdateKey(cmdEnv, cmd)
                is TagCmd.TagGroupUpdateName -> tagGroupUpdateName(cmdEnv, cmd)

                is TagCmd.TagManagedCreate -> tagManagedCreate(cmdEnv, cmd)
                is TagCmd.TagManagedDelete -> tagManagedDelete(cmdEnv, cmd)
                is TagCmd.TagManagedUpdateDescription -> tagManagedUpdateDescription(cmdEnv, cmd)
                is TagCmd.TagManagedUpdateKey -> tagManagedUpdateKey(cmdEnv, cmd)
                is TagCmd.TagManagedUpdateName -> tagManagedUpdateName(cmdEnv, cmd)

                is TagCmd.TagScopeDelete -> tagScopeDelete(cmdEnv, cmd)
            }
        }
    }

    private fun tagScopeDelete(cmdEnv: TagCmdEnveloppe, cmd: TagCmd.TagScopeDelete) {
        storage.findAllTag().filter { it.scope == cmd.scopeRef }.forEach {
            storage.dispatch(TagRepoCmd.TagDelete(it.id))
        }
    }

    private fun findTagFreeOptional(ref: TagRef): Tag? {
        if (ref is TagRef.ByKey && ref.scopeRef.isGlobal) {
            throw TagFreeCommandIncompatibleTagRefException(ref.asString())
        }
        val tag = findTagByRefOptional(ref) ?: return null
        if (tag.isGlobal) {
            throw TagFreeCommandIncompatibleTagRefException(ref.asString())
        }
        return tag
    }

    private fun findTagFree(ref: TagRef): Tag {
        return findTagFreeOptional(ref) ?: throw TagFreeNotFoundException(ref.asString())
    }

    private fun findTagGroupOptional(ref: TagGroupRef): TagGroup? {
        return when (ref) {
            is TagGroupRef.ById -> storage.findTagGroupByIdOptional(ref.id)
            is TagGroupRef.ByKey -> storage.findTagGroupByKeyOptional(ref.key)
        }
    }

    private fun findTagGroup(ref: TagGroupRef): TagGroup {
        return findTagGroupOptional(ref) ?: throw TagGroupNotFoundException(ref.asString())
    }

    private fun findTagByRefOptional(tagRef: TagRef): Tag? {
        return tagRefResolver.findTagByRefOptional(tagRef)
    }

    private fun findTagManagedOptional(tagRef: TagRef): Tag? {
        if (tagRef is TagRef.ByKey && tagRef.scopeRef.isLocal) {
            throw TagManagedCommandIncompatibleTagRefException(tagRef.asString())
        }
        val tag = findTagByRefOptional(tagRef) ?: return null
        if (!tag.isGlobal) {
            throw TagManagedCommandIncompatibleTagRefException(tagRef.asString())
        }
        return tag
    }

    private fun findTagManaged(tagRef: TagRef): Tag {
        return findTagManagedOptional(tagRef)
            ?: throw TagManagedNotFoundException(tagRef.asString())
    }

    private fun tagFreeCreate(cmdEnv: TagCmdEnveloppe, cmd: TagCmd.TagFreeCreate) {
        if (cmd.scopeRef.isGlobal) throw TagFreeCommandIncompatibleTagScopeRefException(cmd.scopeRef.asString())
        val tagScope = cmd.scopeRef
        tagScopes.ensureLocalScopeExists(tagScope)
        val existing = storage.findTagByKeyOptional(tagScope, null, cmd.key)
        if (existing != null) throw TagFreeDuplicateKeyException()
        storage.dispatch(
            TagRepoCmd.TagCreate(
                TagInMemory(
                    id = Id.generate(::TagId),
                    scope = tagScope,
                    groupId = null,
                    key = cmd.key,
                    name = cmd.name,
                    description = cmd.description
                )
            )
        )
    }

    private fun tagFreeUpdateKey(cmdEnv: TagCmdEnveloppe, cmd: TagCmd.TagFreeUpdateKey) {
        val existing = findTagFree(cmd.ref)
        val duplicate = storage.findTagByKeyOptional(existing.scope, null, cmd.value)
        if (duplicate != null && duplicate.id != existing.id) throw TagFreeDuplicateKeyException()
        storage.dispatch(TagRepoCmd.TagUpdateKey(existing.id, cmd.value))
    }

    private fun tagFreeUpdateName(cmdEnv: TagCmdEnveloppe, cmd: TagCmd.TagFreeUpdateName) {
        val existing = findTagFree(cmd.ref)
        storage.dispatch(TagRepoCmd.TagUpdateName(existing.id, cmd.value))
    }

    private fun tagFreeUpdateDescription(cmdEnv: TagCmdEnveloppe, cmd: TagCmd.TagFreeUpdateDescription) {
        val existing = findTagFree(cmd.ref)
        storage.dispatch(TagRepoCmd.TagUpdateDescription(existing.id, cmd.value))
    }

    private fun tagFreeDelete(cmdEnv: TagCmdEnveloppe, cmd: TagCmd.TagFreeDelete) {
        val existing = findTagFree(cmd.ref)
        evts.onBeforeDelete(cmdEnv.traceabilityRecord, existing.id)
        storage.dispatch(TagRepoCmd.TagDelete(existing.id))
    }


    private fun tagGroupCreate(cmdEnv: TagCmdEnveloppe, cmd: TagCmd.TagGroupCreate) {
        val existing = storage.findTagGroupByKeyOptional(cmd.key)
        if (existing != null) throw TagGroupDuplicateKeyException()
        storage.dispatch(
            TagRepoCmd.TagGroupCreate(
                TagGroupInMemory(
                    id = Id.generate(::TagGroupId),
                    key = cmd.key,
                    name = cmd.name,
                    description = cmd.description
                )
            )
        )
    }

    private fun tagGroupUpdateName(cmdEnv: TagCmdEnveloppe, cmd: TagCmd.TagGroupUpdateName) {
        val existing = findTagGroup(cmd.ref)
        storage.dispatch(TagRepoCmd.TagGroupUpdateName(existing.id, cmd.value))

    }

    private fun tagGroupUpdateKey(cmdEnv: TagCmdEnveloppe, cmd: TagCmd.TagGroupUpdateKey) {
        val existing = findTagGroup(cmd.ref)
        val duplicate = storage.findTagGroupByKeyOptional(cmd.value)
        if (duplicate != null && duplicate.id != existing.id) throw TagGroupDuplicateKeyException()
        storage.dispatch(TagRepoCmd.TagGroupUpdateKey(existing.id, cmd.value))
    }

    private fun tagGroupUpdateDescription(cmdEnv: TagCmdEnveloppe, cmd: TagCmd.TagGroupUpdateDescription) {
        val existing = findTagGroup(cmd.ref)
        storage.dispatch(TagRepoCmd.TagGroupUpdateDescription(existing.id, cmd.value))
    }

    private fun tagGroupDelete(cmdEnv: TagCmdEnveloppe, cmd: TagCmd.TagGroupDelete) {
        val existing = findTagGroup(cmd.ref)
        val groupTags = storage.findAllTag()
            .filter { it.groupId == existing.id }

        // Business rule: deleting a group deletes its managed tags and notifies cleanup listeners for each tag.
        // This behavior is implemented here and does not rely on SQL cascade support.
        groupTags.forEach {
            evts.onBeforeDelete(cmdEnv.traceabilityRecord, it.id)
            storage.dispatch(TagRepoCmd.TagDelete(it.id))
        }
        storage.dispatch(TagRepoCmd.TagGroupDelete(existing.id))
    }


    private fun tagManagedCreate(cmdEnv: TagCmdEnveloppe, cmd: TagCmd.TagManagedCreate) {
        val group = findTagGroup(cmd.groupRef)

        val existing = storage.findTagByKeyOptional(group.id, cmd.key)
        if (existing != null) throw TagManagedDuplicateKeyException()

        storage.dispatch(
            TagRepoCmd.TagCreate(
                TagInMemory(
                    id = Id.generate(::TagId),
                    scope = TagScopeRef.Global,
                    key = cmd.key,
                    name = cmd.name,
                    description = cmd.description,
                    groupId = group.id
                )
            )
        )
    }

    private fun tagManagedUpdateKey(cmdEnv: TagCmdEnveloppe, cmd: TagCmd.TagManagedUpdateKey) {
        val existing = findTagManaged(cmd.tagRef)
        val existingGroupId =
            existing.groupId ?: throw TagManagedCommandIncompatibleTagRefException(cmd.tagRef.asString())
        val duplicate = storage.findTagByKeyOptional(existingGroupId, cmd.value)
        if (duplicate != null && duplicate.id != existing.id) throw TagManagedDuplicateKeyException()
        storage.dispatch(TagRepoCmd.TagUpdateKey(existing.id, cmd.value))

    }

    private fun tagManagedUpdateName(cmdEnv: TagCmdEnveloppe, cmd: TagCmd.TagManagedUpdateName) {
        val existing = findTagManaged(cmd.tagRef)
        storage.dispatch(TagRepoCmd.TagUpdateName(existing.id, cmd.value))
    }

    private fun tagManagedUpdateDescription(cmdEnv: TagCmdEnveloppe, cmd: TagCmd.TagManagedUpdateDescription) {
        val existing = findTagManaged(cmd.tagRef)
        storage.dispatch(TagRepoCmd.TagUpdateDescription(existing.id, cmd.value))
    }

    private fun tagManagedDelete(cmdEnv: TagCmdEnveloppe, cmd: TagCmd.TagManagedDelete) {
        val existing = findTagManaged(cmd.tagRef)
        evts.onBeforeDelete(cmdEnv.traceabilityRecord, existing.id)
        storage.dispatch(TagRepoCmd.TagDelete(existing.id))
    }
}
