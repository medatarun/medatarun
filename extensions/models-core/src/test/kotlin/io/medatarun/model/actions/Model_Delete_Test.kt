package io.medatarun.model.actions

import io.medatarun.model.actions.ModelAction
import io.medatarun.model.domain.LocalizedTextNotLocalized
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.ModelNotFoundException
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.ModelVersion
import io.medatarun.model.domain.modelRef
import io.medatarun.model.ports.exposed.ModelQueries
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class Model_Delete_Test {

    @Test
    fun `delete model fails if model Id not found in any storage`() {
        val env = _root_ide_package_.io.medatarun.model.actions.createEnv()
        env.dispatch(
            ModelAction.Model_Create(
                ModelKey("m-to-delete-1"),
                LocalizedTextNotLocalized("Model to delete"),
                null,
                ModelVersion("0.0.1")
            )
        )
        env.dispatch(
            ModelAction.Model_Create(
                ModelKey("m-to-delete-2"),
                LocalizedTextNotLocalized("Model to delete 2"),
                null,
                ModelVersion("0.0.1")
            )
        )
        assertThrows<ModelNotFoundException> {
            env.dispatch(ModelAction.Model_Delete(modelRefKey("m-to-delete-3")))
        }
    }

    @Test
    fun `delete model removes it from storage`() {
        val env = _root_ide_package_.io.medatarun.model.actions.createEnv()
        val query: ModelQueries = env.queries

        env.dispatch(
            ModelAction.Model_Create(
                ModelKey("m-to-delete-1"),
                LocalizedTextNotLocalized("Model to delete"),
                null,
                ModelVersion("0.0.1")
            )
        )
        env.dispatch(
            ModelAction.Model_Create(
                ModelKey("m-to-keep-1"),
                LocalizedTextNotLocalized("Model to preserve"),
                null,
                ModelVersion("0.1.0")
            )
        )
        env.dispatch(
            ModelAction.Model_Create(
                ModelKey("m-to-delete-2"),
                LocalizedTextNotLocalized("Model to delete 2"),
                null,
                ModelVersion("0.0.1")
            )
        )
        env.dispatch(
            ModelAction.Model_Create(
                ModelKey("m-to-keep-2"),
                LocalizedTextNotLocalized("Model to preserve 2"),
                null,
                ModelVersion("0.1.0")
            )
        )

        env.dispatch(ModelAction.Model_Delete(modelRef("m-to-delete-1")))
        assertNull(query.findModelOptional(modelRef("m-to-delete-1")))

        assertNotNull(query.findModelOptional(modelRef("m-to-keep-1")))
        assertNotNull(query.findModelOptional(modelRef("m-to-delete-2")))
        assertNotNull(query.findModelOptional(modelRef("m-to-keep-2")))

        env.dispatch(ModelAction.Model_Delete(modelRefKey("m-to-delete-2")))
        assertNull(query.findModelOptional(modelRef("m-to-delete-2")))
        assertNotNull(query.findModelOptional(modelRef("m-to-keep-1")))
        assertNotNull(query.findModelOptional(modelRef("m-to-keep-2")))

        env.dispatch(ModelAction.Model_Delete(modelRef("m-to-keep-1")))
        assertNull(query.findModelOptional(modelRef("m-to-keep-1")))
        assertNotNull(query.findModelOptional(modelRef("m-to-keep-2")))

        env.dispatch(ModelAction.Model_Delete(modelRefKey("m-to-keep-2")))
        assertNull(query.findModelOptional(modelRef("m-to-keep-2")))

    }
}