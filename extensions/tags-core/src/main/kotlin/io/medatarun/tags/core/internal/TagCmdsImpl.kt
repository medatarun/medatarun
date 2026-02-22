package io.medatarun.tags.core.internal

import io.medatarun.tags.core.domain.*
import io.medatarun.tags.core.ports.needs.TagRepoCmd
import io.medatarun.tags.core.ports.needs.TagStorage
import io.medatarun.type.commons.id.Id

class TagCmdsImpl(private val storage: TagStorage) : TagCmds {
    private val tagRefResolver = TagRefResolver(storage)

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

    private fun findTagFreeOptional(ref: TagRef): Tag? {
        if (ref is TagRef.ByKey && ref.scopeRef.isGlobal) {
            throw TagFreeCommandIncompatibleTagRefException(ref.asString())
        }
        val tag = findTagByRefOptional(ref) ?: return null
        if (tag.isManaged) {
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
        if (!tag.isManaged) {
            throw TagManagedCommandIncompatibleTagRefException(tagRef.asString())
        }
        return tag
    }

    private fun findTagManaged(tagRef: TagRef): Tag {
        return findTagManagedOptional(tagRef)
            ?: throw TagManagedNotFoundException(tagRef.asString())
    }

    private fun tagFreeCreate(cmd: TagCmd.TagFreeCreate) {
        if (cmd.scopeRef.isGlobal) throw TagFreeCommandIncompatibleTagScopeRefException(cmd.scopeRef.asString())
        val tagScope = cmd.scopeRef
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

    private fun tagFreeUpdateKey(cmd: TagCmd.TagFreeUpdateKey) {
        val existing = findTagFree(cmd.ref)
        val duplicate = storage.findTagByKeyOptional(existing.scope, null, cmd.value)
        if (duplicate != null && duplicate.id != existing.id) throw TagFreeDuplicateKeyException()
        storage.dispatch(TagRepoCmd.TagUpdateKey(existing.id, cmd.value))
    }

    private fun tagFreeUpdateName(cmd: TagCmd.TagFreeUpdateName) {
        val existing = findTagFree(cmd.ref)
        storage.dispatch(TagRepoCmd.TagUpdateName(existing.id, cmd.value))
    }

    private fun tagFreeUpdateDescription(cmd: TagCmd.TagFreeUpdateDescription) {
        val existing = findTagFree(cmd.ref)
        storage.dispatch(TagRepoCmd.TagUpdateDescription(existing.id, cmd.value))
    }

    private fun tagFreeDelete(cmd: TagCmd.TagFreeDelete) {
        val existing = findTagFree(cmd.ref)
        storage.dispatch(TagRepoCmd.TagDelete(existing.id))
    }


    private fun tagGroupCreate(cmd: TagCmd.TagGroupCreate) {
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

    private fun tagManagedUpdateKey(cmd: TagCmd.TagManagedUpdateKey) {
        val existing = findTagManaged(cmd.tagRef)
        val existingGroupId = existing.groupId ?: throw TagManagedCommandIncompatibleTagRefException(cmd.tagRef.asString())
        val duplicate = storage.findTagByKeyOptional(existingGroupId, cmd.value)
        if (duplicate != null && duplicate.id != existing.id) throw TagManagedDuplicateKeyException()
        storage.dispatch(TagRepoCmd.TagUpdateKey(existing.id, cmd.value))

    }

    private fun tagManagedUpdateName(cmd: TagCmd.TagManagedUpdateName) {
        val existing = findTagManaged(cmd.tagRef)
        storage.dispatch(TagRepoCmd.TagUpdateName(existing.id, cmd.value))
    }

    private fun tagManagedUpdateDescription(cmd: TagCmd.TagManagedUpdateDescription) {
        val existing = findTagManaged(cmd.tagRef)
        storage.dispatch(TagRepoCmd.TagUpdateDescription(existing.id, cmd.value))
    }

    private fun tagManagedDelete(cmd: TagCmd.TagManagedDelete) {
        val existing = findTagManaged(cmd.tagRef)
        storage.dispatch(TagRepoCmd.TagDelete(existing.id))
    }
}
