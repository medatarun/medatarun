package io.medatarun.tags.core

import io.medatarun.tags.core.actions.TagAction
import io.medatarun.tags.core.domain.*
import io.medatarun.tags.core.domain.TagRef.Companion.tagRefId
import io.medatarun.tags.core.domain.TagRef.Companion.tagRefKey
import kotlin.test.*

class TagTest {
    private fun tagKey(value: TagKey): TagKey = value
    private fun tagRef(groupKey: TagGroupKey?, key: TagKey): TagRef = TagRef.ByKey(groupKey, key)
    private fun tagRef(id: TagId): TagRef = TagRef.ById(id)

    @Test
    fun `tag free created with name and description`() {
        val env = TagTestEnv()
        val key = TagKey("mykey")
        val key2 = TagKey("mykey2")
        env.dispatch(TagAction.TagFreeCreate(tagKey(key), "name", "description"))
        val found = env.tagQueries.findTagByRef(tagRefKey(null, TagKey(key.value)))
        assertEquals(TagKey(key.value), found.key)
        assertFalse(found.isManaged)
        assertNull(found.groupId)
        assertEquals("name", found.name)
        assertEquals("description", found.description)

        // create again
        env.dispatch(TagAction.TagFreeCreate(tagKey(key2), "name2", "description2"))
        val found2 = env.tagQueries.findTagByRef(tagRefKey(null, TagKey(key2.value)))
        assertEquals(TagKey(key2.value), found2.key)
        assertFalse(found2.isManaged)
        assertNull(found2.groupId)
        assertEquals("name2", found2.name)
        assertEquals("description2", found2.description)

        assertNotEquals(found.id, found2.id)

    }

    @Test
    fun `tag free created without name and description`() {
        val env = TagTestEnv()
        val key = TagKey("mykey")
        env.dispatch(TagAction.TagFreeCreate(tagKey(key), null, null))
        val found = env.tagQueries.findTagByRef(tagRefKey(null, TagKey(key.value)))
        assertEquals(TagKey(key.value), found.key)
        assertFalse(found.isManaged)
        assertNull(found.groupId)
        assertNull(found.name)
        assertNull(found.description)
    }

    @Test
    fun `tag free create with duplicate key then error`() {
        val env = TagTestEnv()
        val key = TagKey("mykey")
        env.dispatch(TagAction.TagFreeCreate(tagKey(key), null, null))
        assertFailsWith<TagFreeDuplicateKeyException> {
            env.dispatch(TagAction.TagFreeCreate(tagKey(key), null, null))
        }
    }

    @Test
    fun `tag free update name`() {
        // given 2 tags with names set to null
        val env = TagTestEnv()
        val key1 = TagKey("mykey1")
        val key2 = TagKey("mykey2")
        env.dispatch(TagAction.TagFreeCreate(tagKey(key1), null, null))
        env.dispatch(TagAction.TagFreeCreate(tagKey(key2), null, null))

        fun assertTagName(expected: String?, key: TagKey) {
            val found = env.tagQueries.findTagByRef(tagRefKey(null, TagKey(key.value)))
            assertEquals(expected, found.name)
            assertFalse(found.isManaged)
        }

        // Update tag1 name
        env.dispatch(TagAction.TagFreeUpdateName(tagRef(null, key1), "newname1"))

        // Then tag1 name shall be set and tag2 still null
        assertTagName("newname1", key1)
        assertTagName(null, key2)

        // Update tag2 name
        env.dispatch(TagAction.TagFreeUpdateName(tagRef(null, key2), "newname2"))

        // Then tag2 name shall be set and tag1 unmodified
        assertTagName("newname1", key1)
        assertTagName("newname2", key2)

        // Changes the now not null tag1 name
        env.dispatch(TagAction.TagFreeUpdateName(tagRef(null, key1), "newname1bis"))

        // Then tag1 name shall be set and tag2 unmodified
        assertTagName("newname1bis", key1)
        assertTagName("newname2", key2)

        // Changes the now not null tag2 name to null
        env.dispatch(TagAction.TagFreeUpdateName(tagRef(null, key2), null))

        // Then tag2 name shall be null and tag1 unmodified
        assertTagName("newname1bis", key1)
        assertTagName(null, key2)

    }

