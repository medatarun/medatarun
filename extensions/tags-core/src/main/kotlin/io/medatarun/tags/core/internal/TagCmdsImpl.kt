package io.medatarun.tags.core.internal

import io.medatarun.platform.db.DbTransactionManager
import io.medatarun.tags.core.domain.*
import io.medatarun.tags.core.ports.needs.TagCmdsEvents
import io.medatarun.tags.core.ports.needs.TagStorageCmd
import io.medatarun.tags.core.ports.needs.TagStorageCmdEnveloppe
import io.medatarun.tags.core.ports.needs.TagStorage
import io.medatarun.type.commons.id.Id

class TagCmdsImpl(
    private val storage: TagStorage,
    private val tagScopes: TagScopes,
    private val evts: TagCmdsEvents,
    private val txManager: DbTransactionManager
) : TagCmds {
    private val tagRefResolver = TagRefResolver(storage)

    private fun storageCmdEnveloppe(cmdEnv: TagCmdEnveloppe, repoCmd: TagStorageCmd): TagStorageCmdEnveloppe {
        return TagStorageCmdEnveloppe(
            traceabilityRecord = cmdEnv.traceabilityRecord,
            cmd = repoCmd
        )
    }

    private fun storageDispatch(cmdEnv: TagCmdEnveloppe, repoCmd: TagStorageCmd) {
        storage.dispatch(storageCmdEnveloppe(cmdEnv, repoCmd))
    }

    override fun dispatch(cmdEnv: TagCmdEnveloppe) {
        return txManager.runInTransaction {
            when (val cmd = cmdEnv.cmd) {
                is TagCmd.TagLocalCreate -> tagLocalCreate(cmdEnv, cmd)
                is TagCmd.TagLocalDelete -> tagLocalDelete(cmdEnv, cmd)
                is TagCmd.TagLocalUpdateDescription -> tagLocalUpdateDescription(cmdEnv, cmd)
                is TagCmd.TagLocalUpdateKey -> tagLocalUpdateKey(cmdEnv, cmd)
                is TagCmd.TagLocalUpdateName -> tagLocalUpdateName(cmdEnv, cmd)

                is TagCmd.TagGroupCreate -> tagGroupCreate(cmdEnv, cmd)
                is TagCmd.TagGroupDelete -> tagGroupDelete(cmdEnv, cmd)
                is TagCmd.TagGroupUpdateDescription -> tagGroupUpdateDescription(cmdEnv, cmd)
                is TagCmd.TagGroupUpdateKey -> tagGroupUpdateKey(cmdEnv, cmd)
                is TagCmd.TagGroupUpdateName -> tagGroupUpdateName(cmdEnv, cmd)

                is TagCmd.TagGlobalCreate -> tagGlobalCreate(cmdEnv, cmd)
                is TagCmd.TagGlobalDelete -> tagGlobalDelete(cmdEnv, cmd)
                is TagCmd.TagGlobalUpdateDescription -> tagGlobalUpdateDescription(cmdEnv, cmd)
                is TagCmd.TagGlobalUpdateKey -> tagGlobalUpdateKey(cmdEnv, cmd)
                is TagCmd.TagGlobalUpdateName -> tagGlobalUpdateName(cmdEnv, cmd)

                is TagCmd.TagScopeDelete -> tagScopeDelete(cmdEnv, cmd)
            }
        }
    }

    private fun tagScopeDelete(cmdEnv: TagCmdEnveloppe, cmd: TagCmd.TagScopeDelete) {
        storage.findAllTag().filter { it.scope == cmd.scopeRef }.forEach {
            storageDispatch(cmdEnv, TagStorageCmd.TagDelete(it.id))
        }
    }

    private fun findTagLocalOptional(ref: TagRef): Tag? {
        if (ref is TagRef.ByKey && ref.scopeRef.isGlobal) {
            throw TagLocalCommandIncompatibleTagRefException(ref.asString())
        }
        val tag = findTagByRefOptional(ref) ?: return null
        if (tag.isGlobal) {
            throw TagLocalCommandIncompatibleTagRefException(ref.asString())
        }
        return tag
    }

    private fun findTagLocal(ref: TagRef): Tag {
        return findTagLocalOptional(ref) ?: throw TagLocalNotFoundException(ref.asString())
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

    private fun findTagGlobalOptional(tagRef: TagRef): Tag? {
        if (tagRef is TagRef.ByKey && tagRef.scopeRef.isLocal) {
            throw TagGlobalCommandIncompatibleTagRefException(tagRef.asString())
        }
        val tag = findTagByRefOptional(tagRef) ?: return null
        if (!tag.isGlobal) {
            throw TagGlobalCommandIncompatibleTagRefException(tagRef.asString())
        }
        return tag
    }

    private fun findTagGlobal(tagRef: TagRef): Tag {
        return findTagGlobalOptional(tagRef)
            ?: throw TagGlobalNotFoundException(tagRef.asString())
    }

    private fun tagLocalCreate(cmdEnv: TagCmdEnveloppe, cmd: TagCmd.TagLocalCreate) {
        if (cmd.scopeRef.isGlobal) throw TagLocalCommandIncompatibleTagScopeRefException(cmd.scopeRef.asString())
        val tagScope = cmd.scopeRef
        tagScopes.ensureLocalScopeExists(tagScope)
        val existing = storage.findTagByKeyOptional(tagScope, null, cmd.key)
        if (existing != null) throw TagLocalDuplicateKeyException()
        storageDispatch(
            cmdEnv,
            TagStorageCmd.TagCreate(
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

    private fun tagLocalUpdateKey(cmdEnv: TagCmdEnveloppe, cmd: TagCmd.TagLocalUpdateKey) {
        val existing = findTagLocal(cmd.ref)
        val duplicate = storage.findTagByKeyOptional(existing.scope, null, cmd.value)
        if (duplicate != null && duplicate.id != existing.id) throw TagLocalDuplicateKeyException()
        storageDispatch(cmdEnv, TagStorageCmd.TagUpdateKey(existing.id, cmd.value))
    }

    private fun tagLocalUpdateName(cmdEnv: TagCmdEnveloppe, cmd: TagCmd.TagLocalUpdateName) {
        val existing = findTagLocal(cmd.ref)
        storageDispatch(cmdEnv, TagStorageCmd.TagUpdateName(existing.id, cmd.value))
    }

    private fun tagLocalUpdateDescription(cmdEnv: TagCmdEnveloppe, cmd: TagCmd.TagLocalUpdateDescription) {
        val existing = findTagLocal(cmd.ref)
        storageDispatch(cmdEnv, TagStorageCmd.TagUpdateDescription(existing.id, cmd.value))
    }

    private fun tagLocalDelete(cmdEnv: TagCmdEnveloppe, cmd: TagCmd.TagLocalDelete) {
        val existing = findTagLocal(cmd.ref)
        evts.onBeforeDelete(cmdEnv.traceabilityRecord, existing.id)
        storageDispatch(cmdEnv, TagStorageCmd.TagDelete(existing.id))
    }


    private fun tagGroupCreate(cmdEnv: TagCmdEnveloppe, cmd: TagCmd.TagGroupCreate) {
        val existing = storage.findTagGroupByKeyOptional(cmd.key)
        if (existing != null) throw TagGroupDuplicateKeyException()
        storageDispatch(
            cmdEnv,
            TagStorageCmd.TagGroupCreate(
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
        storageDispatch(cmdEnv, TagStorageCmd.TagGroupUpdateName(existing.id, cmd.value))

    }

    private fun tagGroupUpdateKey(cmdEnv: TagCmdEnveloppe, cmd: TagCmd.TagGroupUpdateKey) {
        val existing = findTagGroup(cmd.ref)
        val duplicate = storage.findTagGroupByKeyOptional(cmd.value)
        if (duplicate != null && duplicate.id != existing.id) throw TagGroupDuplicateKeyException()
        storageDispatch(cmdEnv, TagStorageCmd.TagGroupUpdateKey(existing.id, cmd.value))
    }

    private fun tagGroupUpdateDescription(cmdEnv: TagCmdEnveloppe, cmd: TagCmd.TagGroupUpdateDescription) {
        val existing = findTagGroup(cmd.ref)
        storageDispatch(cmdEnv, TagStorageCmd.TagGroupUpdateDescription(existing.id, cmd.value))
    }

    private fun tagGroupDelete(cmdEnv: TagCmdEnveloppe, cmd: TagCmd.TagGroupDelete) {
        val existing = findTagGroup(cmd.ref)
        val groupTags = storage.findAllTag()
            .filter { it.groupId == existing.id }

        // Business rule: deleting a group deletes its tags and notifies cleanup listeners for each tag.
        // This behavior is implemented here and does not rely on SQL cascade support.
        groupTags.forEach {
            evts.onBeforeDelete(cmdEnv.traceabilityRecord, it.id)
            storageDispatch(cmdEnv, TagStorageCmd.TagDelete(it.id))
        }
        storageDispatch(cmdEnv, TagStorageCmd.TagGroupDelete(existing.id))
    }


    private fun tagGlobalCreate(cmdEnv: TagCmdEnveloppe, cmd: TagCmd.TagGlobalCreate) {
        val group = findTagGroup(cmd.groupRef)

        val existing = storage.findTagByKeyOptional(group.id, cmd.key)
        if (existing != null) throw TagGlobalDuplicateKeyException()

        storageDispatch(
            cmdEnv,
            TagStorageCmd.TagCreate(
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

    private fun tagGlobalUpdateKey(cmdEnv: TagCmdEnveloppe, cmd: TagCmd.TagGlobalUpdateKey) {
        val existing = findTagGlobal(cmd.tagRef)
        val existingGroupId =
            existing.groupId ?: throw TagGlobalCommandIncompatibleTagRefException(cmd.tagRef.asString())
        val duplicate = storage.findTagByKeyOptional(existingGroupId, cmd.value)
        if (duplicate != null && duplicate.id != existing.id) throw TagGlobalDuplicateKeyException()
        storageDispatch(cmdEnv, TagStorageCmd.TagUpdateKey(existing.id, cmd.value))

    }

    private fun tagGlobalUpdateName(cmdEnv: TagCmdEnveloppe, cmd: TagCmd.TagGlobalUpdateName) {
        val existing = findTagGlobal(cmd.tagRef)
        storageDispatch(cmdEnv, TagStorageCmd.TagUpdateName(existing.id, cmd.value))
    }

    private fun tagGlobalUpdateDescription(cmdEnv: TagCmdEnveloppe, cmd: TagCmd.TagGlobalUpdateDescription) {
        val existing = findTagGlobal(cmd.tagRef)
        storageDispatch(cmdEnv, TagStorageCmd.TagUpdateDescription(existing.id, cmd.value))
    }

    private fun tagGlobalDelete(cmdEnv: TagCmdEnveloppe, cmd: TagCmd.TagGlobalDelete) {
        val existing = findTagGlobal(cmd.tagRef)
        evts.onBeforeDelete(cmdEnv.traceabilityRecord, existing.id)
        storageDispatch(cmdEnv, TagStorageCmd.TagDelete(existing.id))
    }
}
