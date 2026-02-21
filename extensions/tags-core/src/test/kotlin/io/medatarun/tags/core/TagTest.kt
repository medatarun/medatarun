package io.medatarun.tags.core

import io.medatarun.tags.core.actions.TagAction
import io.medatarun.tags.core.domain.TagFreeDuplicateKeyException
import io.medatarun.tags.core.domain.TagFreeId
import io.medatarun.tags.core.domain.TagFreeKey
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

        assertNotEquals(found.id, found2.id,)

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
        assertTagName( null, key2)

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
        assertTagDescription( null, key2)

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
}