    @Test
    fun `free tag update description`() {
        // given 2 tags with descriptions set to null
        val env = TagTestEnv()
        val key1 = TagKey("mykey1")
        val key2 = TagKey("mykey2")
        env.dispatch(TagAction.TagFreeCreate(tagKey(key1), null, null))
        env.dispatch(TagAction.TagFreeCreate(tagKey(key2), null, null))

        fun assertTagDescription(expected: String?, key: TagKey) {
            val found = env.tagQueries.findTagByRef(tagRefKey(null, TagKey(key.value)))
            assertEquals(expected, found.description)
            assertFalse(found.isManaged)
        }

        // Update tag1 description
        env.dispatch(TagAction.TagFreeUpdateDescription(tagRef(null, key1), "newname1"))

        // Then tag1 description shall be set and tag2 still null
        assertTagDescription("newname1", key1)
        assertTagDescription(null, key2)

        // Update tag2 description
        env.dispatch(TagAction.TagFreeUpdateDescription(tagRef(null, key2), "newname2"))

        // Then tag2 description shall be set and tag1 unmodified
        assertTagDescription("newname1", key1)
        assertTagDescription("newname2", key2)

        // Changes the now not null tag1 description
        env.dispatch(TagAction.TagFreeUpdateDescription(tagRef(null, key1), "newname1bis"))

        // Then tag1 description shall be set and tag2 unmodified
        assertTagDescription("newname1bis", key1)
        assertTagDescription("newname2", key2)

        // Changes the now not null tag2 description to null
        env.dispatch(TagAction.TagFreeUpdateDescription(tagRef(null, key2), null))

        // Then tag2 description shall be null and tag1 unmodified
        assertTagDescription("newname1bis", key1)
        assertTagDescription(null, key2)
    }

    @Test
    fun `free tag update key`() {
        // given 2 tags with names set to null
        val env = TagTestEnv()
        val key1 = TagKey("mykey1")
        val key2 = TagKey("mykey2")
        env.dispatch(TagAction.TagFreeCreate(tagKey(key1), null, null))
        env.dispatch(TagAction.TagFreeCreate(tagKey(key2), null, null))

        val tag1Id = env.tagQueries.findTagByRef(tagRefKey(null, TagKey(key1.value))).id
        val tag2Id = env.tagQueries.findTagByRef(tagRefKey(null, TagKey(key2.value))).id

        fun assertTagKey(expected: String, id: TagId) {
            val found = env.tagQueries.findTagByRef(tagRefId(id))
            assertEquals(TagKey(expected), found.key)
            assertFalse(found.isManaged)
        }

        // Update tag1 key
        env.dispatch(TagAction.TagFreeUpdateKey(tagRef(TagId(tag1Id.value)), tagKey(TagKey("newkey1"))))

        // Cheks that tag1 key is changed and tag2 key unmodified
        assertTagKey("newkey1", tag1Id)
        assertTagKey("mykey2", tag2Id)

        // Update tag2 key to the same key as tag1 raises a duplicate exception
        assertFailsWith<TagFreeDuplicateKeyException> {
            env.dispatch(TagAction.TagFreeUpdateKey(tagRef(TagId(tag2Id.value)), tagKey(TagKey("newkey1"))))
        }

        // Update tag2 key to another key is ok
        env.dispatch(TagAction.TagFreeUpdateKey(tagRef(TagId(tag2Id.value)), tagKey(TagKey("newkey2"))))

        // Checks the new tag2 key
        assertTagKey("newkey1", tag1Id)
        assertTagKey("newkey2", tag2Id)
    }

    @Test
    fun `free tag delete`() {
        val env = TagTestEnv()
        val key = TagKey("mykey")
        val key2 = TagKey("mykey2")
        env.dispatch(TagAction.TagFreeCreate(tagKey(key), null, null))
        env.dispatch(TagAction.TagFreeCreate(tagKey(key2), null, null))
        env.dispatch(TagAction.TagFreeDelete(tagRef(null, key)))
        assertNull(env.tagQueries.findTagByRefOptional(tagRefKey(null, TagKey(key.value))))
        assertNotNull(env.tagQueries.findTagByRefOptional(tagRefKey(null, TagKey(key2.value))))
        env.dispatch(TagAction.TagFreeDelete(tagRef(null, key2)))
        assertNull(env.tagQueries.findTagByRefOptional(tagRefKey(null, TagKey(key2.value))))
    }

    // Tag groups

