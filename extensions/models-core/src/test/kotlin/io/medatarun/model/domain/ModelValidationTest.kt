package io.medatarun.model.domain

import io.medatarun.model.infra.AttributeDefInMemory
import io.medatarun.model.infra.ModelInMemory
import io.medatarun.model.infra.ModelInMemoryEntityIdentifierPointsToUnknownAttributeException
import io.medatarun.model.infra.ModelTypeInMemory
import io.medatarun.model.internal.ModelValidationImpl
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ModelValidationTest {
    private val validation: ModelValidation = ModelValidationImpl()

    @Test
    fun `model with bad entity identifier`() {
        assertThrows<ModelInMemoryEntityIdentifierPointsToUnknownAttributeException> {
            ModelInMemory.builder(
                key = ModelKey("test"),
                version = ModelVersion("0.0.1"),
            ) {
                types = mutableListOf(
                    ModelTypeInMemory(
                        id = TypeId.generate(),
                        key = TypeKey("String"),
                        name = null,
                        description = null
                    )
                )
                val goodAttributeId = AttributeId.generate()
                val badAttributeId = AttributeId.generate()
                addEntityDef(
                    key = EntityKey("Contact"),
                    identifierAttributeId = badAttributeId
                ) {
                    addAttribute(
                        AttributeDefInMemory(
                            id = goodAttributeId,
                            key = AttributeKey("id"),
                            type = TypeKey("String"),
                            name = null,
                            description = null,
                            optional = false,
                            hashtags = emptyList()
                        )
                    )
                }
            }
        }


    }

    @Test
    fun `model with bad attribute type`() {
        val identifierAttribute = AttributeId.generate()
        val model = ModelInMemory.builder(
            key = ModelKey("test"),
            version = ModelVersion("0.0.1"),
        ) {
            name = null
            description = null
            types = mutableListOf(
                ModelTypeInMemory(
                    id = TypeId.generate(),
                    key = TypeKey("String"),
                    name = null,
                    description = null
                )
            )
            addEntityDef(
                key = EntityKey("Contact"),
                identifierAttributeId = identifierAttribute,
            ) {
                addAttribute(
                    AttributeDefInMemory(
                        id = identifierAttribute,
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