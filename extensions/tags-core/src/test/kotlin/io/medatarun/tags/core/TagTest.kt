package io.medatarun.tags.core

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.platform.kernel.EventObserver
import io.medatarun.tags.core.actions.TagAction
import io.medatarun.tags.core.domain.*
import io.medatarun.tags.core.domain.TagRef.Companion.tagRefId
import io.medatarun.tags.core.fixtures.*
import io.medatarun.tags.core.fixtures.SampleId.Companion.sampleId
import io.medatarun.tags.core.ports.needs.TagScopeManager
import kotlin.test.*

class TagTest {
    private class SampleScopeManagerDeleteVetoException(message: String) : RuntimeException(message)

    private fun tagRef(scopeRef: TagScopeRef.Local, key: TagKey): TagRef {
        return TagRef.ByKey(scopeRef = scopeRef, groupKey = null, key = key)
    }

    private fun tagRef(groupKey: TagGroupKey, key: TagKey): TagRef {
        return TagRef.ByKey(scopeRef = TagScopeRef.Global, groupKey = groupKey, key = key)
    }

    private fun tagRef(id: TagId): TagRef = TagRef.ById(id)

    private fun createRecipeScope(
        service: RecipeService,
        name: String = "scope-root",
    ): TagScopeRef.Local {
        val recipeId = sampleId()
        service.createRecipe(Recipe(recipeId, name, emptyList()))
        return recipeScopeRef(recipeId)
    }

    @Test
    fun `tag free created with name and description`() {
        val env = createEnvironment()
        val scopeRef = createRecipeScope(env.recipeService)
        val key = TagKey("mykey")
        val key2 = TagKey("mykey2")
        env.dispatch(TagAction.TagLocalCreate(scopeRef, key, "name", "description"))
        val found = env.tagQueries.findTagByRef(tagRef(scopeRef, key))
        assertEquals(key, found.key)
        assertFalse(found.isGlobal)
        assertEquals(scopeRef, found.scope)
        assertNull(found.groupId)
        assertEquals("name", found.name)
        assertEquals("description", found.description)

        // create again
        env.dispatch(TagAction.TagLocalCreate(scopeRef, key2, "name2", "description2"))
        val found2 = env.tagQueries.findTagByRef(tagRef(scopeRef, key2))
        assertEquals(key2, found2.key)
        assertFalse(found2.isGlobal)
        assertEquals(scopeRef, found2.scope)
        assertNull(found2.groupId)
        assertEquals("name2", found2.name)
        assertEquals("description2", found2.description)

        assertNotEquals(found.id, found2.id)

    }

    @Test
    fun `tag free created without name and description`() {
        val env = createEnvironment()
        val scopeRef = createRecipeScope(env.recipeService)
        val key = TagKey("mykey")
        env.dispatch(TagAction.TagLocalCreate(scopeRef, key, null, null))
        val found = env.tagQueries.findTagByRef(tagRef(scopeRef, key))
        assertEquals(key, found.key)
        assertFalse(found.isGlobal)
        assertEquals(scopeRef, found.scope)
        assertNull(found.groupId)
        assertNull(found.name)
        assertNull(found.description)
    }

    @Test
    fun `tag free create with duplicate key then error`() {
        val env = createEnvironment()
        val scopeRef = createRecipeScope(env.recipeService)
        val key = TagKey("mykey")
        env.dispatch(TagAction.TagLocalCreate(scopeRef, key, null, null))
        assertFailsWith<TagFreeDuplicateKeyException> {
            env.dispatch(TagAction.TagLocalCreate(scopeRef, key, null, null))
        }
    }

    @Test
    fun `tag free update name`() {
        // given 2 tags with names set to null
        val env = createEnvironment()
        val scopeRef = createRecipeScope(env.recipeService)
        val key1 = TagKey("mykey1")
        val key2 = TagKey("mykey2")
        env.dispatch(TagAction.TagLocalCreate(scopeRef, key1, null, null))
        env.dispatch(TagAction.TagLocalCreate(scopeRef, key2, null, null))

        fun assertTagName(expected: String?, key: TagKey) {
            val found = env.tagQueries.findTagByRef(tagRef(scopeRef, key))
            assertEquals(expected, found.name)
            assertFalse(found.isGlobal)
        }

        // Update tag1 name
        env.dispatch(TagAction.TagLocalUpdateName(tagRef(scopeRef, key1), "newname1"))

        // Then tag1 name shall be set and tag2 still null
        assertTagName("newname1", key1)
        assertTagName(null, key2)

        // Update tag2 name
        env.dispatch(TagAction.TagLocalUpdateName(tagRef(scopeRef, key2), "newname2"))

        // Then tag2 name shall be set and tag1 unmodified
        assertTagName("newname1", key1)
        assertTagName("newname2", key2)

        // Changes the now not null tag1 name
        env.dispatch(TagAction.TagLocalUpdateName(tagRef(scopeRef, key1), "newname1bis"))

        // Then tag1 name shall be set and tag2 unmodified
        assertTagName("newname1bis", key1)
        assertTagName("newname2", key2)

        // Changes the now not null tag2 name to null
        env.dispatch(TagAction.TagLocalUpdateName(tagRef(scopeRef, key2), null))

        // Then tag2 name shall be null and tag1 unmodified
        assertTagName("newname1bis", key1)
        assertTagName(null, key2)

    }

    @Test
    fun `free tag update description`() {
        // given 2 tags with descriptions set to null
        val env = createEnvironment()
        val scopeRef = createRecipeScope(env.recipeService)
        val key1 = TagKey("mykey1")
        val key2 = TagKey("mykey2")
        env.dispatch(TagAction.TagLocalCreate(scopeRef, key1, null, null))
        env.dispatch(TagAction.TagLocalCreate(scopeRef, key2, null, null))

        fun assertTagDescription(expected: String?, key: TagKey) {
            val found = env.tagQueries.findTagByRef(tagRef(scopeRef, key))
            assertEquals(expected, found.description)
            assertFalse(found.isGlobal)
        }

        // Update tag1 description
        env.dispatch(TagAction.TagLocalUpdateDescription(tagRef(scopeRef, key1), "newname1"))

        // Then tag1 description shall be set and tag2 still null
        assertTagDescription("newname1", key1)
        assertTagDescription(null, key2)

        // Update tag2 description
        env.dispatch(TagAction.TagLocalUpdateDescription(tagRef(scopeRef, key2), "newname2"))

        // Then tag2 description shall be set and tag1 unmodified
        assertTagDescription("newname1", key1)
        assertTagDescription("newname2", key2)

        // Changes the now not null tag1 description
        env.dispatch(TagAction.TagLocalUpdateDescription(tagRef(scopeRef, key1), "newname1bis"))

        // Then tag1 description shall be set and tag2 unmodified
        assertTagDescription("newname1bis", key1)
        assertTagDescription("newname2", key2)

        // Changes the now not null tag2 description to null
        env.dispatch(TagAction.TagLocalUpdateDescription(tagRef(scopeRef, key2), null))

        // Then tag2 description shall be null and tag1 unmodified
        assertTagDescription("newname1bis", key1)
        assertTagDescription(null, key2)
    }

