package io.medatarun.tags.core

import io.medatarun.tags.core.actions.TagAction
import io.medatarun.tags.core.domain.TagFreeDuplicateKeyException
import io.medatarun.tags.core.domain.TagFreeId
import io.medatarun.tags.core.domain.TagFreeKey
import io.medatarun.tags.core.domain.TagGroupDuplicateKeyException
import io.medatarun.tags.core.domain.TagGroupId
import io.medatarun.tags.core.domain.TagGroupKey
import io.medatarun.tags.core.domain.TagGroupRef
import io.medatarun.tags.core.domain.TagFreeRef.Companion.tagFreeRefId
import io.medatarun.tags.core.domain.TagFreeRef.Companion.tagFreeRefKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TagTest {

    @Test
    fun `free tag created with name and description`() {
        val env = TagTestEnv()
        val key = TagFreeKey("mykey")
        val key2 = TagFreeKey("mykey2")
        env.dispatch(TagAction.TagFreeCreate(key, "name", "description"))
        val found = env.tagQueries.findTagFreeByRef(tagFreeRefKey(key))
        assertEquals(key, found.key)
        assertEquals("name", found.name)
        assertEquals("description", found.description)

        // create again
        env.dispatch(TagAction.TagFreeCreate(key2, "name2", "description2"))
        val found2 = env.tagQueries.findTagFreeByRef(tagFreeRefKey(key2))
        assertEquals(key2, found2.key)
        assertEquals("name2", found2.name)
        assertEquals("description2", found2.description)

        assertNotEquals(found.id, found2.id)

    }

    @Test
    fun `free tag created without name and description`() {
        val env = TagTestEnv()
        val key = TagFreeKey("mykey")
        env.dispatch(TagAction.TagFreeCreate(key, null, null))
        val found = env.tagQueries.findTagFreeByRef(tagFreeRefKey(key))
        assertEquals(key, found.key)
        assertNull(found.name)
        assertNull(found.description)
    }

    @Test
    fun `free tag create with duplicate key then error`() {
        val env = TagTestEnv()
        val key = TagFreeKey("mykey")
        env.dispatch(TagAction.TagFreeCreate(key, null, null))
        assertFailsWith<TagFreeDuplicateKeyException> {
            env.dispatch(TagAction.TagFreeCreate(key, null, null))
        }
    }

    @Test
    fun `free tag update name`() {
        // given 2 tags with names set to null
        val env = TagTestEnv()
        val key1 = TagFreeKey("mykey1")
        val key2 = TagFreeKey("mykey2")
        env.dispatch(TagAction.TagFreeCreate(key1, null, null))
        env.dispatch(TagAction.TagFreeCreate(key2, null, null))

        fun assertTagName(expected: String?, key: TagFreeKey) {
            val found = env.tagQueries.findTagFreeByRef(tagFreeRefKey(key))
            assertEquals(expected, found.name)
        }

        // Update tag1 name
        env.dispatch(TagAction.TagFreeUpdateName(tagFreeRefKey(key1), "newname1"))

        // Then tag1 name shall be set and tag2 still null
        assertTagName("newname1", key1)
        assertTagName(null, key2)

        // Update tag2 name
        env.dispatch(TagAction.TagFreeUpdateName(tagFreeRefKey(key2), "newname2"))

        // Then tag2 name shall be set and tag1 unmodified
        assertTagName("newname1", key1)
        assertTagName("newname2", key2)

        // Changes the now not null tag1 name
        env.dispatch(TagAction.TagFreeUpdateName(tagFreeRefKey(key1), "newname1bis"))

        // Then tag1 name shall be set and tag2 unmodified
        assertTagName("newname1bis", key1)
        assertTagName("newname2", key2)

        // Changes the now not null tag2 name to null
        env.dispatch(TagAction.TagFreeUpdateName(tagFreeRefKey(key2), null))

        // Then tag2 name shall be null and tag1 unmodified
        assertTagName("newname1bis", key1)
        assertTagName(null, key2)

    }

    @Test
    fun `free tag update description`() {
        // given 2 tags with descriptions set to null
        val env = TagTestEnv()
        val key1 = TagFreeKey("mykey1")
        val key2 = TagFreeKey("mykey2")
        env.dispatch(TagAction.TagFreeCreate(key1, null, null))
        env.dispatch(TagAction.TagFreeCreate(key2, null, null))

        fun assertTagDescription(expected: String?, key: TagFreeKey) {
            val found = env.tagQueries.findTagFreeByRef(tagFreeRefKey(key))
            assertEquals(expected, found.description)
        }

        // Update tag1 description
        env.dispatch(TagAction.TagFreeUpdateDescription(tagFreeRefKey(key1), "newname1"))

        // Then tag1 description shall be set and tag2 still null
        assertTagDescription("newname1", key1)
        assertTagDescription(null, key2)

        // Update tag2 description
        env.dispatch(TagAction.TagFreeUpdateDescription(tagFreeRefKey(key2), "newname2"))

        // Then tag2 description shall be set and tag1 unmodified
        assertTagDescription("newname1", key1)
        assertTagDescription("newname2", key2)

        // Changes the now not null tag1 description
        env.dispatch(TagAction.TagFreeUpdateDescription(tagFreeRefKey(key1), "newname1bis"))

        // Then tag1 description shall be set and tag2 unmodified
        assertTagDescription("newname1bis", key1)
        assertTagDescription("newname2", key2)

        // Changes the now not null tag2 description to null
        env.dispatch(TagAction.TagFreeUpdateDescription(tagFreeRefKey(key2), null))

        // Then tag2 description shall be null and tag1 unmodified
        assertTagDescription("newname1bis", key1)
        assertTagDescription(null, key2)
    }

    @Test
    fun `free tag update key`() {
        // given 2 tags with names set to null
        val env = TagTestEnv()
        val key1 = TagFreeKey("mykey1")
        val key2 = TagFreeKey("mykey2")
        env.dispatch(TagAction.TagFreeCreate(key1, null, null))
        env.dispatch(TagAction.TagFreeCreate(key2, null, null))

        val tag1Id = env.tagQueries.findTagFreeByRef(tagFreeRefKey(key1)).id
        val tag2Id = env.tagQueries.findTagFreeByRef(tagFreeRefKey(key2)).id

        fun assertTagKey(expected: String, id: TagFreeId) {
            val found = env.tagQueries.findTagFreeByRef(tagFreeRefId(id))
            assertEquals(TagFreeKey(expected), found.key)
        }

        // Update tag1 key
        env.dispatch(TagAction.TagFreeUpdateKey(tagFreeRefId(tag1Id), TagFreeKey("newkey1")))

        // Cheks that tag1 key is changed and tag2 key unmodified
        assertTagKey("newkey1", tag1Id)
        assertTagKey("mykey2", tag2Id)

        // Update tag2 key to the same key as tag1 raises a duplicate exception
        assertFailsWith<TagFreeDuplicateKeyException> {
            env.dispatch(TagAction.TagFreeUpdateKey(tagFreeRefId(tag2Id), TagFreeKey("newkey1")))
        }

        // Update tag2 key to another key is ok
        env.dispatch(TagAction.TagFreeUpdateKey(tagFreeRefId(tag2Id), TagFreeKey("newkey2")))

        // Checks the new tag2 key
        assertTagKey("newkey1", tag1Id)
        assertTagKey("newkey2", tag2Id)
    }

    @Test
    fun `free tag delete`() {
        val env = TagTestEnv()
        val key = TagFreeKey("mykey")
        val key2 = TagFreeKey("mykey2")
        env.dispatch(TagAction.TagFreeCreate(key, null, null))
        env.dispatch(TagAction.TagFreeCreate(key2, null, null))
        env.dispatch(TagAction.TagFreeDelete(tagFreeRefKey(key)))
        assertNull(env.tagQueries.findTagFreeByRefOptional(tagFreeRefKey(key)))
        assertNotNull(env.tagQueries.findTagFreeByRefOptional(tagFreeRefKey(key2)))
        env.dispatch(TagAction.TagFreeDelete(tagFreeRefKey(key2)))
        assertNull(env.tagQueries.findTagFreeByRefOptional(tagFreeRefKey(key2)))
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
    fun `group tag delete`() {
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

    // Managed Tags


}
