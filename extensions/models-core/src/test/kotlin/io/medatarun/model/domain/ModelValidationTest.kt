package io.medatarun.model.domain

import io.medatarun.model.infra.AttributeInMemory
import io.medatarun.model.infra.ModelInMemory
import io.medatarun.model.infra.ModelTypeInMemory
import io.medatarun.model.internal.ModelValidationImpl
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test
import kotlin.test.assertIs

class ModelValidationTest {
    private val validation: ModelValidation = ModelValidationImpl()

    @Test
    fun `model with bad entity identifier`() {
            val model = ModelInMemory.builder(
                key = ModelKey("test"),
                version = ModelVersion("0.0.1"),
            ) {
                val typeStringId = TypeId.generate()
                types = mutableListOf(
                    ModelTypeInMemory(
                        id = typeStringId,
                        key = TypeKey("String"),
                        name = null,
                        description = null
                    )
                )
                val goodAttributeId = AttributeId.generate()
                val badAttributeId = AttributeId.generate()
                addEntity(
                    key = EntityKey("Contact"),
                    identifierAttributeId = badAttributeId
                ) {
                    addAttribute(
                        AttributeInMemory(
                            id = goodAttributeId,
                            key = AttributeKey("id"),
                            typeId = typeStringId,
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
        kotlin.test.assertEquals(1, result.errors.size)
        assertIs<ModelValidationErrorInvalidIdentityAttribute>(result.errors.first())

    }

    @Test
    fun `model with bad attribute type`() {
        val identifierAttribute = AttributeId.generate()
        val typeIdString = TypeId.generate()
        val typeIdInvalid = TypeId.generate()
        val model = ModelInMemory.builder(
            key = ModelKey("test"),
            version = ModelVersion("0.0.1"),
        ) {
            name = null
            description = null
            types = mutableListOf(
                ModelTypeInMemory(
                    id = typeIdString,
                    key = TypeKey("String"),
                    name = null,
                    description = null
                )
            )
            addEntity(
                key = EntityKey("Contact"),
                identifierAttributeId = identifierAttribute,
            ) {
                addAttribute(
                    AttributeInMemory(
                        id = identifierAttribute,
                        key = AttributeKey("id"),
                        typeId = typeIdInvalid,
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