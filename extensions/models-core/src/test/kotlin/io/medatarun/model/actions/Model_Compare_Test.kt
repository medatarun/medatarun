package io.medatarun.model.actions

import io.medatarun.model.actions.compare.ModelCompareDto
import io.medatarun.model.domain.*
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.diff.ModelDiffScope
import io.medatarun.model.domain.fixtures.ModelTestEnv
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class Model_Compare_Test {

    @Test
    fun `compare action returns model diff result`() {
        val env = ModelTestEnv()
        val leftKey = ModelKey("left-model")
        val rightKey = ModelKey("right-model")
        createBaseModel(env, leftKey)
        createBaseModel(env, rightKey)

        val result = env.dispatch(
            ModelAction.Compare(
                leftModelRef = modelRefKey(leftKey),
                rightModelRef = modelRefKey(rightKey),
                scope = ModelDiffScope.STRUCTURAL
            )
        )

        assertIs<ModelCompareDto>(result)
    }

    @Test
    fun `compare structural includes attribute optional change`() {
        val env = ModelTestEnv()
        val leftKey = ModelKey("left-model-optional")
        val rightKey = ModelKey("right-model-optional")
        createBaseModel(env, leftKey)
        createBaseModel(env, rightKey)

        env.dispatch(
            ModelAction.EntityAttribute_UpdateOptional(
                modelRef = modelRefKey(rightKey),
                entityRef = EntityRef.ByKey(EntityKey("Customer")),
                attributeRef = EntityAttributeRef.ByKey(AttributeKey("email")),
                value = true
            )
        )

        val result = env.dispatch(
            ModelAction.Compare(
                leftModelRef = modelRefKey(leftKey),
                rightModelRef = modelRefKey(rightKey),
                scope = ModelDiffScope.STRUCTURAL
            )
        )
        val diff = assertIs<ModelCompareDto>(result)

        val hasModifiedEmail = diff.entries.any { entry ->
            entry.status == "MODIFIED" &&
                    entry.objectType == "entityAttribute" &&
                    entry.entityKey == "Customer" &&
                    entry.attributeKey == "email"
        }
        assertTrue(hasModifiedEmail)
    }

    @Test
    fun `compare complete includes description changes but structural ignores them`() {
        val env = ModelTestEnv()
        val leftKey = ModelKey("left-model-content")
        val rightKey = ModelKey("right-model-content")
        createBaseModel(env, leftKey)
        createBaseModel(env, rightKey)

        env.dispatch(
            ModelAction.EntityAttribute_UpdateDescription(
                modelRef = modelRefKey(rightKey),
                entityRef = EntityRef.ByKey(EntityKey("Customer")),
                attributeRef = EntityAttributeRef.ByKey(AttributeKey("email")),
                value = LocalizedMarkdownNotLocalized("email used for notifications")
            )
        )

        val structuralResult = env.dispatch(
            ModelAction.Compare(
                leftModelRef = modelRefKey(leftKey),
                rightModelRef = modelRefKey(rightKey),
                scope = ModelDiffScope.STRUCTURAL
            )
        )
        val structuralDiff = assertIs<ModelCompareDto>(structuralResult)
        val structuralEntriesWithoutModel = structuralDiff.entries.filter { it.objectType != "model" }
        assertEquals(0, structuralEntriesWithoutModel.size)

        val completeResult = env.dispatch(
            ModelAction.Compare(
                leftModelRef = modelRefKey(leftKey),
                rightModelRef = modelRefKey(rightKey),
                scope = ModelDiffScope.COMPLETE
            )
        )
        val completeDiff = assertIs<ModelCompareDto>(completeResult)
        val hasModifiedEmail = completeDiff.entries.any { entry ->
            entry.status == "MODIFIED" &&
                    entry.objectType == "entityAttribute" &&
                    entry.entityKey == "Customer" &&
                    entry.attributeKey == "email"
        }
        assertTrue(hasModifiedEmail)
    }

    private fun createBaseModel(env: ModelTestEnv, modelKey: ModelKey) {
        val modelRef = modelRefKey(modelKey)
        val entityRef = EntityRef.ByKey(EntityKey("Customer"))

        env.dispatch(
            ModelAction.Model_Create(
                modelKey = modelKey,
                name = LocalizedTextNotLocalized("Model ${modelKey.value}"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )
        env.dispatch(
            ModelAction.Type_Create(
                modelRef = modelRef,
                typeKey = TypeKey("String"),
                name = null,
                description = null
            )
        )
        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = modelRef,
                entityKey = EntityKey("Customer"),
                name = LocalizedTextNotLocalized("Customer"),
                description = null,
                identityAttributeKey = AttributeKey("id"),
                identityAttributeType = typeRef("String"),
                identityAttributeName = null,
                documentationHome = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeKey = AttributeKey("email"),
                type = typeRef("String"),
                optional = false,
                name = LocalizedTextNotLocalized("Email"),
                description = null
            )
        )
    }
}