    @Test
    fun `free tag update key`() {
        // given 2 tags with names set to null
        val env = createEnvironment()
        val scopeRef = createRecipeScope(env.recipeService)
        val key1 = TagKey("mykey1")
        val key2 = TagKey("mykey2")
        env.dispatch(TagAction.TagLocalCreate(scopeRef, key1, null, null))
        env.dispatch(TagAction.TagLocalCreate(scopeRef, key2, null, null))

        val tag1Id = env.tagQueries.findTagByRef(tagRef(scopeRef, key1)).id
        val tag2Id = env.tagQueries.findTagByRef(tagRef(scopeRef, key2)).id

        fun assertTagKey(expected: String, id: TagId) {
            val found = env.tagQueries.findTagByRef(tagRefId(id))
            assertEquals(TagKey(expected), found.key)
            assertFalse(found.isGlobal)
        }

        // Update tag1 key
        env.dispatch(TagAction.TagLocalUpdateKey(tagRef(tag1Id), TagKey("newkey1")))

        // Cheks that tag1 key is changed and tag2 key unmodified
        assertTagKey("newkey1", tag1Id)
        assertTagKey("mykey2", tag2Id)

        // Update tag2 key to the same key as tag1 raises a duplicate exception
        assertFailsWith<TagFreeDuplicateKeyException> {
            env.dispatch(TagAction.TagLocalUpdateKey(tagRef(tag2Id), TagKey("newkey1")))
        }

        // Update tag2 key to another key is ok
        env.dispatch(TagAction.TagLocalUpdateKey(tagRef(tag2Id), TagKey("newkey2")))

        // Checks the new tag2 key
        assertTagKey("newkey1", tag1Id)
        assertTagKey("newkey2", tag2Id)
    }

    @Test
    fun `free tag delete`() {
        val env = createEnvironment()
        val scopeRef = createRecipeScope(env.recipeService)
        val key = TagKey("mykey")
        val key2 = TagKey("mykey2")
        env.dispatch(TagAction.TagLocalCreate(scopeRef, key, null, null))
        env.dispatch(TagAction.TagLocalCreate(scopeRef, key2, null, null))
        env.dispatch(TagAction.TagLocalDelete(tagRef(scopeRef, key)))
        assertNull(env.tagQueries.findTagByRefOptional(tagRef(scopeRef, key)))
        assertNotNull(env.tagQueries.findTagByRefOptional(tagRef(scopeRef, key2)))
        env.dispatch(TagAction.TagLocalDelete(tagRef(scopeRef, key2)))
        assertNull(env.tagQueries.findTagByRefOptional(tagRef(scopeRef, key2)))
    }

    // Tag groups

    @Test
    fun `tag group created with name and description`() {
        val env = createEnvironment()
        val key = TagGroupKey("mykey")
        val key2 = TagGroupKey("mykey2")
        env.dispatch(TagAction.TagGroupCreate(key, "name", "description"))
        val found = env.tagQueries.findTagGroupByKeyOptional(key)
        assertNotNull(found)
        assertEquals(key, found.key)
        assertEquals("name", found.name)
        assertEquals("description", found.description)

        // create again
        env.dispatch(TagAction.TagGroupCreate(key2, "name2", "description2"))
        val found2 = env.tagQueries.findTagGroupByKeyOptional(key2)
        assertNotNull(found2)
        assertEquals(key2, found2.key)
        assertEquals("name2", found2.name)
        assertEquals("description2", found2.description)

        assertNotEquals(found.id, found2.id)
    }

    @Test
    fun `tag group created without name and description`() {
        val env = createEnvironment()
        val key = TagGroupKey("mykey")
        env.dispatch(TagAction.TagGroupCreate(key, null, null))
        val found = env.tagQueries.findTagGroupByKeyOptional(key)
        assertNotNull(found)
        assertEquals(key, found.key)
        assertNull(found.name)
        assertNull(found.description)
    }

    @Test
    fun `tag group create with duplicate key then error`() {
        val env = createEnvironment()
        val key = TagGroupKey("mykey")
        env.dispatch(TagAction.TagGroupCreate(key, null, null))
        assertFailsWith<TagGroupDuplicateKeyException> {
            env.dispatch(TagAction.TagGroupCreate(key, null, null))
        }
    }

