package io.medatarun.tags.core

import io.medatarun.platform.kernel.EventObserver
import io.medatarun.tags.core.actions.TagAction
import io.medatarun.tags.core.domain.*
import io.medatarun.tags.core.fixtures.*
import io.medatarun.tags.core.fixtures.SampleId.Companion.sampleId
import io.medatarun.tags.core.ports.needs.TagScopeManager
import kotlinx.serialization.json.*
import kotlin.test.*

class TagSearchTest {

    @Test
    fun `tag search without scope filter returns global and local tags with expected scope payload`() {
        val env = createEnvironment()
        val recipeScope = createRecipeScope(env.recipeService)
        val vehicleId = sampleId()
        env.vehicleService.createVehicle(Vehicle(vehicleId, "vehicle-1", emptyList()))
        val vehicleScope = vehicleScopeRef(vehicleId)

        val groupKey = TagGroupKey("global-group")
        env.dispatch(TagAction.TagGroupCreate(groupKey, null, null))
        val group = env.tagQueries.findTagGroupByKeyOptional(groupKey)
        assertNotNull(group)

        env.dispatch(TagAction.TagGlobalCreate(TagGroupRef.ById(group.id), TagKey("global-tag"), null, null))
        env.dispatch(TagAction.TagLocalCreate(recipeScope, TagKey("recipe-tag"), null, null))
        env.dispatch(TagAction.TagLocalCreate(vehicleScope, TagKey("vehicle-tag"), null, null))

        val items = tagSearchItems(env.dispatch(TagAction.TagSearch(filters = null)))
        assertEquals(3, items.size)

        val globalItem = findTagSearchItemByKey(items, "global-tag")
        assertNotNull(globalItem)
        assertEquals(group.id.asString(), jsonString(globalItem, "groupId"))
        val globalScopeRef = globalItem["tagScopeRef"]!!.jsonObject
        assertEquals("global", jsonString(globalScopeRef, "type"))
        assertTrue(globalScopeRef["id"] is JsonNull)

        val recipeItem = findTagSearchItemByKey(items, "recipe-tag")
        assertNotNull(recipeItem)
        assertTrue(recipeItem["groupId"] is JsonNull)
        val recipeScopeRef = recipeItem["tagScopeRef"]!!.jsonObject
        assertEquals(recipeScope.type.value, jsonString(recipeScopeRef, "type"))
        assertEquals(recipeScope.scopeId.asString(), jsonString(recipeScopeRef, "id"))

        val vehicleItem = findTagSearchItemByKey(items, "vehicle-tag")
        assertNotNull(vehicleItem)
        assertTrue(vehicleItem["groupId"] is JsonNull)
        val vehicleScopeRefJson = vehicleItem["tagScopeRef"]!!.jsonObject
        assertEquals(vehicleScope.type.value, jsonString(vehicleScopeRefJson, "type"))
        assertEquals(vehicleScope.scopeId.asString(), jsonString(vehicleScopeRefJson, "id"))
    }

    @Test
    fun `tag search with global scope filter returns only global tags`() {
        val env = createEnvironment()
        val recipeScope = createRecipeScope(env.recipeService)
        val groupKey = TagGroupKey("global-group")
        env.dispatch(TagAction.TagGroupCreate(groupKey, null, null))
        val group = env.tagQueries.findTagGroupByKeyOptional(groupKey)
        assertNotNull(group)

        env.dispatch(TagAction.TagGlobalCreate(TagGroupRef.ById(group.id), TagKey("global-tag"), null, null))
        env.dispatch(TagAction.TagLocalCreate(recipeScope, TagKey("recipe-tag"), null, null))

        val items = tagSearchItems(env.dispatch(TagAction.TagSearch(filters = scopeIsFilter(TagScopeRef.Global))))
        assertEquals(1, items.size)
        val globalItem = findTagSearchItemByKey(items, "global-tag")
        assertNotNull(globalItem)
        val globalScopeRef = globalItem["tagScopeRef"]!!.jsonObject
        assertEquals("global", jsonString(globalScopeRef, "type"))
        assertTrue(globalScopeRef["id"] is JsonNull)
    }

    @Test
    fun `tag search with local scope filter returns only tags of the requested scope`() {
        val env = createEnvironment()
        val recipeScope = createRecipeScope(env.recipeService, "recipe-1")
        val vehicleId = sampleId()
        env.vehicleService.createVehicle(Vehicle(vehicleId, "vehicle-1", emptyList()))
        val vehicleScope = vehicleScopeRef(vehicleId)

        env.dispatch(TagAction.TagLocalCreate(recipeScope, TagKey("recipe-tag"), null, null))
        env.dispatch(TagAction.TagLocalCreate(vehicleScope, TagKey("vehicle-tag"), null, null))

        val items = tagSearchItems(env.dispatch(TagAction.TagSearch(filters = scopeIsFilter(recipeScope))))
        assertEquals(1, items.size)
        val recipeItem = findTagSearchItemByKey(items, "recipe-tag")
        assertNotNull(recipeItem)
        val recipeScopeRef = recipeItem["tagScopeRef"]!!.jsonObject
        assertEquals(recipeScope.type.value, jsonString(recipeScopeRef, "type"))
        assertEquals(recipeScope.scopeId!!.asString(), jsonString(recipeScopeRef, "id"))
        assertNull(findTagSearchItemByKey(items, "vehicle-tag"))
    }

    @Test
    fun `tag search with local scope filter fails when scope does not exist`() {
        val env = createEnvironment()
        val missingRecipeScope = recipeScopeRef(sampleId())

        assertFailsWith<TagScopeNotFoundException> {
            env.dispatch(TagAction.TagSearch(filters = scopeIsFilter(missingRecipeScope)))
        }
    }

    @Test
    fun `tag search with local scope filter fails when scope type is unknown`() {
        val env = createEnvironment()
        val unknownScope = TagScopeRef.Local(TagScopeType("unknown-scope"), TagScopeId(sampleId().value))

        assertFailsWith<TagScopeManagerNotFoundException> {
            env.dispatch(TagAction.TagSearch(filters = scopeIsFilter(unknownScope)))
        }
    }

    private fun createEnvironment(
        extraScopeManagers: List<TagScopeManager> = emptyList(),
        extraListeners: List<EventObserver<TagBeforeDeleteEvt>> = emptyList()
    ): TagTestEnv = TagTestEnv(extraScopeManagers, extraListeners)

    private fun tagSearchItems(result: Any?): JsonArray {
        val json = result as? JsonObject ?: fail("TagSearch response should be a JsonObject")
        return json["items"]?.jsonArray ?: fail("TagSearch response should contain items")
    }

    private fun findTagSearchItemByKey(items: JsonArray, key: String): JsonObject? {
        return items.firstOrNull {
            val current = it.jsonObject["key"] as? JsonPrimitive
            current?.content == key
        }?.jsonObject
    }

    private fun jsonString(json: JsonObject, key: String): String {
        return json[key]?.jsonPrimitive?.content ?: fail("Missing string field '$key'")
    }

    private fun scopeIsFilter(scopeRef: TagScopeRef): TagSearchFilters {
        return TagSearchFilters(
            operator = TagSearchFiltersLogicalOperator.AND,
            items = listOf(TagSearchFilterScopeRef.Is(scopeRef))
        )
    }

    private fun createRecipeScope(
        service: RecipeService,
        name: String = "scope-root",
    ): TagScopeRef.Local {
        val recipeId = sampleId()
        service.createRecipe(Recipe(recipeId, name, emptyList()))
        return recipeScopeRef(recipeId)
    }

}
