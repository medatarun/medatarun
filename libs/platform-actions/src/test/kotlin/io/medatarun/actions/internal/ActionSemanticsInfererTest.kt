package io.medatarun.actions.internal

import io.medatarun.actions.domain.ActionSemanticsInvalidSubjectFormatException
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ActionSemanticsInfererTest {

    @Nested inner class DecodeSubjects {

        @Test
        fun `decodeSubjects should decode one subject with one reference param`() {
            val result = ActionSemanticsInferer.decodeSubjects(listOf("model(modelRef)"))

            assertEquals(1, result.size)
            assertEquals("model", result[0].type)
            assertEquals(listOf("modelRef"), result[0].referencingParams)
        }

        @Test
        fun `decodeSubjects should decode multiple subjects`() {
            val result = ActionSemanticsInferer.decodeSubjects(
                listOf("model(modelRef)", "entity(modelRef,entityId)")
            )

            assertEquals(2, result.size)
            assertEquals("model", result[0].type)
            assertEquals(listOf("modelRef"), result[0].referencingParams)
            assertEquals("entity", result[1].type)
            assertEquals(listOf("modelRef", "entityId"), result[1].referencingParams)
        }

        @Test
        fun `decodeSubjects should trim spaces around type and reference params`() {
            val result = ActionSemanticsInferer.decodeSubjects(listOf("  entity ( modelRef , entityId )  "))

            assertEquals(1, result.size)
            assertEquals("entity", result[0].type)
            assertEquals(listOf("modelRef", "entityId"), result[0].referencingParams)
        }

        @Test
        fun `decodeSubjects should throw when subject is blank`() {
            assertFailsWith<ActionSemanticsInvalidSubjectFormatException> {
                ActionSemanticsInferer.decodeSubjects(listOf("   "))
            }
        }

        @Test
        fun `decodeSubjects should throw when parenthesis are missing`() {
            assertFailsWith<ActionSemanticsInvalidSubjectFormatException> {
                ActionSemanticsInferer.decodeSubjects(listOf("entitymodelRef,entityId"))
            }
        }

        @Test
        fun `decodeSubjects should throw when data exists after closing parenthesis`() {
            assertFailsWith<ActionSemanticsInvalidSubjectFormatException> {
                ActionSemanticsInferer.decodeSubjects(listOf("entity(modelRef) trailing"))
            }
        }
    }
}