    @Test
    fun `tag group created with name and description`() {
        val env = TagTestEnv()
        val key = TagGroupKey("mykey")
        val key2 = TagGroupKey("mykey2")
        env.dispatch(TagAction.TagGroupCreate(key, "name", "description"))
        val found = env.tagStorage.findTagGroupByKeyOptional(key)
        assertNotNull(found)
        assertEquals(key, found.key)
        assertEquals("name", found.name)
        assertEquals("description", found.description)

        // create again
        env.dispatch(TagAction.TagGroupCreate(key2, "name2", "description2"))
        val found2 = env.tagStorage.findTagGroupByKeyOptional(key2)
        assertNotNull(found2)
        assertEquals(key2, found2.key)
        assertEquals("name2", found2.name)
        assertEquals("description2", found2.description)

        assertNotEquals(found.id, found2.id)
    }

    @Test
    fun `tag group created without name and description`() {
        val env = TagTestEnv()
        val key = TagGroupKey("mykey")
        env.dispatch(TagAction.TagGroupCreate(key, null, null))
        val found = env.tagStorage.findTagGroupByKeyOptional(key)
        assertNotNull(found)
        assertEquals(key, found.key)
        assertNull(found.name)
        assertNull(found.description)
    }

    @Test
    fun `tag group create with duplicate key then error`() {
        val env = TagTestEnv()
        val key = TagGroupKey("mykey")
        env.dispatch(TagAction.TagGroupCreate(key, null, null))
        assertFailsWith<TagGroupDuplicateKeyException> {
            env.dispatch(TagAction.TagGroupCreate(key, null, null))
        }
    }

    @Test
    fun `tag group update name`() {
        // given 2 groups with names set to null
        val env = TagTestEnv()
        val key1 = TagGroupKey("mykey1")
        val key2 = TagGroupKey("mykey2")
        env.dispatch(TagAction.TagGroupCreate(key1, null, null))
        env.dispatch(TagAction.TagGroupCreate(key2, null, null))

        fun assertTagGroupName(expected: String?, key: TagGroupKey) {
            val found = env.tagStorage.findTagGroupByKeyOptional(key)
            assertNotNull(found)
            assertEquals(expected, found.name)
        }

        // Update group1 name
        env.dispatch(TagAction.TagGroupUpdateName(TagGroupRef.ByKey(key1), "newname1"))

        // Then group1 name shall be set and group2 still null
        assertTagGroupName("newname1", key1)
        assertTagGroupName(null, key2)

        // Update group2 name
        env.dispatch(TagAction.TagGroupUpdateName(TagGroupRef.ByKey(key2), "newname2"))

        // Then group2 name shall be set and group1 unmodified
        assertTagGroupName("newname1", key1)
        assertTagGroupName("newname2", key2)

        // Changes group1 name
        env.dispatch(TagAction.TagGroupUpdateName(TagGroupRef.ByKey(key1), "newname1bis"))

        // Then group1 name shall be set and group2 unmodified
        assertTagGroupName("newname1bis", key1)
        assertTagGroupName("newname2", key2)
    }

    @Test
    fun `tag group update description`() {
        // given 2 groups with descriptions set to null
        val env = TagTestEnv()
        val key1 = TagGroupKey("mykey1")
        val key2 = TagGroupKey("mykey2")
        env.dispatch(TagAction.TagGroupCreate(key1, null, null))
        env.dispatch(TagAction.TagGroupCreate(key2, null, null))

        fun assertTagGroupDescription(expected: String?, key: TagGroupKey) {
            val found = env.tagStorage.findTagGroupByKeyOptional(key)
            assertNotNull(found)
            assertEquals(expected, found.description)
        }

        // Update group1 description
        env.dispatch(TagAction.TagGroupUpdateDescription(TagGroupRef.ByKey(key1), "newdescription1"))

        // Then group1 description shall be set and group2 still null
        assertTagGroupDescription("newdescription1", key1)
        assertTagGroupDescription(null, key2)

        // Update group2 description
        env.dispatch(TagAction.TagGroupUpdateDescription(TagGroupRef.ByKey(key2), "newdescription2"))

        // Then group2 description shall be set and group1 unmodified
        assertTagGroupDescription("newdescription1", key1)
        assertTagGroupDescription("newdescription2", key2)

        // Changes group1 description
        env.dispatch(TagAction.TagGroupUpdateDescription(TagGroupRef.ByKey(key1), "newdescription1bis"))

        // Then group1 description shall be set and group2 unmodified
        assertTagGroupDescription("newdescription1bis", key1)
        assertTagGroupDescription("newdescription2", key2)
    }