    @Test
    fun `tag group update name`() {
        // given 2 groups with names set to null
        val env = createEnvironment()
        val key1 = TagGroupKey("mykey1")
        val key2 = TagGroupKey("mykey2")
        env.dispatch(TagAction.TagGroupCreate(key1, null, null))
        env.dispatch(TagAction.TagGroupCreate(key2, null, null))

        fun assertTagGroupName(expected: String?, key: TagGroupKey) {
            val found = env.tagQueries.findTagGroupByKeyOptional(key)
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
        val env = createEnvironment()
        val key1 = TagGroupKey("mykey1")
        val key2 = TagGroupKey("mykey2")
        env.dispatch(TagAction.TagGroupCreate(key1, null, null))
        env.dispatch(TagAction.TagGroupCreate(key2, null, null))

        fun assertTagGroupDescription(expected: String?, key: TagGroupKey) {
            val found = env.tagQueries.findTagGroupByKeyOptional(key)
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
        val env = createEnvironment()
        val key1 = TagGroupKey("mykey1")
        val key2 = TagGroupKey("mykey2")
        env.dispatch(TagAction.TagGroupCreate(key1, null, null))
        env.dispatch(TagAction.TagGroupCreate(key2, null, null))

        val group1 = env.tagQueries.findTagGroupByKeyOptional(key1)
        assertNotNull(group1)
        val group2 = env.tagQueries.findTagGroupByKeyOptional(key2)
        assertNotNull(group2)
        val tagGroup1Id = group1.id
        val tagGroup2Id = group2.id

        fun assertTagGroupKey(expected: String, id: TagGroupId) {
            val found = env.tagQueries.findTagGroupByIdOptional(id)
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
        val env = createEnvironment()
        val key1 = TagGroupKey("mykey1")
        val key2 = TagGroupKey("mykey2")
        env.dispatch(TagAction.TagGroupCreate(key1, null, null))
        env.dispatch(TagAction.TagGroupCreate(key2, null, null))
        env.dispatch(TagAction.TagGroupDelete(TagGroupRef.ByKey(key1)))
        assertNull(env.tagQueries.findTagGroupByKeyOptional(key1))
        assertNotNull(env.tagQueries.findTagGroupByKeyOptional(key2))
        env.dispatch(TagAction.TagGroupDelete(TagGroupRef.ByKey(key2)))
        assertNull(env.tagQueries.findTagGroupByKeyOptional(key2))
    }

    @Test
    fun `tag group delete deletes managed tags`() {
        val env = createEnvironment()
        val scopeRef = createRecipeScope(env.recipeService)
        val groupKey1 = TagGroupKey("group-key1")
        val groupKey2 = TagGroupKey("group-key2")
        env.dispatch(TagAction.TagGroupCreate(groupKey1, null, null))
        env.dispatch(TagAction.TagGroupCreate(groupKey2, null, null))

        val group1 = env.tagQueries.findTagGroupByKeyOptional(groupKey1)
        assertNotNull(group1)
        val group2 = env.tagQueries.findTagGroupByKeyOptional(groupKey2)
        assertNotNull(group2)

        val managedKey1 = TagKey("managed-key1")
        val managedKey1b = TagKey("managed-key1b")
        val managedKey2 = TagKey("managed-key2")
        env.dispatch(TagAction.TagGlobalCreate(TagGroupRef.ById(group1.id), managedKey1, null, null))
        env.dispatch(TagAction.TagGlobalCreate(TagGroupRef.ById(group1.id), managedKey1b, null, null))
        env.dispatch(TagAction.TagGlobalCreate(TagGroupRef.ById(group2.id), managedKey2, null, null))
        val freeKey1 = TagKey("free-key1")
        val freeKey2 = TagKey("free-key2")
        env.dispatch(TagAction.TagLocalCreate(scopeRef, freeKey1, null, null))
        env.dispatch(TagAction.TagLocalCreate(scopeRef, freeKey2, null, null))

        // Business rule: deleting a group also deletes the managed tags that belong to it.
        env.dispatch(TagAction.TagGroupDelete(TagGroupRef.ById(group1.id)))

        // All managed tags of the deleted group must be removed.
        assertNull(env.tagQueries.findTagByKeyOptional(group1.id, managedKey1))
        assertNull(env.tagQueries.findTagByKeyOptional(group1.id, managedKey1b))
        // Managed tags in other groups must remain unchanged.
        assertNotNull(env.tagQueries.findTagByKeyOptional(group2.id, managedKey2))
        // Free tags (without group) must remain unchanged.
        assertNotNull(env.tagQueries.findTagByRefOptional(tagRef(scopeRef, freeKey1)))
        assertNotNull(env.tagQueries.findTagByRefOptional(tagRef(scopeRef, freeKey2)))
    }

    @Test
    fun `tag group delete removes managed tags from objects`() {
        // Why this test:
        // SQL cascade deletes managed tags when a group is deleted, but the business rule also requires notifying
        // scope managers so they can remove those tag ids from every object they own.
        val env = createEnvironment()
        val scopeRef = createRecipeScope(env.recipeService)
        val groupKey = TagGroupKey("governance")
        env.dispatch(TagAction.TagGroupCreate(groupKey, null, null))
        val group = env.tagQueries.findTagGroupByKeyOptional(groupKey)
        assertNotNull(group)

        env.dispatch(TagAction.TagGlobalCreate(TagGroupRef.ById(group.id), TagKey("managed-a"), null, null))
        env.dispatch(TagAction.TagGlobalCreate(TagGroupRef.ById(group.id), TagKey("managed-b"), null, null))
        env.dispatch(TagAction.TagLocalCreate(scopeRef, TagKey("free-keep"), null, null))

        val managedA = env.tagQueries.findTagByKeyOptional(group.id, TagKey("managed-a"))
        assertNotNull(managedA)
        val managedB = env.tagQueries.findTagByKeyOptional(group.id, TagKey("managed-b"))
        assertNotNull(managedB)
        val freeKeep = env.tagQueries.findTagByRef(tagRef(scopeRef, TagKey("free-keep")))

        val recipeId = sampleId()
        val ingredientId = sampleId()
        val stepId = sampleId()
        val vehicleId = sampleId()
        val partId = sampleId()

        env.recipeService.createRecipe(Recipe(recipeId, "Pasta", listOf(managedA.id, freeKeep.id, managedB.id)))
        env.recipeService.createIngredient(Ingredient(ingredientId, recipeId, "Tomato", listOf(managedA.id)))
        env.recipeService.createRecipeStep(RecipeStep(stepId, recipeId, "Boil", listOf(managedB.id, freeKeep.id)))
        env.vehicleService.createVehicle(Vehicle(vehicleId, "Truck", listOf(managedA.id, managedB.id)))
        env.vehicleService.createVehiclePart(VehiclePart(partId, vehicleId, "Wheel", listOf(freeKeep.id, managedB.id)))

        env.dispatch(TagAction.TagGroupDelete(TagGroupRef.ById(group.id)))

        // Managed tags are deleted from storage by SQL cascade.
        assertNull(env.tagQueries.findTagByIdOptional(managedA.id))
        assertNull(env.tagQueries.findTagByIdOptional(managedB.id))
        // Free tag remains.
        assertNotNull(env.tagQueries.findTagByRefOptional(tagRef(freeKeep.id)))

        // Managed tag ids are removed from all objects, other tags are kept.
        assertEquals(listOf(freeKeep.id), env.recipeService.findRecipeById(recipeId).tags)
        assertEquals(emptyList(), env.recipeService.findIngredientById(ingredientId).tags)
        assertEquals(listOf(freeKeep.id), env.recipeService.findRecipeStepById(stepId).tags)
        assertEquals(emptyList(), env.vehicleService.findVehicleById(vehicleId).tags)
        assertEquals(listOf(freeKeep.id), env.vehicleService.findVehiclePartById(partId).tags)
    }

    // Managed Tags

    @Test
    fun `tag managed created with name and description`() {
        val env = createEnvironment()
        val groupKey = TagGroupKey("group-key")
        env.dispatch(TagAction.TagGroupCreate(groupKey, "group", "group-description"))

        val group = env.tagQueries.findTagGroupByKeyOptional(groupKey)
        assertNotNull(group)

        val managedKey = TagKey("managed-key")
        val managedKey2 = TagKey("managed-key2")
        env.dispatch(TagAction.TagGlobalCreate(TagGroupRef.ById(group.id), managedKey, "name", "description"))
        // This test also checks that a second managed tag can be created in the same group
        // (with a different key) and gets a distinct identifier.
        env.dispatch(TagAction.TagGlobalCreate(TagGroupRef.ById(group.id), managedKey2, "name2", "description2"))

        val found = env.tagQueries.findTagByKeyOptional(group.id, managedKey)
        assertNotNull(found)
        assertEquals(TagScopeRef.Global, found.scope)
        assertTrue(found.isGlobal)
        assertEquals(group.id, found.groupId)
        assertEquals(managedKey, found.key)
        assertEquals("name", found.name)
        assertEquals("description", found.description)

        val found2 = env.tagQueries.findTagByKeyOptional(group.id, managedKey2)
        assertNotNull(found2)
        assertEquals(TagScopeRef.Global, found2.scope)
        assertTrue(found2.isGlobal)
        assertEquals(group.id, found2.groupId)
        assertEquals(managedKey2, found2.key)
        assertEquals("name2", found2.name)
        assertEquals("description2", found2.description)
        assertNotEquals(found.id, found2.id)
    }

    @Test
    fun `tag managed created without name and description`() {
        val env = createEnvironment()
        val groupKey = TagGroupKey("group-key")
        env.dispatch(TagAction.TagGroupCreate(groupKey, null, null))
        val group = env.tagQueries.findTagGroupByKeyOptional(groupKey)
        assertNotNull(group)

        val managedKey = TagKey("managed-key")
        env.dispatch(TagAction.TagGlobalCreate(TagGroupRef.ById(group.id), managedKey, null, null))

        val found = env.tagQueries.findTagByKeyOptional(group.id, managedKey)
        assertNotNull(found)
        assertEquals(TagScopeRef.Global, found.scope)
        assertTrue(found.isGlobal)
        assertEquals(group.id, found.groupId)
        assertEquals(managedKey, found.key)
        assertNull(found.name)
        assertNull(found.description)
    }

    @Test
    fun `tag managed create with duplicate key in same group then error`() {
        val env = createEnvironment()
        val groupKey = TagGroupKey("group-key")
        env.dispatch(TagAction.TagGroupCreate(groupKey, null, null))
        val group = env.tagQueries.findTagGroupByKeyOptional(groupKey)
        assertNotNull(group)

        val managedKey = TagKey("managed-key")
        env.dispatch(TagAction.TagGlobalCreate(TagGroupRef.ById(group.id), managedKey, null, null))
        assertFailsWith<TagManagedDuplicateKeyException> {
            env.dispatch(TagAction.TagGlobalCreate(TagGroupRef.ById(group.id), managedKey, null, null))
        }
    }

    @Test
    fun `tag managed create with same key in different groups is allowed`() {
        val env = createEnvironment()
        val groupKey1 = TagGroupKey("group-key1")
        val groupKey2 = TagGroupKey("group-key2")
        env.dispatch(TagAction.TagGroupCreate(groupKey1, null, null))
        env.dispatch(TagAction.TagGroupCreate(groupKey2, null, null))

        val group1 = env.tagQueries.findTagGroupByKeyOptional(groupKey1)
        assertNotNull(group1)
        val group2 = env.tagQueries.findTagGroupByKeyOptional(groupKey2)
        assertNotNull(group2)

        val managedKey = TagKey("shared-key")
        env.dispatch(TagAction.TagGlobalCreate(TagGroupRef.ById(group1.id), managedKey, "name-1", "description-1"))
        env.dispatch(TagAction.TagGlobalCreate(TagGroupRef.ById(group2.id), managedKey, "name-2", "description-2"))

        val found1 = env.tagQueries.findTagByKeyOptional(group1.id, managedKey)
        assertNotNull(found1)
        val found2 = env.tagQueries.findTagByKeyOptional(group2.id, managedKey)
        assertNotNull(found2)
        assertNotEquals(found1.id, found2.id)
        assertEquals(group1.id, found1.groupId)
        assertEquals(group2.id, found2.groupId)
    }

    @Test
    fun `tag managed create with unknown group then error`() {
        val env = createEnvironment()

        // Why this test:
        // Creating a managed tag without an existing group should fail early with a group-not-found error.
        assertFailsWith<TagGroupNotFoundException> {
            env.dispatch(
                TagAction.TagGlobalCreate(
                    TagGroupRef.ByKey(TagGroupKey("missing-group")),
                    TagKey("managed-key"),
                    "name",
                    "description"
                )
            )
        }
    }

    @Test
    fun `tag managed update name`() {
        val env = createEnvironment()
        val groupKey = TagGroupKey("group-key")
        env.dispatch(TagAction.TagGroupCreate(groupKey, null, null))
        val group = env.tagQueries.findTagGroupByKeyOptional(groupKey)
        assertNotNull(group)

        val managedKey1 = TagKey("managed-key1")
        val managedKey2 = TagKey("managed-key2")
        env.dispatch(TagAction.TagGlobalCreate(TagGroupRef.ById(group.id), managedKey1, "name-1", null))
        env.dispatch(TagAction.TagGlobalCreate(TagGroupRef.ById(group.id), managedKey2, "name-2", null))

        fun assertTagManagedName(expected: String, key: TagKey) {
            val found = env.tagQueries.findTagByKeyOptional(group.id, key)
            assertNotNull(found)
            assertEquals(expected, found.name)
        }

        env.dispatch(
            TagAction.TagGlobalUpdateName(
                tagRef(groupKey, managedKey1), "new-name-1"
            )
        )
        assertTagManagedName("new-name-1", managedKey1)
        assertTagManagedName("name-2", managedKey2)

        env.dispatch(
            TagAction.TagGlobalUpdateName(
                tagRef(groupKey, managedKey2), "new-name-2"
            )
        )
        assertTagManagedName("new-name-1", managedKey1)
        assertTagManagedName("new-name-2", managedKey2)
    }

    @Test
    fun `tag managed update description`() {
        val env = createEnvironment()
        val groupKey = TagGroupKey("group-key")
        env.dispatch(TagAction.TagGroupCreate(groupKey, null, null))
        val group = env.tagQueries.findTagGroupByKeyOptional(groupKey)
        assertNotNull(group)

        val managedKey1 = TagKey("managed-key1")
        val managedKey2 = TagKey("managed-key2")
        env.dispatch(TagAction.TagGlobalCreate(TagGroupRef.ById(group.id), managedKey1, null, "description-1"))
        env.dispatch(TagAction.TagGlobalCreate(TagGroupRef.ById(group.id), managedKey2, null, "description-2"))

        fun assertTagManagedDescription(expected: String, key: TagKey) {
            val found = env.tagQueries.findTagByKeyOptional(group.id, key)
            assertNotNull(found)
            assertEquals(expected, found.description)
        }

        env.dispatch(
            TagAction.TagGlobalUpdateDescription(
                tagRef(groupKey, managedKey1), "new-description-1"
            )
        )
        assertTagManagedDescription("new-description-1", managedKey1)
        assertTagManagedDescription("description-2", managedKey2)

        env.dispatch(
            TagAction.TagGlobalUpdateDescription(
                tagRef(groupKey, managedKey2), "new-description-2"
            )
        )
        assertTagManagedDescription("new-description-1", managedKey1)
        assertTagManagedDescription("new-description-2", managedKey2)
    }

    @Test
    fun `tag managed update key with uniqueness relative to group`() {
        val env = createEnvironment()
        val groupKey1 = TagGroupKey("group-key1")
        val groupKey2 = TagGroupKey("group-key2")
        env.dispatch(TagAction.TagGroupCreate(groupKey1, null, null))
        env.dispatch(TagAction.TagGroupCreate(groupKey2, null, null))

        val group1 = env.tagQueries.findTagGroupByKeyOptional(groupKey1)
        assertNotNull(group1)
        val group2 = env.tagQueries.findTagGroupByKeyOptional(groupKey2)
        assertNotNull(group2)

        val group1Key1 = TagKey("group1-key1")
        val group1Key2 = TagKey("group1-key2")
        val group2Key1 = TagKey("group2-key1")
        env.dispatch(TagAction.TagGlobalCreate(TagGroupRef.ById(group1.id), group1Key1, null, null))
        env.dispatch(TagAction.TagGlobalCreate(TagGroupRef.ById(group1.id), group1Key2, null, null))
        env.dispatch(TagAction.TagGlobalCreate(TagGroupRef.ById(group2.id), group2Key1, null, null))

        val group1Tag1 = env.tagQueries.findTagByKeyOptional(group1.id, group1Key1)
        assertNotNull(group1Tag1)
        val group1Tag2 = env.tagQueries.findTagByKeyOptional(group1.id, group1Key2)
        assertNotNull(group1Tag2)

        // Duplicate key inside the same group is forbidden.
        assertFailsWith<TagManagedDuplicateKeyException> {
            env.dispatch(
                TagAction.TagGlobalUpdateKey(
                    tagRef(group1Tag2.id),
                    TagKey("group1-key1")
                )
            )
        }

        // Reusing in group 1 a key that exists only in group 2 is allowed.
        env.dispatch(
            TagAction.TagGlobalUpdateKey(
                tagRef(group1Tag2.id),
                TagKey("group2-key1")
            )
        )

        fun assertTagKey(expected: String, id: TagId) {
            val found = env.tagQueries.findTagByIdOptional(id)
            assertNotNull(found)
            assertEquals(TagKey(expected), found.key)
        }

        assertTagKey("group1-key1", group1Tag1.id)
        assertTagKey("group2-key1", group1Tag2.id)
    }

    @Test
    fun `tag managed update unknown tag then error`() {
        val env = createEnvironment()
        val groupKey = TagGroupKey("group-key")
        env.dispatch(TagAction.TagGroupCreate(groupKey, null, null))
        val group = env.tagQueries.findTagGroupByKeyOptional(groupKey)
        assertNotNull(group)

        // Why this test:
        // Update must fail with a dedicated managed-tag-not-found error when the target tag is absent.
        assertFailsWith<TagManagedNotFoundException> {
            env.dispatch(
                TagAction.TagGlobalUpdateDescription(
                    tagRef(groupKey, TagKey("missing-tag")),
                    "new-description"
                )
            )
        }
    }

    @Test
    fun `tag managed update works with tag by id`() {
        val env = createEnvironment()
        val groupKey = TagGroupKey("group-key")
        env.dispatch(TagAction.TagGroupCreate(groupKey, null, null))
        val group = env.tagQueries.findTagGroupByKeyOptional(groupKey)
        assertNotNull(group)
        val managedKey = TagKey("managed-key")
        env.dispatch(TagAction.TagGlobalCreate(TagGroupRef.ById(group.id), managedKey, "name", "description"))
        val tag = env.tagQueries.findTagByKeyOptional(group.id, managedKey)
        assertNotNull(tag)

        // Why this test:
        // Tag references can be passed by id; we validate update name/description on this path.
        env.dispatch(
            TagAction.TagGlobalUpdateName(
                tagRef(tag.id),
                "new-name"
            )
        )
        env.dispatch(
            TagAction.TagGlobalUpdateDescription(
                tagRef(tag.id),
                "new-description"
            )
        )

        val updated = env.tagQueries.findTagByIdOptional(tag.id)
        assertNotNull(updated)
        assertEquals("new-name", updated.name)
        assertEquals("new-description", updated.description)
    }

    @Test
    fun `tag managed update key with same value is a no-op and does not fail`() {
        val env = createEnvironment()
        val groupKey = TagGroupKey("group-key")
        env.dispatch(TagAction.TagGroupCreate(groupKey, null, null))
        val group = env.tagQueries.findTagGroupByKeyOptional(groupKey)
        assertNotNull(group)
        val managedKey = TagKey("managed-key")
        env.dispatch(TagAction.TagGlobalCreate(TagGroupRef.ById(group.id), managedKey, null, null))
        val tag = env.tagQueries.findTagByKeyOptional(group.id, managedKey)
        assertNotNull(tag)

        // Why this test:
        // Updating a key to its current value should be accepted and must not trigger duplicate detection.
        env.dispatch(
            TagAction.TagGlobalUpdateKey(
                tagRef(tag.id),
                managedKey
            )
        )

        val found = env.tagQueries.findTagByIdOptional(tag.id)
        assertNotNull(found)
        assertEquals(managedKey, found.key)
    }

    @Test
    fun `tag managed delete`() {
        val env = createEnvironment()
        val groupKey = TagGroupKey("group-key")
        env.dispatch(TagAction.TagGroupCreate(groupKey, null, null))
        val group = env.tagQueries.findTagGroupByKeyOptional(groupKey)
        assertNotNull(group)

        val managedKey1 = TagKey("managed-key1")
        val managedKey2 = TagKey("managed-key2")
        env.dispatch(TagAction.TagGlobalCreate(TagGroupRef.ById(group.id), managedKey1, null, null))
        env.dispatch(TagAction.TagGlobalCreate(TagGroupRef.ById(group.id), managedKey2, null, null))

        env.dispatch(TagAction.TagGlobalDelete(tagRef(groupKey, managedKey1)))
        assertNull(env.tagQueries.findTagByKeyOptional(group.id, managedKey1))
        assertNotNull(env.tagQueries.findTagByKeyOptional(group.id, managedKey2))

        env.dispatch(TagAction.TagGlobalDelete(tagRef(groupKey, managedKey2)))
        assertNull(env.tagQueries.findTagByKeyOptional(group.id, managedKey2))
    }

    @Test
    fun `tag managed delete unknown tag then error`() {
        val env = createEnvironment()
        val groupKey = TagGroupKey("group-key")
        env.dispatch(TagAction.TagGroupCreate(groupKey, null, null))
        val group = env.tagQueries.findTagGroupByKeyOptional(groupKey)
        assertNotNull(group)

        // Why this test:
        // Delete must fail with a dedicated managed-tag-not-found error when the target tag is absent.
        assertFailsWith<TagManagedNotFoundException> {
            env.dispatch(
                TagAction.TagGlobalDelete(
                    tagRef(groupKey, TagKey("missing-tag"))
                )
            )
        }
    }

    @Test
    fun `tag managed operations work with group by key`() {
        val env = createEnvironment()
        val groupKey = TagGroupKey("group-key")
        env.dispatch(TagAction.TagGroupCreate(groupKey, null, null))
        val managedKey = TagKey("managed-key")

        // Why this test:
        // Group references can be passed by key in commands; we validate create/update/delete on this path.
        env.dispatch(
            TagAction.TagGlobalCreate(
                TagGroupRef.ByKey(groupKey),
                managedKey,
                "name",
                "description"
            )
        )

        env.dispatch(
            TagAction.TagGlobalUpdateName(
                tagRef(groupKey, managedKey), "new-name"
            )
        )
        env.dispatch(
            TagAction.TagGlobalUpdateDescription(
                tagRef(groupKey, managedKey), "new-description"
            )
        )

        val group = env.tagQueries.findTagGroupByKeyOptional(groupKey)
        assertNotNull(group)
        val found = env.tagQueries.findTagByKeyOptional(group.id, managedKey)
        assertNotNull(found)
        assertEquals("new-name", found.name)
        assertEquals("new-description", found.description)

        env.dispatch(TagAction.TagGlobalDelete(tagRef(groupKey, managedKey)))
        assertNull(env.tagQueries.findTagByKeyOptional(group.id, managedKey))
    }

    @Test
    fun `tag managed delete works with tag by id`() {
        val env = createEnvironment()
        val groupKey = TagGroupKey("group-key")
        env.dispatch(TagAction.TagGroupCreate(groupKey, null, null))
        val group = env.tagQueries.findTagGroupByKeyOptional(groupKey)
        assertNotNull(group)
        val managedKey = TagKey("managed-key")
        env.dispatch(TagAction.TagGlobalCreate(TagGroupRef.ById(group.id), managedKey, "name", "description"))
        val tag = env.tagQueries.findTagByKeyOptional(group.id, managedKey)
        assertNotNull(tag)

        // Why this test:
        // Tag references can be passed by id; we validate delete on this path.
        env.dispatch(TagAction.TagGlobalDelete(tagRef(tag.id)))
        assertNull(env.tagQueries.findTagByIdOptional(tag.id))
    }

    @Test
    fun `tag managed delete removes tag from objects`() {
        val env = createEnvironment()
        // Why this test:
        // Deleting a managed tag must propagate through TagCmds to all registered scope managers so they can
        // remove the deleted tag from every object they own (recipe and vehicle fixture domains here).
        val groupKey = TagGroupKey("governance")
        env.dispatch(TagAction.TagGroupCreate(groupKey, null, null))
        val group = env.tagQueries.findTagGroupByKeyOptional(groupKey)
        assertNotNull(group)

        env.dispatch(TagAction.TagGlobalCreate(TagGroupRef.ById(group.id), TagKey("shared"), null, null))
        val deletedTag = env.tagQueries.findTagByKeyOptional(group.id, TagKey("shared"))
        assertNotNull(deletedTag)

        env.dispatch(TagAction.TagGlobalCreate(TagGroupRef.ById(group.id), TagKey("keep"), null, null))
        val keptTag = env.tagQueries.findTagByKeyOptional(group.id, TagKey("keep"))
        assertNotNull(keptTag)

        val recipeId = sampleId()
        val ingredientId = sampleId()
        val stepId = sampleId()
        val vehicleId = sampleId()
        val partId = sampleId()

        // This test explicitly defines the objects that exist and their tag usage.
        env.recipeService.createRecipe(Recipe(recipeId, "Pasta", listOf(deletedTag.id, keptTag.id)))
        env.recipeService.createIngredient(Ingredient(ingredientId, recipeId, "Tomato", listOf(deletedTag.id)))
        env.recipeService.createRecipeStep(
            RecipeStep(
                stepId,
                recipeId,
                "Boil water",
                listOf(deletedTag.id, keptTag.id)
            )
        )
        env.vehicleService.createVehicle(Vehicle(vehicleId, "Truck", listOf(deletedTag.id)))
        env.vehicleService.createVehiclePart(VehiclePart(partId, vehicleId, "Wheel", listOf(keptTag.id, deletedTag.id)))

        env.dispatch(TagAction.TagGlobalDelete(tagRef(deletedTag.id)))

        assertEquals(listOf(keptTag.id), env.recipeService.findRecipeById(recipeId).tags)
        assertEquals(emptyList(), env.recipeService.findIngredientById(ingredientId).tags)
        assertEquals(listOf(keptTag.id), env.recipeService.findRecipeStepById(stepId).tags)
        assertEquals(emptyList(), env.vehicleService.findVehicleById(vehicleId).tags)
        assertEquals(listOf(keptTag.id), env.vehicleService.findVehiclePartById(partId).tags)
    }

    @Test
    fun `tag free delete removes tag from objects`() {
        val env = createEnvironment()
        val scopeRef = createRecipeScope(env.recipeService)
        // Why this test:
        // Deleting a free tag must propagate through the free-tag command path and still notify all registered
        // scope managers so they can clean their objects exactly like the managed path does.
        val deletedKey = TagKey("local-shared")
        val keptKey = TagKey("local-keep")

        env.dispatch(TagAction.TagLocalCreate(scopeRef, deletedKey, null, null))
        val deletedTag = env.tagQueries.findTagByRef(tagRef(scopeRef, deletedKey))
        env.dispatch(TagAction.TagLocalCreate(scopeRef, keptKey, null, null))
        val keptTag = env.tagQueries.findTagByRef(tagRef(scopeRef, keptKey))

        val recipeId = sampleId()
        val ingredientId = sampleId()
        val stepId = sampleId()
        val vehicleId = sampleId()
        val partId = sampleId()

        // This test explicitly defines the objects that exist and their tag usage.
        env.recipeService.createRecipe(Recipe(recipeId, "Soup", listOf(deletedTag.id, keptTag.id)))
        env.recipeService.createIngredient(Ingredient(ingredientId, recipeId, "Salt", listOf(deletedTag.id)))
        env.recipeService.createRecipeStep(RecipeStep(stepId, recipeId, "Mix", listOf(deletedTag.id, keptTag.id)))
        env.vehicleService.createVehicle(Vehicle(vehicleId, "Bike", listOf(deletedTag.id)))
        env.vehicleService.createVehiclePart(VehiclePart(partId, vehicleId, "Chain", listOf(keptTag.id, deletedTag.id)))

        env.dispatch(TagAction.TagLocalDelete(tagRef(deletedTag.id)))

        assertEquals(listOf(keptTag.id), env.recipeService.findRecipeById(recipeId).tags)
        assertEquals(emptyList(), env.recipeService.findIngredientById(ingredientId).tags)
        assertEquals(listOf(keptTag.id), env.recipeService.findRecipeStepById(stepId).tags)
        assertEquals(emptyList(), env.vehicleService.findVehicleById(vehicleId).tags)
        assertEquals(listOf(keptTag.id), env.vehicleService.findVehiclePartById(partId).tags)
    }

    @Test
    fun `tag delete blocked by scope manager veto`() {
        // Why this test:
        // Scope managers own local security rules and can veto tag deletion before storage mutation.
        // We verify the veto blocks the command and the tag remains present.
        val vetoManager = object : TagScopeManager {
            override val type: TagScopeType = TagScopeType("veto-test")

            override fun localScopeExists(scopeRef: TagScopeRef.Local): Boolean {
                // Not used in this test.
                return true
            }

        }
        val env = createEnvironment(
            extraScopeManagers = listOf(vetoManager), extraListeners = listOf(
                object : EventObserver<TagBeforeDeleteEvt> {
                    override fun onEvent(evt: TagBeforeDeleteEvt) {
                        throw SampleScopeManagerDeleteVetoException("Deletion vetoed by scope manager for tag ${evt.id.asString()}")
                    }

                }
            ))
        val scopeRef = createRecipeScope(service = env.recipeService)

        val key = TagKey("mykey")
        env.dispatch(TagAction.TagLocalCreate(scopeRef, key, null, null))
        val tag = env.tagQueries.findTagByRef(tagRef(scopeRef, key))

        val ex = assertFailsWith<SampleScopeManagerDeleteVetoException> {
            env.dispatch(TagAction.TagLocalDelete(tagRef(tag.id)))
        }
        assertContains(ex.message ?: "", "Deletion vetoed by scope manager")

        // The tag must still exist because the veto happens before the storage delete.
        assertNotNull(env.tagQueries.findTagByRefOptional(tagRef(tag.id)))
    }

    @Test
    fun `tag free create with missing scope then error`() {
        // Why this test:
        // A free tag is local to a scope, so creation must fail when the referenced scope does not exist.
        val env = createEnvironment()
        val missingScopeRef = recipeScopeRef(sampleId())

        assertFailsWith<TagScopeNotFoundException> {
            env.dispatch(TagAction.TagLocalCreate(missingScopeRef, TagKey("mykey"), null, null))
        }
    }

    @Test
    fun `tag free create with unknown scope type then error`() {
        // Why this test:
        // Free-tag creation depends on a scope manager for the local scope type. Unknown types must fail fast.
        val env = createEnvironment()
        val unknownScopeRef = TagScopeRef.Local(TagScopeType("unknown-scope"), TagScopeId(UuidUtils.generateV7()))

        assertFailsWith<TagScopeManagerNotFoundException> {
            env.dispatch(TagAction.TagLocalCreate(unknownScopeRef, TagKey("mykey"), null, null))
        }
    }

    @Test
    fun `tag free create with same key in different scopes is allowed`() {
        // Why this test:
        // Free-tag uniqueness is local to a scope, so the same key can exist in two different recipe scopes.
        val env = createEnvironment()
        val scopeRef1 = createRecipeScope(env.recipeService, "recipe-1")
        val scopeRef2 = createRecipeScope(env.recipeService, "recipe-2")
        val sharedKey = TagKey("shared-key")

        env.dispatch(TagAction.TagLocalCreate(scopeRef1, sharedKey, "name-1", null))
        env.dispatch(TagAction.TagLocalCreate(scopeRef2, sharedKey, "name-2", null))

        val found1 = env.tagQueries.findTagByRef(tagRef(scopeRef1, sharedKey))
        val found2 = env.tagQueries.findTagByRef(tagRef(scopeRef2, sharedKey))
        assertNotEquals(found1.id, found2.id)
        assertEquals(scopeRef1, found1.scope)
        assertEquals(scopeRef2, found2.scope)
    }

    @Test
    fun `tag free create with same key in same scope then error`() {
        // Why this test:
        // Free-tag uniqueness still applies inside one scope, so a duplicate key in the same local scope must fail.
        val env = createEnvironment()
        val scopeRef = createRecipeScope(env.recipeService)
        val sharedKey = TagKey("shared-key")
        env.dispatch(TagAction.TagLocalCreate(scopeRef, sharedKey, null, null))

        assertFailsWith<TagFreeDuplicateKeyException> {
            env.dispatch(TagAction.TagLocalCreate(scopeRef, sharedKey, null, null))
        }
    }

    @Test
    fun `tag managed delete blocked by scope manager veto`() {
        // Why this test:
        // Managed-tag deletion uses a different command path than free deletion but must still honor scope-manager vetoes.
        val vetoManager = object : TagScopeManager {
            override val type: TagScopeType = TagScopeType("veto-test")

            override fun localScopeExists(scopeRef: TagScopeRef.Local): Boolean {
                return true
            }

        }
        val env = createEnvironment(
            extraScopeManagers = listOf(vetoManager),
            extraListeners = listOf(object : EventObserver<TagBeforeDeleteEvt> {
                override fun onEvent(evt: TagBeforeDeleteEvt) {
                    throw SampleScopeManagerDeleteVetoException("Deletion vetoed by scope manager for tag ${evt.id.asString()}")
                }

            })
        )
        val groupKey = TagGroupKey("group-key")
        env.dispatch(TagAction.TagGroupCreate(groupKey, null, null))
        val group = env.tagQueries.findTagGroupByKeyOptional(groupKey)
        assertNotNull(group)
        val managedKey = TagKey("managed-key")
        env.dispatch(TagAction.TagGlobalCreate(TagGroupRef.ById(group.id), managedKey, null, null))
        val managedTag = env.tagQueries.findTagByKeyOptional(group.id, managedKey)
        assertNotNull(managedTag)

        val ex = assertFailsWith<SampleScopeManagerDeleteVetoException> {
            env.dispatch(TagAction.TagGlobalDelete(tagRef(managedTag.id)))
        }
        assertContains(ex.message ?: "", "Deletion vetoed by scope manager")

        // The managed tag must still exist because the veto happens before the storage delete.
        assertNotNull(env.tagQueries.findTagByRefOptional(tagRef(managedTag.id)))
    }

    @Test
    fun `tag free command with global ref then error`() {
        // Why this test:
        // Free-tag commands must reject a managed/global key reference, even if the tag itself exists.
        val env = createEnvironment()
        val groupKey = TagGroupKey("group-key")
        env.dispatch(TagAction.TagGroupCreate(groupKey, null, null))
        val group = env.tagQueries.findTagGroupByKeyOptional(groupKey)
        assertNotNull(group)
        val managedKey = TagKey("managed-key")
        env.dispatch(TagAction.TagGlobalCreate(TagGroupRef.ById(group.id), managedKey, null, null))

        assertFailsWith<TagFreeCommandIncompatibleTagRefException> {
            env.dispatch(TagAction.TagLocalUpdateName(tagRef(groupKey, managedKey), "new-name"))
        }
    }

    @Test
    fun `tag managed command with local ref then error`() {
        // Why this test:
        // Managed-tag commands must reject a local/free key reference because it does not identify a managed tag.
        val env = createEnvironment()
        val scopeRef = createRecipeScope(env.recipeService)
        val freeKey = TagKey("free-key")
        env.dispatch(TagAction.TagLocalCreate(scopeRef, freeKey, null, null))

        assertFailsWith<TagManagedCommandIncompatibleTagRefException> {
            env.dispatch(TagAction.TagGlobalUpdateDescription(tagRef(scopeRef, freeKey), "new-description"))
        }
    }

    @Test
    fun `recipe delete deletes tags of recipe scope only`() {
        // Why this test:
        // Deleting a recipe emits a scope-delete event. tags-core must remove only free tags of that recipe scope.
        val env = createEnvironment()
        val recipeId1 = sampleId()
        val recipeId2 = sampleId()
        val vehicleId = sampleId()
        env.recipeService.createRecipe(Recipe(recipeId1, "recipe-1", emptyList()))
        env.recipeService.createRecipe(Recipe(recipeId2, "recipe-2", emptyList()))
        env.vehicleService.createVehicle(Vehicle(vehicleId, "vehicle-1", emptyList()))

        val recipeScope1 = recipeScopeRef(recipeId1)
        val recipeScope2 = recipeScopeRef(recipeId2)
        val vehicleScope = vehicleScopeRef(vehicleId)

        env.dispatch(TagAction.TagLocalCreate(recipeScope1, TagKey("recipe-1-tag"), null, null))
        env.dispatch(TagAction.TagLocalCreate(recipeScope2, TagKey("recipe-2-tag"), null, null))
        env.dispatch(TagAction.TagLocalCreate(vehicleScope, TagKey("vehicle-tag"), null, null))

        env.recipeService.deleteRecipe(recipeId1)

        assertNull(env.tagQueries.findTagByRefOptional(tagRef(recipeScope1, TagKey("recipe-1-tag"))))
        assertNotNull(env.tagQueries.findTagByRefOptional(tagRef(recipeScope2, TagKey("recipe-2-tag"))))
        assertNotNull(env.tagQueries.findTagByRefOptional(tagRef(vehicleScope, TagKey("vehicle-tag"))))
    }

    @Test
    fun `vehicle delete deletes tags of vehicle scope only`() {
        // Why this test:
        // Deleting a vehicle emits a scope-delete event. tags-core must remove only free tags of that vehicle scope.
        val env = createEnvironment()
        val vehicleId1 = sampleId()
        val vehicleId2 = sampleId()
        val recipeId = sampleId()
        env.vehicleService.createVehicle(Vehicle(vehicleId1, "vehicle-1", emptyList()))
        env.vehicleService.createVehicle(Vehicle(vehicleId2, "vehicle-2", emptyList()))
        env.recipeService.createRecipe(Recipe(recipeId, "recipe-1", emptyList()))

        val vehicleScope1 = vehicleScopeRef(vehicleId1)
        val vehicleScope2 = vehicleScopeRef(vehicleId2)
        val recipeScope = recipeScopeRef(recipeId)

        env.dispatch(TagAction.TagLocalCreate(vehicleScope1, TagKey("vehicle-1-tag"), null, null))
        env.dispatch(TagAction.TagLocalCreate(vehicleScope2, TagKey("vehicle-2-tag"), null, null))
        env.dispatch(TagAction.TagLocalCreate(recipeScope, TagKey("recipe-tag"), null, null))

        env.vehicleService.deleteVehicle(vehicleId1)

        assertNull(env.tagQueries.findTagByRefOptional(tagRef(vehicleScope1, TagKey("vehicle-1-tag"))))
        assertNotNull(env.tagQueries.findTagByRefOptional(tagRef(vehicleScope2, TagKey("vehicle-2-tag"))))
        assertNotNull(env.tagQueries.findTagByRefOptional(tagRef(recipeScope, TagKey("recipe-tag"))))
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Tag Search
    // -----------------------------------------------------------------------------------------------------------------

    private fun createEnvironment(
        extraScopeManagers: List<TagScopeManager> = emptyList(),
        extraListeners: List<EventObserver<TagBeforeDeleteEvt>> = emptyList()
    ): TagTestEnv = TagTestEnv(extraScopeManagers, extraListeners)



}
