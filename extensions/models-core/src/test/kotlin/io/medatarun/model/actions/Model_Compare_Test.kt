package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.actions.compare.ModelCompareDto
import io.medatarun.model.domain.*
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.diff.ModelDiffScope
import io.medatarun.model.domain.fixtures.ModelTestEnv
import kotlin.test.*

@EnableDatabaseTests
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
                leftModelVersion = null,
                rightModelRef = modelRefKey(rightKey),
                rightModelVersion = null,
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
                leftModelVersion = null,
                rightModelRef = modelRefKey(rightKey),
                rightModelVersion = null,
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
                leftModelVersion = null,
                rightModelRef = modelRefKey(rightKey),
                rightModelVersion = null,
                scope = ModelDiffScope.STRUCTURAL
            )
        )
        val structuralDiff = assertIs<ModelCompareDto>(structuralResult)
        val structuralEntriesWithoutModel = structuralDiff.entries.filter { it.objectType != "model" }
        assertEquals(0, structuralEntriesWithoutModel.size)

        val completeResult = env.dispatch(
            ModelAction.Compare(
                leftModelRef = modelRefKey(leftKey),
                leftModelVersion = null,
                rightModelRef = modelRefKey(rightKey),
                rightModelVersion = null,
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

    @Test
    fun `compare without requested version uses current model state`() {
        val env = ModelTestEnv()
        val leftKey = ModelKey("left-model-current-version")
        val rightKey = ModelKey("right-model-current-version")
        createBaseModel(env, leftKey)
        createBaseModel(env, rightKey)

        env.dispatch(
            ModelAction.EntityAttribute_UpdateOptional(
                modelRef = modelRefKey(leftKey),
                entityRef = EntityRef.ByKey(EntityKey("Customer")),
                attributeRef = EntityAttributeRef.ByKey(AttributeKey("email")),
                value = true
            )
        )
        env.dispatch(
            ModelAction.Model_Release(
                modelRef = modelRefKey(leftKey),
                value = ModelVersion("2.0.0")
            )
        )

        val result = env.dispatch(
            ModelAction.Compare(
                leftModelRef = modelRefKey(leftKey),
                leftModelVersion = null,
                rightModelRef = modelRefKey(rightKey),
                rightModelVersion = null,
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
        assertEquals("2.0.0", diff.left.modelVersion)
    }

    @Test
    fun `compare with requested version uses released snapshot`() {
        val env = ModelTestEnv()
        val leftKey = ModelKey("left-model-requested-version")
        val rightKey = ModelKey("right-model-requested-version")
        createBaseModel(env, leftKey)
        createBaseModel(env, rightKey)

        env.dispatch(
            ModelAction.EntityAttribute_UpdateOptional(
                modelRef = modelRefKey(leftKey),
                entityRef = EntityRef.ByKey(EntityKey("Customer")),
                attributeRef = EntityAttributeRef.ByKey(AttributeKey("email")),
                value = true
            )
        )
        env.dispatch(
            ModelAction.Model_Release(
                modelRef = modelRefKey(leftKey),
                value = ModelVersion("2.0.0")
            )
        )
        env.dispatch(
            ModelAction.Model_Release(
                modelRef = modelRefKey(rightKey),
                value = ModelVersion("2.0.0")
            )
        )

        val result = env.dispatch(
            ModelAction.Compare(
                leftModelRef = modelRefKey(leftKey),
                leftModelVersion = ModelVersion("1.0.0"),
                rightModelRef = modelRefKey(rightKey),
                rightModelVersion = ModelVersion("1.0.0"),
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
        assertFalse(hasModifiedEmail)
        assertEquals("1.0.0", diff.left.modelVersion)
        assertEquals("1.0.0", diff.right.modelVersion)
    }

    @Test
    fun `compare with requested right version ignores changes made after that release`() {
        val env = ModelTestEnv()
        val leftKey = ModelKey("crm-canonical")
        val rightKey = ModelKey("crm-prod")
        val leftModelRef = modelRefKey(leftKey)
        val rightModelRef = modelRefKey(rightKey)
        val customerEntityRef = EntityRef.ByKey(EntityKey("Customer"))
        val emailAttributeRef = EntityAttributeRef.ByKey(AttributeKey("email"))

        // - create crm-canonical 1.0.0
        // - crm-canonical create type String
        // - crm-canonical create entity key=Customer name=Customer identifier=id:String
        // - crm-canonical create attribute email:String
        // - crm-canonical release 2.0.0

        // - create crm-prod 1.0.0
        // - crm-prod create type String
        // - crm-prod create entity Customer named Customer with id:String
        // - crm-prod.Customer createAttribute email type String optional false
        // - crm-prod.Customer.email set optional true
        // - crm-prod release 2.0.0
        // - crm-prod.Customer.email set optional false

        env.dispatch(
            ModelAction.Model_Create(
                key = leftKey,
                name = LocalizedTextNotLocalized("Left model"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )
        env.dispatch(
            ModelAction.Type_Create(
                modelRef = leftModelRef,
                typeKey = TypeKey("String"),
                name = null,
                description = null
            )
        )
        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = leftModelRef,
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
                modelRef = leftModelRef,
                entityRef = customerEntityRef,
                attributeKey = AttributeKey("email"),
                type = typeRef("String"),
                optional = false,
                name = LocalizedTextNotLocalized("Email"),
                description = null
            )
        )
        env.dispatch(
            ModelAction.Model_Release(
                modelRef = leftModelRef,
                value = ModelVersion("2.0.0")
            )
        )

        env.dispatch(
            ModelAction.Model_Create(
                key = rightKey,
                name = LocalizedTextNotLocalized("Right model"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )
        env.dispatch(
            ModelAction.Type_Create(
                modelRef = rightModelRef,
                typeKey = TypeKey("String"),
                name = null,
                description = null
            )
        )
        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = rightModelRef,
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
                modelRef = rightModelRef,
                entityRef = customerEntityRef,
                attributeKey = AttributeKey("email"),
                type = typeRef("String"),
                optional = false,
                name = LocalizedTextNotLocalized("Email"),
                description = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_UpdateOptional(
                modelRef = rightModelRef,
                entityRef = customerEntityRef,
                attributeRef = emailAttributeRef,
                value = true
            )
        )
        env.dispatch(
            ModelAction.Model_Release(
                modelRef = rightModelRef,
                value = ModelVersion("2.0.0")
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_UpdateOptional(
                modelRef = rightModelRef,
                entityRef = customerEntityRef,
                attributeRef = emailAttributeRef,
                value = false
            )
        )

        val result = env.dispatch(
            ModelAction.Compare(
                leftModelRef = leftModelRef,
                leftModelVersion = ModelVersion("2.0.0"),
                rightModelRef = rightModelRef,
                rightModelVersion = ModelVersion("2.0.0"),
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
        assertEquals("2.0.0", diff.left.modelVersion)
        assertEquals("2.0.0", diff.right.modelVersion)
    }

    private fun createBaseModel(env: ModelTestEnv, modelKey: ModelKey) {
        val modelRef = modelRefKey(modelKey)
        val entityRef = EntityRef.ByKey(EntityKey("Customer"))

        env.dispatch(
            ModelAction.Model_Create(
                key = modelKey,
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