    @Test
    fun `tag group  update key`() {
        // given 2 groups with names set to null
        val env = TagTestEnv()
        val key1 = TagGroupKey("mykey1")
        val key2 = TagGroupKey("mykey2")
        env.dispatch(TagAction.TagGroupCreate(key1, null, null))
        env.dispatch(TagAction.TagGroupCreate(key2, null, null))

        val group1 = env.tagStorage.findTagGroupByKeyOptional(key1)
        assertNotNull(group1)
        val group2 = env.tagStorage.findTagGroupByKeyOptional(key2)
        assertNotNull(group2)
        val tagGroup1Id = group1.id
        val tagGroup2Id = group2.id

        fun assertTagGroupKey(expected: String, id: TagGroupId) {
            val found = env.tagStorage.findTagGroupByIdOptional(id)
            assertNotNull(found)
            assertEquals(TagGroupKey(expected), found.key)
        }

        // Update group1 key
        env.dispatch(TagAction.TagGroupUpdateKey(TagGroupRef.ById(tagGroup1Id), TagGroupKey("newkey1")))

        // Checks that group1 key is changed and group2 key unmodified
        assertTagGroupKey("newkey1", tagGroup1Id)
        assertTagGroupKey("mykey2", tagGroup2Id)

        // Update group2 key to the same key as group1 raises a duplicate exception
        assertFailsWith<TagGroupDuplicateKeyException> {
            env.dispatch(TagAction.TagGroupUpdateKey(TagGroupRef.ById(tagGroup2Id), TagGroupKey("newkey1")))
        }

        // Update group2 key to another key is ok
        env.dispatch(TagAction.TagGroupUpdateKey(TagGroupRef.ById(tagGroup2Id), TagGroupKey("newkey2")))

        // Checks the new group2 key
        assertTagGroupKey("newkey1", tagGroup1Id)
        assertTagGroupKey("newkey2", tagGroup2Id)
    }

    @Test
    fun `tag group delete`() {
        val env = TagTestEnv()
        val key1 = TagGroupKey("mykey1")
        val key2 = TagGroupKey("mykey2")
        env.dispatch(TagAction.TagGroupCreate(key1, null, null))
        env.dispatch(TagAction.TagGroupCreate(key2, null, null))
        env.dispatch(TagAction.TagGroupDelete(TagGroupRef.ByKey(key1)))
        assertNull(env.tagStorage.findTagGroupByKeyOptional(key1))
        assertNotNull(env.tagStorage.findTagGroupByKeyOptional(key2))
        env.dispatch(TagAction.TagGroupDelete(TagGroupRef.ByKey(key2)))
        assertNull(env.tagStorage.findTagGroupByKeyOptional(key2))
    }

    @Test
    fun `tag group delete deletes managed tags`() {
        val env = TagTestEnv()
        val groupKey1 = TagGroupKey("group-key1")
        val groupKey2 = TagGroupKey("group-key2")
        env.dispatch(TagAction.TagGroupCreate(groupKey1, null, null))
        env.dispatch(TagAction.TagGroupCreate(groupKey2, null, null))

        val group1 = env.tagStorage.findTagGroupByKeyOptional(groupKey1)
        assertNotNull(group1)
        val group2 = env.tagStorage.findTagGroupByKeyOptional(groupKey2)
        assertNotNull(group2)

        val managedKey1 = TagKey("managed-key1")
        val managedKey1b = TagKey("managed-key1b")
        val managedKey2 = TagKey("managed-key2")
        env.dispatch(TagAction.TagManagedCreate(TagGroupRef.ById(group1.id), tagKey(managedKey1), null, null))
        env.dispatch(TagAction.TagManagedCreate(TagGroupRef.ById(group1.id), tagKey(managedKey1b), null, null))
        env.dispatch(TagAction.TagManagedCreate(TagGroupRef.ById(group2.id), tagKey(managedKey2), null, null))
        val freeKey1 = TagKey("free-key1")
        val freeKey2 = TagKey("free-key2")
        env.dispatch(TagAction.TagFreeCreate(tagKey(freeKey1), null, null))
        env.dispatch(TagAction.TagFreeCreate(tagKey(freeKey2), null, null))

        // Business rule: deleting a group also deletes the managed tags that belong to it.
        env.dispatch(TagAction.TagGroupDelete(TagGroupRef.ById(group1.id)))

        // All managed tags of the deleted group must be removed.
        assertNull(env.tagStorage.findTagByKeyOptional(group1.id, TagKey(managedKey1.value)))
        assertNull(env.tagStorage.findTagByKeyOptional(group1.id, TagKey(managedKey1b.value)))
        // Managed tags in other groups must remain unchanged.
        assertNotNull(env.tagStorage.findTagByKeyOptional(group2.id, TagKey(managedKey2.value)))
        // Free tags (without group) must remain unchanged.
        assertNotNull(env.tagQueries.findTagByRefOptional(tagRefKey(null, TagKey(freeKey1.value))))
        assertNotNull(env.tagQueries.findTagByRefOptional(tagRefKey(null, TagKey(freeKey2.value))))
    }

