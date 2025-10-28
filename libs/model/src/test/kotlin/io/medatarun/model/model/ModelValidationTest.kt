package io.medatarun.model.model

import io.medatarun.model.infra.AttributeDefInMemory
import io.medatarun.model.infra.EntityDefInMemory
import io.medatarun.model.infra.ModelInMemory
import io.medatarun.model.infra.ModelTypeInMemory
import io.medatarun.model.internal.ModelValidationImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ModelValidationTest {
    private val validation: ModelValidation = ModelValidationImpl()

    @Test
    fun `model with bad entity identifier`() {
        val model = ModelInMemory(
            id = ModelId("test"),
            name = null,
            description = null,
            version = ModelVersion("0.0.1"),
            types = listOf(ModelTypeInMemory(id = ModelTypeId("String"), name = null, description = null)),
            entityDefs = listOf(
                EntityDefInMemory(
                    id = EntityDefId("Contact"),
                    name = null,
                    description = null,
                    identifierAttributeDefId = AttributeDefId("unknown"),
                    attributes = listOf(
                        AttributeDefInMemory(
                            id = AttributeDefId("id"),
                            type = ModelTypeId("String"),
                            name = null,
                            description = null,
                            optional = false
                        )
                    )
                )
            ),
            relationshipDefs = emptyList()
        )
        val result = validation.validate(model)
        assertIs<ModelValidationState.Error>(result)
        assertEquals(1, result.errors.size)
        assertIs<ModelValidationErrorInvalidIdentityAttribute>(result.errors.first())

    }

    @Test
    fun `model with bad attribute type`() {
        val model = ModelInMemory(
            id = ModelId("test"),
            name = null,
            description = null,
            version = ModelVersion("0.0.1"),
            types = listOf(ModelTypeInMemory(id = ModelTypeId("String"), name = null, description = null)),
            entityDefs = listOf(
                EntityDefInMemory(
                    id = EntityDefId("Contact"),
                    name = null,
                    description = null,
                    identifierAttributeDefId = AttributeDefId("id"),
                    attributes = listOf(
                        AttributeDefInMemory(
                            id = AttributeDefId("id"),
                            type = ModelTypeId("Int"),
                            name = null,
                            description = null,
                            optional = false
                        )
                    )
                )
            ),
            relationshipDefs = emptyList()
        )
        val result = validation.validate(model)
        assertIs<ModelValidationState.Error>(result)
        assertEquals(1, result.errors.size)
        assertIs<ModelValidationErrorTypeNotFound>(result.errors.first())

    }

}