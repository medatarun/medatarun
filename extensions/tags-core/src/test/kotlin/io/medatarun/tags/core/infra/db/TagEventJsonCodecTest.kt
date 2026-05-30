package io.medatarun.tags.core.infra.db

import io.medatarun.storage.eventsourcing.testkit.StorageCmdJsonCodecTestBase
import io.medatarun.storage.eventsourcing.testkit.StorageCmdTestCase
import io.medatarun.tags.core.domain.*
import io.medatarun.tags.core.infra.db.events.TagEventSystem
import io.medatarun.tags.core.ports.needs.TagStorageCmd
import io.medatarun.type.commons.id.Id
import io.medatarun.type.commons.text.TextMarkdown
import io.medatarun.type.commons.text.TextSingleLine

class TagEventJsonCodecTest : StorageCmdJsonCodecTestBase<TagStorageCmd>(
    codec = sys.codec,
    registeredContracts = sys.registry.findAllDescriptor(),
) {
    companion object {
        private val sys = TagEventSystem()
    }

    override val storageCmdRootClass = TagStorageCmd::class

    override fun testCases(): List<StorageCmdTestCase<TagStorageCmd>> {
        val tagId = TagId.fromString("00000000-0000-0000-0000-000000000001")
        val tagGroupId = Id.fromString("00000000-0000-0000-0000-000000000002", ::TagGroupId)
        val localScope = TagScopeRef.Local(
            type = TagScopeType("model"),
            localScopeId = Id.fromString("00000000-0000-0000-0000-000000000003", ::TagScopeId)
        )

        return listOf(
            StorageCmdTestCase(
                eventType = "tag_created",
                eventVersion = 1,
                cmd = TagStorageCmd.TagCreate(
                    tagId = tagId,
                    scope = localScope,
                    groupId = tagGroupId,
                    key = TagKey("active"),
                    name = TextSingleLine("Active"),
                    description = TextMarkdown("Active tag")
                ),
                json = """{"tagId":"00000000-0000-0000-0000-000000000001","scope":{"type":"model","id":"00000000-0000-0000-0000-000000000003"},"groupId":"00000000-0000-0000-0000-000000000002","key":"active","name":"Active","description":"Active tag"}"""
            ),
            StorageCmdTestCase(
                eventType = "tag_key_updated",
                eventVersion = 1,
                cmd = TagStorageCmd.TagUpdateKey(
                    tagId = tagId,
                    scope = localScope,
                    key = TagKey("inactive")
                ),
                json = """{"tagId":"00000000-0000-0000-0000-000000000001","scope":{"type":"model","id":"00000000-0000-0000-0000-000000000003"},"key":"inactive"}"""
            ),
            StorageCmdTestCase(
                eventType = "tag_name_updated",
                eventVersion = 1,
                cmd = TagStorageCmd.TagUpdateName(
                    tagId = tagId,
                    scope = localScope,
                    name = TextSingleLine("Inactive")
                ),
                json = """{"tagId":"00000000-0000-0000-0000-000000000001","scope":{"type":"model","id":"00000000-0000-0000-0000-000000000003"},"name":"Inactive"}"""
            ),
            StorageCmdTestCase(
                eventType = "tag_description_updated",
                eventVersion = 1,
                cmd = TagStorageCmd.TagUpdateDescription(
                    tagId = tagId,
                    scope = localScope,
                    description = TextMarkdown("Inactive tag")
                ),
                json = """{"tagId":"00000000-0000-0000-0000-000000000001","scope":{"type":"model","id":"00000000-0000-0000-0000-000000000003"},"description":"Inactive tag"}"""
            ),
            StorageCmdTestCase(
                eventType = "tag_deleted",
                eventVersion = 1,
                cmd = TagStorageCmd.TagDelete(
                    tagId = tagId,
                    scope = localScope
                ),
                json = """{"tagId":"00000000-0000-0000-0000-000000000001","scope":{"type":"model","id":"00000000-0000-0000-0000-000000000003"}}"""
            ),
            StorageCmdTestCase(
                eventType = "tag_local_scope_deleted",
                eventVersion = 1,
                cmd = TagStorageCmd.TagLocalScopeDelete(
                    scope = localScope
                ),
                json = """{"scope":{"type":"model","id":"00000000-0000-0000-0000-000000000003"}}"""
            ),
            StorageCmdTestCase(
                eventType = "tag_group_created",
                eventVersion = 1,
                cmd = TagStorageCmd.TagGroupCreate(
                    tagGroupId = tagGroupId,
                    key = TagGroupKey("status"),
                    name = TextSingleLine("Status"),
                    description = TextMarkdown("Status group")
                ),
                json = """{"tagGroupId":"00000000-0000-0000-0000-000000000002","key":"status","name":"Status","description":"Status group"}"""
            ),
            StorageCmdTestCase(
                eventType = "tag_group_key_updated",
                eventVersion = 1,
                cmd = TagStorageCmd.TagGroupUpdateKey(
                    tagGroupId = tagGroupId,
                    key = TagGroupKey("category")
                ),
                json = """{"tagGroupId":"00000000-0000-0000-0000-000000000002","key":"category"}"""
            ),
            StorageCmdTestCase(
                eventType = "tag_group_name_updated",
                eventVersion = 1,
                cmd = TagStorageCmd.TagGroupUpdateName(
                    tagGroupId = tagGroupId,
                    name = TextSingleLine("Category")
                ),
                json = """{"tagGroupId":"00000000-0000-0000-0000-000000000002","name":"Category"}"""
            ),
            StorageCmdTestCase(
                eventType = "tag_group_description_updated",
                eventVersion = 1,
                cmd = TagStorageCmd.TagGroupUpdateDescription(
                    tagGroupId = tagGroupId,
                    description = TextMarkdown("Category group")
                ),
                json = """{"tagGroupId":"00000000-0000-0000-0000-000000000002","description":"Category group"}"""
            ),
            StorageCmdTestCase(
                eventType = "tag_group_deleted",
                eventVersion = 1,
                cmd = TagStorageCmd.TagGroupDelete(
                    tagGroupId = tagGroupId
                ),
                json = """{"tagGroupId":"00000000-0000-0000-0000-000000000002"}"""
            ),
        )
    }
}