    // Managed Tags

    @Test
    fun `tag managed created with name and description`() {
        val env = TagTestEnv()
        val groupKey = TagGroupKey("group-key")
        env.dispatch(TagAction.TagGroupCreate(groupKey, "group", "group-description"))

        val group = env.tagStorage.findTagGroupByKeyOptional(groupKey)
        assertNotNull(group)

        val managedKey = TagKey("managed-key")
        val managedKey2 = TagKey("managed-key2")
        env.dispatch(TagAction.TagManagedCreate(TagGroupRef.ById(group.id), tagKey(managedKey), "name", "description"))
        // This test also checks that a second managed tag can be created in the same group
        // (with a different key) and gets a distinct identifier.
        env.dispatch(TagAction.TagManagedCreate(TagGroupRef.ById(group.id), tagKey(managedKey2), "name2", "description2"))

        val found = env.tagStorage.findTagByKeyOptional(group.id, TagKey(managedKey.value))
        assertNotNull(found)
        assertEquals(group.id, found.groupId)
        assertEquals(TagKey(managedKey.value), found.key)
        assertEquals("name", found.name)
        assertEquals("description", found.description)

        val found2 = env.tagStorage.findTagByKeyOptional(group.id, TagKey(managedKey2.value))
        assertNotNull(found2)
        assertEquals(group.id, found2.groupId)
        assertEquals(TagKey(managedKey2.value), found2.key)
        assertEquals("name2", found2.name)
        assertEquals("description2", found2.description)
        assertNotEquals(found.id, found2.id)
    }

    @Test
    fun `tag managed created without name and description`() {
        val env = TagTestEnv()
        val groupKey = TagGroupKey("group-key")
        env.dispatch(TagAction.TagGroupCreate(groupKey, null, null))
        val group = env.tagStorage.findTagGroupByKeyOptional(groupKey)
        assertNotNull(group)

        val managedKey = TagKey("managed-key")
        env.dispatch(TagAction.TagManagedCreate(TagGroupRef.ById(group.id), tagKey(managedKey), null, null))

        val found = env.tagStorage.findTagByKeyOptional(group.id, TagKey(managedKey.value))
        assertNotNull(found)
        assertEquals(group.id, found.groupId)
        assertEquals(TagKey(managedKey.value), found.key)
        assertNull(found.name)
        assertNull(found.description)
    }

    @Test
    fun `tag managed create with duplicate key in same group then error`() {
        val env = TagTestEnv()
        val groupKey = TagGroupKey("group-key")
        env.dispatch(TagAction.TagGroupCreate(groupKey, null, null))
        val group = env.tagStorage.findTagGroupByKeyOptional(groupKey)
        assertNotNull(group)

        val managedKey = TagKey("managed-key")
        env.dispatch(TagAction.TagManagedCreate(TagGroupRef.ById(group.id), tagKey(managedKey), null, null))
        assertFailsWith<TagManagedDuplicateKeyException> {
            env.dispatch(TagAction.TagManagedCreate(TagGroupRef.ById(group.id), tagKey(managedKey), null, null))
        }
    }

    @Test
    fun `tag managed create with same key in different groups is allowed`() {
        val env = TagTestEnv()
        val groupKey1 = TagGroupKey("group-key1")
        val groupKey2 = TagGroupKey("group-key2")
        env.dispatch(TagAction.TagGroupCreate(groupKey1, null, null))
        env.dispatch(TagAction.TagGroupCreate(groupKey2, null, null))

        val group1 = env.tagStorage.findTagGroupByKeyOptional(groupKey1)
        assertNotNull(group1)
        val group2 = env.tagStorage.findTagGroupByKeyOptional(groupKey2)
        assertNotNull(group2)

        val managedKey = TagKey("shared-key")
        env.dispatch(TagAction.TagManagedCreate(TagGroupRef.ById(group1.id), tagKey(managedKey), "name-1", "description-1"))
        env.dispatch(TagAction.TagManagedCreate(TagGroupRef.ById(group2.id), tagKey(managedKey), "name-2", "description-2"))

        val found1 = env.tagStorage.findTagByKeyOptional(group1.id, TagKey(managedKey.value))
        assertNotNull(found1)
        val found2 = env.tagStorage.findTagByKeyOptional(group2.id, TagKey(managedKey.value))
        assertNotNull(found2)
        assertNotEquals(found1.id, found2.id)
        assertEquals(group1.id, found1.groupId)
        assertEquals(group2.id, found2.groupId)
    }

