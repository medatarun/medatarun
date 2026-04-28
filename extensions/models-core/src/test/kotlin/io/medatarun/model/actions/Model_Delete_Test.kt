package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.domain.*
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.fixtures.ModelTestEnv
import io.medatarun.model.ports.exposed.ModelQueries
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@EnableDatabaseTests
class Model_Delete_Test {

    @Test
    fun `delete model fails if model Id not found in any storage`() {
        val env = ModelTestEnv()
        env.dispatch(
            ModelAction.Model_Create(
                ModelKey("recipe"),
                LocalizedText("Recipe"),
                null,
                ModelVersion("0.0.1")
            )
        )
        env.dispatch(
            ModelAction.Model_Create(
                ModelKey("movie"),
                LocalizedText("Movie"),
                null,
                ModelVersion("0.0.1")
            )
        )
        assertThrows<ModelNotFoundException> {
            env.dispatch(ModelAction.Model_Delete(modelRefKey("vehicle")))
        }
    }

    @Test
    fun `delete model removes it from storage`() {
        val env = ModelTestEnv()
        val query: ModelQueries = env.queries

        env.dispatch(
            ModelAction.Model_Create(
                ModelKey("recipe"),
                LocalizedText("Recipe"),
                null,
                ModelVersion("0.0.1")
            )
        )
        env.dispatch(
            ModelAction.Model_Create(
                ModelKey("movie"),
                LocalizedText("Movie"),
                null,
                ModelVersion("0.1.0")
            )
        )
        env.dispatch(
            ModelAction.Model_Create(
                ModelKey("vehicle"),
                LocalizedText("Vehicle"),
                null,
                ModelVersion("0.0.1")
            )
        )
        env.dispatch(
            ModelAction.Model_Create(
                ModelKey("videogame"),
                LocalizedText("Videogame"),
                null,
                ModelVersion("0.1.0")
            )
        )

        val recipeUiSearchTag = env.createLocalTagInModelScope(modelRefKey("recipe"), "ui-search")
        val vehicleImportedTag = env.createLocalTagInModelScope(modelRefKey("vehicle"), "imported")
        val movieUiResultTag = env.createLocalTagInModelScope(modelRefKey("movie"), "ui-result")
        val videogameImportedTag = env.createLocalTagInModelScope(modelRefKey("videogame"), "imported")
        val globalSecurityInternalTag = env.createGlobalTag("security", "internal")

        env.dispatch(ModelAction.Model_Delete(modelRefKey("recipe")))
        assertNull(query.findModelAggregateOptional(modelRefKey("recipe")))
        assertNull(env.tagQueries.findTagByIdOptional(recipeUiSearchTag.id))
        assertNotNull(env.tagQueries.findTagByIdOptional(vehicleImportedTag.id))
        assertNotNull(env.tagQueries.findTagByIdOptional(movieUiResultTag.id))
        assertNotNull(env.tagQueries.findTagByIdOptional(videogameImportedTag.id))
        assertNotNull(env.tagQueries.findTagByIdOptional(globalSecurityInternalTag.id))

        assertNotNull(query.findModelAggregateOptional(modelRefKey("movie")))
        assertNotNull(query.findModelAggregateOptional(modelRefKey("vehicle")))
        assertNotNull(query.findModelAggregateOptional(modelRefKey("videogame")))

        env.dispatch(ModelAction.Model_Delete(modelRefKey("vehicle")))
        assertNull(query.findModelAggregateOptional(modelRefKey("vehicle")))
        assertNull(env.tagQueries.findTagByIdOptional(recipeUiSearchTag.id))
        assertNull(env.tagQueries.findTagByIdOptional(vehicleImportedTag.id))
        assertNotNull(env.tagQueries.findTagByIdOptional(movieUiResultTag.id))
        assertNotNull(env.tagQueries.findTagByIdOptional(videogameImportedTag.id))
        assertNotNull(env.tagQueries.findTagByIdOptional(globalSecurityInternalTag.id))
        assertNotNull(query.findModelAggregateOptional(modelRefKey("movie")))
        assertNotNull(query.findModelAggregateOptional(modelRefKey("videogame")))

        env.dispatch(ModelAction.Model_Delete(modelRefKey("movie")))
        assertNull(query.findModelAggregateOptional(modelRefKey("movie")))
        assertNull(env.tagQueries.findTagByIdOptional(recipeUiSearchTag.id))
        assertNull(env.tagQueries.findTagByIdOptional(vehicleImportedTag.id))
        assertNull(env.tagQueries.findTagByIdOptional(movieUiResultTag.id))
        assertNotNull(env.tagQueries.findTagByIdOptional(videogameImportedTag.id))
        assertNotNull(env.tagQueries.findTagByIdOptional(globalSecurityInternalTag.id))
        assertNotNull(query.findModelAggregateOptional(modelRefKey("videogame")))

        env.dispatch(ModelAction.Model_Delete(modelRefKey("videogame")))
        env.replayWithRebuild {
            assertNull(query.findModelAggregateOptional(modelRefKey("videogame")))
            assertNull(env.tagQueries.findTagByIdOptional(recipeUiSearchTag.id))
            assertNull(env.tagQueries.findTagByIdOptional(vehicleImportedTag.id))
            assertNull(env.tagQueries.findTagByIdOptional(movieUiResultTag.id))
            assertNull(env.tagQueries.findTagByIdOptional(videogameImportedTag.id))
            assertNotNull(env.tagQueries.findTagByIdOptional(globalSecurityInternalTag.id))
        }

    }
}
