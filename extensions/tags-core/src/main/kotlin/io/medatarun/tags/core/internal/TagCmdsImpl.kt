package io.medatarun.tags.core.internal

import io.medatarun.tags.core.domain.TagFreeRef
import io.medatarun.tags.core.domain.TagManagedRef
import io.medatarun.tags.core.domain.TagGroupRef
import io.medatarun.tags.core.domain.TagCmd
import io.medatarun.tags.core.domain.TagCmds
import io.medatarun.tags.core.domain.TagFree
import io.medatarun.tags.core.domain.TagFreeDuplicateKeyException
import io.medatarun.tags.core.domain.TagFreeId
import io.medatarun.tags.core.domain.TagFreeNotFoundException
import io.medatarun.tags.core.domain.TagGroup
import io.medatarun.tags.core.domain.TagGroupDuplicateKeyException
import io.medatarun.tags.core.domain.TagGroupId
import io.medatarun.tags.core.domain.TagGroupNotFoundException
import io.medatarun.tags.core.domain.TagManaged
import io.medatarun.tags.core.domain.TagManagedDuplicateKeyException
import io.medatarun.tags.core.domain.TagManagedId
import io.medatarun.tags.core.domain.TagManagedNotFoundException
import io.medatarun.tags.core.ports.needs.TagRepoCmd
import io.medatarun.tags.core.ports.needs.TagStorage

class TagCmdsImpl(private val storage: TagStorage) : TagCmds {
    override fun dispatch(cmd: TagCmd) {
        return when (cmd) {
            is TagCmd.TagFreeCreate -> tagFreeCreate(cmd)
            is TagCmd.TagFreeDelete -> tagFreeDelete(cmd)
            is TagCmd.TagFreeUpdateDescription -> tagFreeUpdateDescription(cmd)
            is TagCmd.TagFreeUpdateKey -> tagFreeUpdateKey(cmd)
            is TagCmd.TagFreeUpdateName -> tagFreeUpdateName(cmd)

            is TagCmd.TagGroupCreate -> tagGroupCreate(cmd)
            is TagCmd.TagGroupDelete -> tagGroupDelete(cmd)
            is TagCmd.TagGroupUpdateDescription -> tagGroupUpdateDescription(cmd)
            is TagCmd.TagGroupUpdateKey -> tagGroupUpdateKey(cmd)
            is TagCmd.TagGroupUpdateName -> tagGroupUpdateName(cmd)

            is TagCmd.TagManagedCreate -> tagManagedCreate(cmd)
            is TagCmd.TagManagedDelete -> tagManagedDelete(cmd)
            is TagCmd.TagManagedUpdateDescription -> tagManagedUpdateDescription(cmd)
            is TagCmd.TagManagedUpdateKey -> tagManagedUpdateKey(cmd)
            is TagCmd.TagManagedUpdateName -> tagManagedUpdateName(cmd)
        }
    }

    private fun findTagFreeOptional(ref: TagFreeRef): TagFree? {
        return when (ref) {
            is TagFreeRef.ById -> storage.findTagFreeByIdOptional(ref.id)
            is TagFreeRef.ByKey -> storage.findTagFreeByKeyOptional(ref.key)
        }
    }