    @Test
    fun `tag managed create with unknown group then error`() {
        val env = TagTestEnv()

        // Why this test:
        // Creating a managed tag without an existing group should fail early with a group-not-found error.
        assertFailsWith<TagGroupNotFoundException> {
            env.dispatch(
                TagAction.TagManagedCreate(
                    TagGroupRef.ByKey(TagGroupKey("missing-group")),
                    tagKey(TagKey("managed-key")),
                    "name",
                    "description"
                )
            )
        }
    }
    @Test
    fun `tag managed update name`() {
        val env = TagTestEnv()
        val groupKey = TagGroupKey("group-key")
        env.dispatch(TagAction.TagGroupCreate(groupKey, null, null))
        val group = env.tagStorage.findTagGroupByKeyOptional(groupKey)
        assertNotNull(group)

        val managedKey1 = TagKey("managed-key1")
        val managedKey2 = TagKey("managed-key2")
        env.dispatch(TagAction.TagManagedCreate(TagGroupRef.ById(group.id), tagKey(managedKey1), "name-1", null))
        env.dispatch(TagAction.TagManagedCreate(TagGroupRef.ById(group.id), tagKey(managedKey2), "name-2", null))

        fun assertTagManagedName(expected: String, key: TagKey) {
            val found = env.tagStorage.findTagByKeyOptional(group.id, TagKey(key.value))
            assertNotNull(found)
            assertEquals(expected, found.name)
        }

        env.dispatch(
            TagAction.TagManagedUpdateName(tagRef(groupKey, managedKey1), "new-name-1"
            )
        )
        assertTagManagedName("new-name-1", managedKey1)
        assertTagManagedName("name-2", managedKey2)

        env.dispatch(
            TagAction.TagManagedUpdateName(tagRef(groupKey, managedKey2), "new-name-2"
            )
        )
        assertTagManagedName("new-name-1", managedKey1)
        assertTagManagedName("new-name-2", managedKey2)
    }

    @Test
    fun `tag managed update description`() {
        val env = TagTestEnv()
        val groupKey = TagGroupKey("group-key")
        env.dispatch(TagAction.TagGroupCreate(groupKey, null, null))
        val group = env.tagStorage.findTagGroupByKeyOptional(groupKey)
        assertNotNull(group)

        val managedKey1 = TagKey("managed-key1")
        val managedKey2 = TagKey("managed-key2")
        env.dispatch(TagAction.TagManagedCreate(TagGroupRef.ById(group.id), tagKey(managedKey1), null, "description-1"))
        env.dispatch(TagAction.TagManagedCreate(TagGroupRef.ById(group.id), tagKey(managedKey2), null, "description-2"))

        fun assertTagManagedDescription(expected: String, key: TagKey) {
            val found = env.tagStorage.findTagByKeyOptional(group.id, TagKey(key.value))
            assertNotNull(found)
            assertEquals(expected, found.description)
        }

        env.dispatch(
            TagAction.TagManagedUpdateDescription(tagRef(groupKey, managedKey1), "new-description-1"
            )
        )
        assertTagManagedDescription("new-description-1", managedKey1)
        assertTagManagedDescription("description-2", managedKey2)

        env.dispatch(
            TagAction.TagManagedUpdateDescription(tagRef(groupKey, managedKey2), "new-description-2"
            )
        )
        assertTagManagedDescription("new-description-1", managedKey1)
        assertTagManagedDescription("new-description-2", managedKey2)
    }

