package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.domain.*
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.ports.exposed.ModelQueries
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@EnableDatabaseTests
class Model_Delete_Test {

    @Test
    fun `delete model fails if model Id not found in any storage`() {
        val env = createEnv()
        env.dispatch(
            ModelAction.Model_Create(
                ModelKey("recipe"),
                LocalizedTextNotLocalized("Recipe"),
                null,
                ModelVersion("0.0.1")
            )
        )
        env.dispatch(
            ModelAction.Model_Create(
                ModelKey("movie"),
                LocalizedTextNotLocalized("Movie"),
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
        val env = createEnv()
        val query: ModelQueries = env.queries

        env.dispatch(
            ModelAction.Model_Create(
                ModelKey("recipe"),
                LocalizedTextNotLocalized("Recipe"),
                null,
                ModelVersion("0.0.1")
            )
        )
        env.dispatch(
            ModelAction.Model_Create(
                ModelKey("movie"),
                LocalizedTextNotLocalized("Movie"),
                null,
                ModelVersion("0.1.0")
            )
        )
        env.dispatch(
            ModelAction.Model_Create(
                ModelKey("vehicle"),
                LocalizedTextNotLocalized("Vehicle"),
                null,
                ModelVersion("0.0.1")
            )
        )
        env.dispatch(
            ModelAction.Model_Create(
                ModelKey("videogame"),
                LocalizedTextNotLocalized("Videogame"),
                null,
                ModelVersion("0.1.0")
            )
        )

        val recipeUiSearchTag = env.createLocalTagInModelScope(modelRef("recipe"), "ui-search")
        val vehicleImportedTag = env.createLocalTagInModelScope(modelRef("vehicle"), "imported")
        val movieUiResultTag = env.createLocalTagInModelScope(modelRef("movie"), "ui-result")
        val videogameImportedTag = env.createLocalTagInModelScope(modelRef("videogame"), "imported")
        val globalSecurityInternalTag = env.createGlobalTag("security", "internal")

        env.dispatch(ModelAction.Model_Delete(modelRef("recipe")))
        assertNull(query.findModelOptional(modelRef("recipe")))
        assertNull(env.tagQueries.findTagByIdOptional(recipeUiSearchTag.id))
        assertNotNull(env.tagQueries.findTagByIdOptional(vehicleImportedTag.id))
        assertNotNull(env.tagQueries.findTagByIdOptional(movieUiResultTag.id))
        assertNotNull(env.tagQueries.findTagByIdOptional(videogameImportedTag.id))
        assertNotNull(env.tagQueries.findTagByIdOptional(globalSecurityInternalTag.id))

        assertNotNull(query.findModelOptional(modelRef("movie")))
        assertNotNull(query.findModelOptional(modelRef("vehicle")))
        assertNotNull(query.findModelOptional(modelRef("videogame")))

        env.dispatch(ModelAction.Model_Delete(modelRefKey("vehicle")))
        assertNull(query.findModelOptional(modelRef("vehicle")))
        assertNull(env.tagQueries.findTagByIdOptional(recipeUiSearchTag.id))
        assertNull(env.tagQueries.findTagByIdOptional(vehicleImportedTag.id))
        assertNotNull(env.tagQueries.findTagByIdOptional(movieUiResultTag.id))
        assertNotNull(env.tagQueries.findTagByIdOptional(videogameImportedTag.id))
        assertNotNull(env.tagQueries.findTagByIdOptional(globalSecurityInternalTag.id))
        assertNotNull(query.findModelOptional(modelRef("movie")))
        assertNotNull(query.findModelOptional(modelRef("videogame")))

        env.dispatch(ModelAction.Model_Delete(modelRef("movie")))
        assertNull(query.findModelOptional(modelRef("movie")))
        assertNull(env.tagQueries.findTagByIdOptional(recipeUiSearchTag.id))
        assertNull(env.tagQueries.findTagByIdOptional(vehicleImportedTag.id))
        assertNull(env.tagQueries.findTagByIdOptional(movieUiResultTag.id))
        assertNotNull(env.tagQueries.findTagByIdOptional(videogameImportedTag.id))
        assertNotNull(env.tagQueries.findTagByIdOptional(globalSecurityInternalTag.id))
        assertNotNull(query.findModelOptional(modelRef("videogame")))

        env.dispatch(ModelAction.Model_Delete(modelRefKey("videogame")))
        env.replayWithRebuild {
            assertNull(query.findModelOptional(modelRef("videogame")))
            assertNull(env.tagQueries.findTagByIdOptional(recipeUiSearchTag.id))
            assertNull(env.tagQueries.findTagByIdOptional(vehicleImportedTag.id))
            assertNull(env.tagQueries.findTagByIdOptional(movieUiResultTag.id))
            assertNull(env.tagQueries.findTagByIdOptional(videogameImportedTag.id))
            assertNotNull(env.tagQueries.findTagByIdOptional(globalSecurityInternalTag.id))
        }

    }
}
