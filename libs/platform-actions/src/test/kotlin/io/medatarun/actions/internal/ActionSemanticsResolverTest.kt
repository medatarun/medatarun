package io.medatarun.actions.internal

import io.medatarun.actions.domain.ActionCmdAccessType
import io.medatarun.actions.domain.ActionCmdDescriptor
import io.medatarun.actions.domain.ActionCmdParamDescriptor
import io.medatarun.actions.domain.ActionId
import io.medatarun.actions.domain.ActionSemantics
import io.medatarun.actions.domain.ActionSemanticsAutoInferenceException
import io.medatarun.actions.domain.ActionSemanticsConfig
import io.medatarun.actions.domain.ActionSemanticsInvalidSubjectFormatException
import io.medatarun.actions.domain.ActionSemanticsSubject
import io.medatarun.actions.domain.ActionSemanticsSubjectReferencingParam
import io.medatarun.actions.domain.ActionSemanticsSubjectReferencingParamKind
import io.medatarun.actions.ports.needs.ActionDocSemanticsIntent
import io.medatarun.type.commons.id.Id
import io.medatarun.types.TypeJsonEquiv
import org.junit.jupiter.api.Nested
import java.util.UUID
import kotlin.math.acos
import kotlin.reflect.full.createType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ActionSemanticsResolverTest {

    @Nested inner class DecodeSubjects {

        fun decodeSubjects(subjects: List<String>): List<ActionSemanticsSubject> {
            return ActionSemanticsResolver().createSemantics(
                ActionCmdDescriptor(
                    id = Id.generate(::ActionId),
                    key = "dummy",
                    actionClassName = "",
                    group = "",
                    title = null,
                    description = null,
                    resultType = Unit::class.createType(),
                    parameters = emptyList(),
                    accessType = ActionCmdAccessType.DISPATCH,
                    uiLocations = emptySet(),
                    securityRule = "",
                    semantics = ActionSemanticsConfig.Declared(
                        intent = ActionDocSemanticsIntent.CREATE,
                        subjects = subjects,
                        returns = emptyList()
                    )

                )
            ).subjects
        }

        @Test
        fun `decodeSubjects should decode one subject with one reference param`() {
            val result = decodeSubjects(listOf("model(modelRef)"))

            assertEquals(1, result.size)
            assertEquals("model", result[0].type)
            assertEquals(
                listOf(
                    ActionSemanticsSubjectReferencingParam(
                        name = "modelRef",
                        kind = ActionSemanticsSubjectReferencingParamKind.REF
                    )
                ),
                result[0].referencingParams
            )
        }

        @Test
        fun `decodeSubjects should decode multiple subjects`() {
            val result = decodeSubjects(
                listOf("model(modelRef)", "entity(modelRef,entityId)")
            )

            assertEquals(2, result.size)
            assertEquals("model", result[0].type)
            assertEquals(
                listOf(
                    ActionSemanticsSubjectReferencingParam(
                        name = "modelRef",
                        kind = ActionSemanticsSubjectReferencingParamKind.REF
                    )
                ),
                result[0].referencingParams
            )
            assertEquals("entity", result[1].type)
            assertEquals(
                listOf(
                    ActionSemanticsSubjectReferencingParam(
                        name = "modelRef",
                        kind = ActionSemanticsSubjectReferencingParamKind.REF
                    ),
                    ActionSemanticsSubjectReferencingParam(
                        name = "entityId",
                        kind = ActionSemanticsSubjectReferencingParamKind.ID
                    )
                ),
                result[1].referencingParams
            )
        }

        @Test
        fun `decodeSubjects should trim spaces around type and reference params`() {
            val result = decodeSubjects(listOf("  entity ( modelRef , entityId )  "))

            assertEquals(1, result.size)
            assertEquals("entity", result[0].type)
            assertEquals(
                listOf(
                    ActionSemanticsSubjectReferencingParam(
                        name = "modelRef",
                        kind = ActionSemanticsSubjectReferencingParamKind.REF
                    ),
                    ActionSemanticsSubjectReferencingParam(
                        name = "entityId",
                        kind = ActionSemanticsSubjectReferencingParamKind.ID
                    )
                ),
                result[0].referencingParams
            )
        }

        @Test
        fun `decodeSubjects should throw when subject is blank`() {
            assertFailsWith<ActionSemanticsInvalidSubjectFormatException> {
                decodeSubjects(listOf("   "))
            }
        }

        @Test
        fun `decodeSubjects should throw when parenthesis are missing`() {
            assertFailsWith<ActionSemanticsInvalidSubjectFormatException> {
                decodeSubjects(listOf("entitymodelRef,entityId"))
            }
        }

        @Test
        fun `decodeSubjects should throw when data exists after closing parenthesis`() {
            assertFailsWith<ActionSemanticsInvalidSubjectFormatException> {
                decodeSubjects(listOf("entity(modelRef) trailing"))
            }
        }
    }

    @Nested
    inner class InferFromAction {
        @Test
        fun `auto should infer model create from model_create`() {
            val action = createAutoActionDescriptor(
                key = "model_create",
                group = "model",
                paramNames = listOf("modelKey", "name", "description", "version")
            )

            val semantics = ActionSemanticsResolver().createSemantics(action)

            assertEquals(ActionDocSemanticsIntent.CREATE, semantics.intent)
            assertEquals(1, semantics.subjects.size)
            assertEquals("model", semantics.subjects[0].type)
            assertEquals(
                listOf(
                    ActionSemanticsSubjectReferencingParam(
                        name = "modelKey",
                        kind = ActionSemanticsSubjectReferencingParamKind.KEY
                    )
                ),
                semantics.subjects[0].referencingParams
            )
        }

        @Test
        fun `auto should infer model update from model_update_name`() {
            val action = createAutoActionDescriptor(
                key = "model_update_name",
                group = "model",
                paramNames = listOf("modelRef", "value")
            )

            val semantics = ActionSemanticsResolver().createSemantics(action)

            assertEquals(ActionDocSemanticsIntent.UPDATE, semantics.intent)
            assertEquals("model", semantics.subjects[0].type)
            assertEquals(
                listOf(
                    ActionSemanticsSubjectReferencingParam(
                        name = "modelRef",
                        kind = ActionSemanticsSubjectReferencingParamKind.REF
                    )
                ),
                semantics.subjects[0].referencingParams
            )
        }

        @Test
        fun `auto should infer model delete from model_delete`() {
            val action = createAutoActionDescriptor(
                key = "model_delete",
                group = "model",
                paramNames = listOf("modelRef")
            )

            val semantics = ActionSemanticsResolver().createSemantics(action)

            assertEquals(ActionDocSemanticsIntent.DELETE, semantics.intent)
            assertEquals("model", semantics.subjects[0].type)
            assertEquals(
                listOf(
                    ActionSemanticsSubjectReferencingParam(
                        name = "modelRef",
                        kind = ActionSemanticsSubjectReferencingParamKind.REF
                    )
                ),
                semantics.subjects[0].referencingParams
            )
        }

        @Test
        fun `auto should throw when subject is not explicit in action key`() {
            val action = createAutoActionDescriptor(
                key = "search",
                group = "model",
                paramNames = listOf("filters", "fields")
            )

            assertFailsWith<ActionSemanticsAutoInferenceException> {
                ActionSemanticsResolver().createSemantics(action)
            }
        }

        @Test
        fun `auto should infer tag free create from tag_free_create`() {
            val action = createAutoActionDescriptor(
                key = "tag_free_create",
                group = "tag",
                paramNames = listOf("scopeRef", "key", "name", "description")
            )

            val semantics = ActionSemanticsResolver().createSemantics(action)

            assertEquals(ActionDocSemanticsIntent.CREATE, semantics.intent)
            assertEquals("tag_free", semantics.subjects[0].type)
            assertEquals(
                listOf(
                    ActionSemanticsSubjectReferencingParam(
                        name = "scopeRef",
                        kind = ActionSemanticsSubjectReferencingParamKind.REF
                    ),
                    ActionSemanticsSubjectReferencingParam(
                        name = "key",
                        kind = ActionSemanticsSubjectReferencingParamKind.KEY
                    )
                ),
                semantics.subjects[0].referencingParams
            )
        }

        @Test
        fun `auto should infer tag read from tag_search`() {
            val action = createAutoActionDescriptor(
                key = "tag_search",
                group = "tag",
                paramNames = listOf("filters")
            )

            val semantics = ActionSemanticsResolver().createSemantics(action)

            assertEquals(ActionDocSemanticsIntent.READ, semantics.intent)
            assertEquals("tag", semantics.subjects[0].type)
            assertEquals(emptyList(), semantics.subjects[0].referencingParams)
        }

        @Test
        fun `auto should throw when verb is unknown`() {
            val action = createAutoActionDescriptor(
                key = "model_reconcile",
                group = "model",
                paramNames = listOf("modelRef")
            )

            assertFailsWith<ActionSemanticsAutoInferenceException> {
                ActionSemanticsResolver().createSemantics(action)
            }
        }
    }

    private fun createAutoActionDescriptor(
        key: String,
        group: String,
        paramNames: List<String>
    ): ActionCmdDescriptor {
        val params = paramNames.mapIndexed { index, paramName ->
            ActionCmdParamDescriptor(
                name = paramName,
                title = null,
                type = String::class.createType(),
                multiplatformType = "String",
                jsonType = TypeJsonEquiv.STRING,
                optional = false,
                order = index,
                description = null
            )
        }

        return ActionCmdDescriptor(
            id = ActionId(UUID.randomUUID()),
            key = key,
            actionClassName = "TestAction",
            group = group,
            title = null,
            description = null,
            resultType = Unit::class.createType(),
            parameters = params,
            accessType = ActionCmdAccessType.DISPATCH,
            uiLocations = emptySet(),
            securityRule = "test",
            semantics = ActionSemanticsConfig.Auto
        )
    }
}