    @Test
    fun `tag managed update key with uniqueness relative to group`() {
        val env = TagTestEnv()
        val groupKey1 = TagGroupKey("group-key1")
        val groupKey2 = TagGroupKey("group-key2")
        env.dispatch(TagAction.TagGroupCreate(groupKey1, null, null))
        env.dispatch(TagAction.TagGroupCreate(groupKey2, null, null))

        val group1 = env.tagStorage.findTagGroupByKeyOptional(groupKey1)
        assertNotNull(group1)
        val group2 = env.tagStorage.findTagGroupByKeyOptional(groupKey2)
        assertNotNull(group2)

        val group1Key1 = TagKey("group1-key1")
        val group1Key2 = TagKey("group1-key2")
        val group2Key1 = TagKey("group2-key1")
        env.dispatch(TagAction.TagManagedCreate(TagGroupRef.ById(group1.id), tagKey(group1Key1), null, null))
        env.dispatch(TagAction.TagManagedCreate(TagGroupRef.ById(group1.id), tagKey(group1Key2), null, null))
        env.dispatch(TagAction.TagManagedCreate(TagGroupRef.ById(group2.id), tagKey(group2Key1), null, null))

        val group1Tag1 = env.tagStorage.findTagByKeyOptional(group1.id, TagKey(group1Key1.value))
        assertNotNull(group1Tag1)
        val group1Tag2 = env.tagStorage.findTagByKeyOptional(group1.id, TagKey(group1Key2.value))
        assertNotNull(group1Tag2)

        // Duplicate key inside the same group is forbidden.
        assertFailsWith<TagManagedDuplicateKeyException> {
            env.dispatch(
                TagAction.TagManagedUpdateKey(
                    tagRef(TagId(group1Tag2.id.value)),
                    tagKey(TagKey("group1-key1"))
                )
            )
        }

        // Reusing in group 1 a key that exists only in group 2 is allowed.
        env.dispatch(
            TagAction.TagManagedUpdateKey(
                tagRef(TagId(group1Tag2.id.value)),
                tagKey(TagKey("group2-key1"))
            )
        )

        fun assertTagKey(expected: String, id: TagId) {
            val found = env.tagStorage.findTagByIdOptional(id)
            assertNotNull(found)
            assertEquals(TagKey(expected), found.key)
        }

        assertTagKey("group1-key1", group1Tag1.id)
        assertTagKey("group2-key1", group1Tag2.id)
    }

    @Test
    fun `tag managed update unknown tag then error`() {
        val env = TagTestEnv()
        val groupKey = TagGroupKey("group-key")
        env.dispatch(TagAction.TagGroupCreate(groupKey, null, null))
        val group = env.tagStorage.findTagGroupByKeyOptional(groupKey)
        assertNotNull(group)

        // Why this test:
        // Update must fail with a dedicated managed-tag-not-found error when the target tag is absent.
        assertFailsWith<TagManagedNotFoundException> {
            env.dispatch(
                TagAction.TagManagedUpdateDescription(
                    tagRef(groupKey, TagKey("missing-tag")),
                    "new-description"
                )
            )
        }
    }

    @Test
    fun `tag managed update works with tag by id`() {
        val env = TagTestEnv()
        val groupKey = TagGroupKey("group-key")
        env.dispatch(TagAction.TagGroupCreate(groupKey, null, null))
        val group = env.tagStorage.findTagGroupByKeyOptional(groupKey)
        assertNotNull(group)
        val managedKey = TagKey("managed-key")
        env.dispatch(TagAction.TagManagedCreate(TagGroupRef.ById(group.id), tagKey(managedKey), "name", "description"))
        val tag = env.tagStorage.findTagByKeyOptional(group.id, TagKey(managedKey.value))
        assertNotNull(tag)

        // Why this test:
        // Tag references can be passed by id; we validate update name/description on this path.
        env.dispatch(
            TagAction.TagManagedUpdateName(
                tagRef(TagId(tag.id.value)),
                "new-name"
            )
        )
        env.dispatch(
            TagAction.TagManagedUpdateDescription(
                tagRef(TagId(tag.id.value)),
                "new-description"
            )
        )

        val updated = env.tagStorage.findTagByIdOptional(TagId(tag.id.value))
        assertNotNull(updated)
        assertEquals("new-name", updated.name)
        assertEquals("new-description", updated.description)
    }

    @Test
    fun `tag managed update key with same value is a no-op and does not fail`() {
        val env = TagTestEnv()
        val groupKey = TagGroupKey("group-key")
        env.dispatch(TagAction.TagGroupCreate(groupKey, null, null))
        val group = env.tagStorage.findTagGroupByKeyOptional(groupKey)
        assertNotNull(group)
        val managedKey = TagKey("managed-key")
        env.dispatch(TagAction.TagManagedCreate(TagGroupRef.ById(group.id), tagKey(managedKey), null, null))
        val tag = env.tagStorage.findTagByKeyOptional(group.id, TagKey(managedKey.value))
        assertNotNull(tag)

        // Why this test:
        // Updating a key to its current value should be accepted and must not trigger duplicate detection.
        env.dispatch(
            TagAction.TagManagedUpdateKey(
                tagRef(TagId(tag.id.value)),
                tagKey(managedKey)
            )
        )

        val found = env.tagStorage.findTagByIdOptional(TagId(tag.id.value))
        assertNotNull(found)
        assertEquals(TagKey(managedKey.value), found.key)
    }