    private fun findTagFree(ref: TagFreeRef): TagFree {
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

    private fun findTagManagedOptional(groupRef: TagGroupRef, tagRef: TagManagedRef): TagManaged? {
        val group = findTagGroupOptional(groupRef) ?: return null
        return when (tagRef) {
            is TagManagedRef.ById -> {
                val existing = storage.findTagManagedByIdOptional(tagRef.id) ?: return null
                if (existing.groupId != group.id) return null
                existing
            }
            is TagManagedRef.ByKey -> storage.findTagManagedByKeyOptional(group.id, tagRef.key)
        }
    }

    private fun findTagManaged(groupRef: TagGroupRef, tagRef: TagManagedRef): TagManaged {
        return findTagManagedOptional(groupRef, tagRef)
            ?: throw TagManagedNotFoundException(groupRef.asString(), tagRef.asString())

    }

    private fun tagFreeCreate(cmd: TagCmd.TagFreeCreate) {
        val existing = storage.findTagFreeByKeyOptional(cmd.key)
        if (existing != null) throw TagFreeDuplicateKeyException()
        storage.dispatch(
            TagRepoCmd.TagFreeCreate(
                TagFreeInMemory(
                    id = TagFreeId.generate(),
                    key = cmd.key,
                    name = cmd.name,
                    description = cmd.description
                )
            )
        )
    }

    private fun tagFreeUpdateKey(cmd: TagCmd.TagFreeUpdateKey) {
        val existing = findTagFree(cmd.ref)
        val duplicate = storage.findTagFreeByKeyOptional(cmd.value)
        if (duplicate != null && duplicate.id != existing.id) throw TagFreeDuplicateKeyException()
        storage.dispatch(TagRepoCmd.TagFreeUpdateKey(existing.id, cmd.value))
    }

    private fun tagFreeUpdateName(cmd: TagCmd.TagFreeUpdateName) {
        val existing = findTagFree(cmd.ref)
        storage.dispatch(TagRepoCmd.TagFreeUpdateName(existing.id, cmd.value))
    }

    private fun tagFreeUpdateDescription(cmd: TagCmd.TagFreeUpdateDescription) {
        val existing = findTagFree(cmd.ref)
        storage.dispatch(TagRepoCmd.TagFreeUpdateDescription(existing.id, cmd.value))
    }

    private fun tagFreeDelete(cmd: TagCmd.TagFreeDelete) {
        val existing = findTagFree(cmd.ref)
        storage.dispatch(TagRepoCmd.TagFreeDelete(existing.id))
    }


    private fun tagGroupCreate(cmd: TagCmd.TagGroupCreate) {
        val existing = storage.findTagGroupByKeyOptional(cmd.key)
        if (existing != null) throw TagGroupDuplicateKeyException()
        storage.dispatch(
            TagRepoCmd.TagGroupCreate(
                TagGroupInMemory(
                    id = TagGroupId.generate(),
                    key = cmd.key,
                    name = cmd.name,
                    description = cmd.description
                )
            )
        )
    }

    private fun tagGroupUpdateName(cmd: TagCmd.TagGroupUpdateName) {
        val existing = findTagGroup(cmd.ref)
        storage.dispatch(TagRepoCmd.TagGroupUpdateName(existing.id, cmd.value))

    }

    private fun tagGroupUpdateKey(cmd: TagCmd.TagGroupUpdateKey) {
        val existing = findTagGroup(cmd.ref)
        val duplicate = storage.findTagGroupByKeyOptional(cmd.value)
        if (duplicate != null && duplicate.id != existing.id) throw TagGroupDuplicateKeyException()
        storage.dispatch(TagRepoCmd.TagGroupUpdateKey(existing.id, cmd.value))
    }

    private fun tagGroupUpdateDescription(cmd: TagCmd.TagGroupUpdateDescription) {
        val existing = findTagGroup(cmd.ref)
        storage.dispatch(TagRepoCmd.TagGroupUpdateDescription(existing.id, cmd.value))
    }

    private fun tagGroupDelete(cmd: TagCmd.TagGroupDelete) {
        val existing = findTagGroup(cmd.ref)
        storage.dispatch(TagRepoCmd.TagGroupDelete(existing.id))
    }


    private fun tagManagedCreate(cmd: TagCmd.TagManagedCreate) {
        val group = findTagGroup(cmd.groupRef)

        val existing = storage.findTagManagedByKeyOptional(group.id, cmd.key)
        if (existing != null) throw TagManagedDuplicateKeyException()

        storage.dispatch(
            TagRepoCmd.TagManagedCreate(
                TagManagedInMemory(
                    id = TagManagedId.generate(),
                    key = cmd.key,
                    name = cmd.name,
                    description = cmd.description,
                    groupId = group.id
                )
            )
        )
    }

    private fun tagManagedUpdateKey(cmd: TagCmd.TagManagedUpdateKey) {
        val existing = findTagManaged(cmd.groupRef, cmd.tagRef)
        val duplicate = storage.findTagManagedByKeyOptional(existing.groupId, cmd.value)
        if (duplicate != null && duplicate.id != existing.id) throw TagManagedDuplicateKeyException()
        storage.dispatch(TagRepoCmd.TagManagedUpdateKey(existing.id, cmd.value))

    }

    private fun tagManagedUpdateName(cmd: TagCmd.TagManagedUpdateName) {
        val existing = findTagManaged(cmd.groupRef, cmd.tagRef)
        storage.dispatch(TagRepoCmd.TagManagedUpdateName(existing.id, cmd.value))
    }

    private fun tagManagedUpdateDescription(cmd: TagCmd.TagManagedUpdateDescription) {
        val existing = findTagManaged(cmd.groupRef, cmd.tagRef)
        storage.dispatch(TagRepoCmd.TagManagedUpdateDescription(existing.id, cmd.value))
    }

    private fun tagManagedDelete(cmd: TagCmd.TagManagedDelete) {
        val existing = findTagManaged(cmd.groupRef, cmd.tagRef)
        storage.dispatch(TagRepoCmd.TagManagedDelete(existing.id))
    }


}
