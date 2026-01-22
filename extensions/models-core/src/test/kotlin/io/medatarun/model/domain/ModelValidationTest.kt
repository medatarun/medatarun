package io.medatarun.model.domain

import io.medatarun.model.infra.AttributeDefInMemory
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
        val model = ModelInMemory.builder(
            id = ModelKey("test"),
            version = ModelVersion("0.0.1"),
        ) {
            types = mutableListOf(ModelTypeInMemory(key = TypeKey("String"), name = null, description = null))
            addEntityDef(
                id = EntityKey("Contact"),
                identifierAttributeKey = AttributeKey("unknown"),
                {
                    addAttribute(
                        AttributeDefInMemory(
                            key = AttributeKey("id"),
                            type = TypeKey("String"),
                            name = null,
                            description = null,
                            optional = false,
                            hashtags = emptyList()
                        )
                    )
                }
            )
        }
        val result = validation.validate(model)
        assertIs<ModelValidationState.Error>(result)
        assertEquals(1, result.errors.size)
        assertIs<ModelValidationErrorInvalidIdentityAttribute>(result.errors.first())

    }

    @Test
    fun `model with bad attribute type`() {
        val model = ModelInMemory.builder(
            id = ModelKey("test"),
            version = ModelVersion("0.0.1"),
        ) {
            name = null
            description = null
            types = mutableListOf(ModelTypeInMemory(key = TypeKey("String"), name = null, description = null))
            addEntityDef(
                id = EntityKey("Contact"),
                identifierAttributeKey = AttributeKey("id"),
            ) {
                addAttribute(
                    AttributeDefInMemory(
                        key = AttributeKey("id"),
                        type = TypeKey("Int"),
                        name = null,
                        description = null,
                        optional = false,
                        hashtags = emptyList()
                    )
                )
            }
        }

        val result = validation.validate(model)
        assertIs<ModelValidationState.Error>(result)
        assertEquals(1, result.errors.size)
        assertIs<ModelValidationErrorTypeNotFound>(result.errors.first())

    }

}