    @Test
    fun `tag managed delete`() {
        val env = TagTestEnv()
        val groupKey = TagGroupKey("group-key")
        env.dispatch(TagAction.TagGroupCreate(groupKey, null, null))
        val group = env.tagStorage.findTagGroupByKeyOptional(groupKey)
        assertNotNull(group)

        val managedKey1 = TagKey("managed-key1")
        val managedKey2 = TagKey("managed-key2")
        env.dispatch(TagAction.TagManagedCreate(TagGroupRef.ById(group.id), tagKey(managedKey1), null, null))
        env.dispatch(TagAction.TagManagedCreate(TagGroupRef.ById(group.id), tagKey(managedKey2), null, null))

        env.dispatch(TagAction.TagManagedDelete(tagRef(groupKey, managedKey1)))
        assertNull(env.tagStorage.findTagByKeyOptional(group.id, TagKey(managedKey1.value)))
        assertNotNull(env.tagStorage.findTagByKeyOptional(group.id, TagKey(managedKey2.value)))

        env.dispatch(TagAction.TagManagedDelete(tagRef(groupKey, managedKey2)))
        assertNull(env.tagStorage.findTagByKeyOptional(group.id, TagKey(managedKey2.value)))
    }

    @Test
    fun `tag managed delete unknown tag then error`() {
        val env = TagTestEnv()
        val groupKey = TagGroupKey("group-key")
        env.dispatch(TagAction.TagGroupCreate(groupKey, null, null))
        val group = env.tagStorage.findTagGroupByKeyOptional(groupKey)
        assertNotNull(group)

        // Why this test:
        // Delete must fail with a dedicated managed-tag-not-found error when the target tag is absent.
        assertFailsWith<TagManagedNotFoundException> {
            env.dispatch(
                TagAction.TagManagedDelete(tagRef(groupKey, TagKey("missing-tag"))
                )
            )
        }
    }

    @Test
    fun `tag managed operations work with group by key`() {
        val env = TagTestEnv()
        val groupKey = TagGroupKey("group-key")
        env.dispatch(TagAction.TagGroupCreate(groupKey, null, null))
        val managedKey = TagKey("managed-key")

        // Why this test:
        // Group references can be passed by key in commands; we validate create/update/delete on this path.
        env.dispatch(
                TagAction.TagManagedCreate(
                    TagGroupRef.ByKey(groupKey),
                    tagKey(managedKey),
                    "name",
                    "description"
                )
        )

        env.dispatch(
            TagAction.TagManagedUpdateName(tagRef(groupKey, managedKey), "new-name"
            )
        )
        env.dispatch(
            TagAction.TagManagedUpdateDescription(tagRef(groupKey, managedKey), "new-description"
            )
        )

        val group = env.tagStorage.findTagGroupByKeyOptional(groupKey)
        assertNotNull(group)
        val found = env.tagStorage.findTagByKeyOptional(group.id, TagKey(managedKey.value))
        assertNotNull(found)
        assertEquals("new-name", found.name)
        assertEquals("new-description", found.description)

        env.dispatch(TagAction.TagManagedDelete(tagRef(groupKey, managedKey)))
        assertNull(env.tagStorage.findTagByKeyOptional(group.id, TagKey(managedKey.value)))
    }

    @Test
    fun `tag managed delete works with tag by id`() {
        val env = TagTestEnv()
        val groupKey = TagGroupKey("group-key")
        env.dispatch(TagAction.TagGroupCreate(groupKey, null, null))
        val group = env.tagStorage.findTagGroupByKeyOptional(groupKey)
        assertNotNull(group)
        val managedKey = TagKey("managed-key")
        env.dispatch(TagAction.TagManagedCreate(TagGroupRef.ById(group.id), tagKey(managedKey), "name", "description"))
        val tag = env.tagStorage.findTagByKeyOptional(group.id, TagKey(managedKey.value))
        assertNotNull(tag)

        // Why this test:
        // Tag references can be passed by id; we validate delete on this path.
        env.dispatch(TagAction.TagManagedDelete(tagRef(TagId(tag.id.value))))
        assertNull(env.tagStorage.findTagByIdOptional(TagId(tag.id.value)))
    }


}
