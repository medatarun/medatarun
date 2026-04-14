package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.actions.compare.ModelCompareDto
import io.medatarun.model.domain.*
import io.medatarun.model.domain.EntityAttributeRef.Companion.entityAttributeRefKey
import io.medatarun.model.domain.EntityRef.Companion.entityRefKey
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.TypeKey
import io.medatarun.model.domain.TypeRef.Companion.typeRefKey
import io.medatarun.model.domain.diff.ModelDiffScope
import io.medatarun.model.domain.fixtures.ModelTestEnv
import kotlin.test.*

@EnableDatabaseTests
class Model_Compare_Test {

    @Test
    fun `compare action returns model diff result`() {
        val env = ModelTestEnv()
        val leftRef = modelRefKey("left-model")
        val rightRef = modelRefKey("right-model")
        createBaseModel(env, leftRef.key)
        createBaseModel(env, rightRef.key)

        val result = env.dispatch(
            ModelAction.Compare(
                leftModelRef = leftRef,
                leftModelVersion = null,
                rightModelRef = rightRef,
                rightModelVersion = null,
                scope = ModelDiffScope.STRUCTURAL
            )
        )

        assertIs<ModelCompareDto>(result)
    }

    @Test
    fun `compare structural includes attribute optional change`() {
        val env = ModelTestEnv()
        val leftRef = modelRefKey("left-model-optional")
        val rightRef = modelRefKey("right-model-optional")
        createBaseModel(env, leftRef.key)
        createBaseModel(env, rightRef.key)

        env.dispatch(
            ModelAction.EntityAttribute_UpdateOptional(
                modelRef = rightRef,
                entityRef = EntityRef.ByKey(EntityKey("Customer")),
                attributeRef = EntityAttributeRef.ByKey(AttributeKey("email")),
                value = true
            )
        )

        val result = env.dispatch(
            ModelAction.Compare(
                leftModelRef = leftRef,
                leftModelVersion = null,
                rightModelRef = rightRef,
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
        val leftRef = modelRefKey("left-model-content")
        val rightRef = modelRefKey("right-model-content")
        createBaseModel(env, leftRef.key)
        createBaseModel(env, rightRef.key)

        env.dispatch(
            ModelAction.EntityAttribute_UpdateDescription(
                modelRef = rightRef,
                entityRef = EntityRef.ByKey(EntityKey("Customer")),
                attributeRef = EntityAttributeRef.ByKey(AttributeKey("email")),
                value = LocalizedMarkdownNotLocalized("email used for notifications")
            )
        )

        val structuralResult = env.dispatch(
            ModelAction.Compare(
                leftModelRef = leftRef,
                leftModelVersion = null,
                rightModelRef = rightRef,
                rightModelVersion = null,
                scope = ModelDiffScope.STRUCTURAL
            )
        )
        val structuralDiff = assertIs<ModelCompareDto>(structuralResult)
        val structuralEntriesWithoutModel = structuralDiff.entries.filter { it.objectType != "model" }
        assertEquals(0, structuralEntriesWithoutModel.size)

        val completeResult = env.dispatch(
            ModelAction.Compare(
                leftModelRef = leftRef,
                leftModelVersion = null,
                rightModelRef = rightRef,
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
        val leftRef = modelRefKey("left-model-current-version")
        val rightRef = modelRefKey("right-model-current-version")
        createBaseModel(env, leftRef.key)
        createBaseModel(env, rightRef.key)

        env.dispatch(
            ModelAction.EntityAttribute_UpdateOptional(
                modelRef = leftRef,
                entityRef = EntityRef.ByKey(EntityKey("Customer")),
                attributeRef = EntityAttributeRef.ByKey(AttributeKey("email")),
                value = true
            )
        )
        env.dispatch(
            ModelAction.Model_Release(
                modelRef = leftRef,
                value = ModelVersion("2.0.0")
            )
        )

        val result = env.dispatch(
            ModelAction.Compare(
                leftModelRef = leftRef,
                leftModelVersion = null,
                rightModelRef = rightRef,
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
        val leftModelRef = modelRefKey("crm-canonical")
        val rightModelRef = modelRefKey("crm-prod")
        val customerEntityRef = entityRefKey("Customer")
        val emailAttributeRef = entityAttributeRefKey("email")
        val idAttributeRef = entityAttributeRefKey("id")
        val stringTypeRef = typeRefKey("String")

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
                key = leftModelRef.key,
                name = LocalizedTextNotLocalized("Left model"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )
        env.dispatch(
            ModelAction.Type_Create(
                modelRef = leftModelRef,
                typeKey = stringTypeRef.key,
                name = null,
                description = null
            )
        )
        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = leftModelRef,
                entityKey = customerEntityRef.key,
                name = LocalizedTextNotLocalized("Customer"),
                description = null,
                documentationHome = null
            )
        )
        env.dispatch(ModelAction.EntityAttribute_Create(leftModelRef, customerEntityRef, null, idAttributeRef.key, stringTypeRef, false, null))
        env.dispatch(ModelAction.EntityPrimaryKey_Update(leftModelRef, customerEntityRef, listOf(idAttributeRef)))
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = leftModelRef,
                entityRef = customerEntityRef,
                attributeKey = AttributeKey("email"),
                type = stringTypeRef,
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
                key = rightModelRef.key,
                name = LocalizedTextNotLocalized("Right model"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )
        env.dispatch(
            ModelAction.Type_Create(
                modelRef = rightModelRef,
                typeKey = stringTypeRef.key,
                name = null,
                description = null
            )
        )
        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = rightModelRef,
                entityKey = customerEntityRef.key,
                name = LocalizedTextNotLocalized("Customer"),
                description = null,
                documentationHome = null
            )
        )
        env.dispatch(ModelAction.EntityAttribute_Create(rightModelRef, customerEntityRef, null, idAttributeRef.key, stringTypeRef, false, null))
        env.dispatch(ModelAction.EntityPrimaryKey_Update(rightModelRef, customerEntityRef, listOf(idAttributeRef)))

        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = rightModelRef,
                entityRef = customerEntityRef,
                attributeKey = AttributeKey("email"),
                type = stringTypeRef,
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
        val entityRef = entityRefKey("Customer")
        val typeRef = typeRefKey("String")
        val attributeIdRef = entityAttributeRefKey("id")

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
                entityKey = entityRef.key,
                name = LocalizedTextNotLocalized("Customer"),
                description = null,
                documentationHome = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                name = null,
                description = null,
                attributeKey = attributeIdRef.key,
                type = typeRef
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeKey = AttributeKey("email"),
                type = typeRef,
                optional = false,
                name = LocalizedTextNotLocalized("Email"),
                description = null
            )
        )
        env.dispatch(
            ModelAction.EntityPrimaryKey_Update(modelRef, entityRef, listOf(attributeIdRef))
        )
    }
}
