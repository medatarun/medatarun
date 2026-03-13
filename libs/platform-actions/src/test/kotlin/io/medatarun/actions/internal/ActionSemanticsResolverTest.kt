package io.medatarun.actions.internal

import io.medatarun.actions.domain.*
import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionDocSemanticsIntent
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx
import io.medatarun.type.commons.id.Id
import io.medatarun.types.TypeJsonEquiv
import org.junit.jupiter.api.Nested
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.createType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ActionSemanticsResolverTest {

    @Nested
    inner class DecodeSubjects {

        fun decodeSubjects(subjects: List<String>): List<ActionSemanticsSubject> {
            val action = ActionDescriptorBase(
                id = Id.generate(::ActionId),
                key = "dummy",
                actionClassName = "",
                group = "test",
                title = null,
                description = null,
                resultType = Unit::class.createType(),
                accessType = ActionAccessType.DISPATCH,
                uiLocations = emptySet(),
                securityRule = "",
            )
            val parameters = emptyList<ActionDescriptorParamImpl>()
            val semantics = ActionSemanticsConfig.Declared(
                intent = ActionDocSemanticsIntent.CREATE,
                subjects = subjects,
                returns = emptyList()
            )
            val descriptor = ActionDescriptorImpl(action, parameters, semantics)
            val env = envWithActionDescriptors(listOf(descriptor))

            return env.actionPlatform.registry.findAction("test", "dummy").semantics.subjects
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
        private fun createAutoActionDescriptor(
            key: String,
            group: String,
            paramNames: List<String>
        ): ActionDescriptor {

            return ActionDescriptorImpl(
                base = ActionDescriptorBase(
                    id = ActionId(UUID.randomUUID()),
                    key = key,
                    actionClassName = "TestAction",
                    group = group,
                    title = null,
                    description = null,
                    resultType = Unit::class.createType(),
                    accessType = ActionAccessType.DISPATCH,
                    uiLocations = emptySet(),
                    securityRule = "test",
                ),
                params = paramNames.mapIndexed { index, paramName ->
                    ActionDescriptorParamImpl(
                        key = paramName,
                        title = null,
                        type = String::class.createType(),
                        multiplatformType = "String",
                        jsonType = TypeJsonEquiv.STRING,
                        optional = false,
                        order = index,
                        description = null
                    )
                },
                semantics = ActionSemanticsConfig.Auto
            )

        }

        fun semantics(action: ActionDescriptor): ActionSemantics {
            val env = envWithActionDescriptors(listOf(action))

            return env.actionPlatform.registry.findAction(action.group, action.key).semantics
        }


        @Test
        fun `auto should infer model create from model_create`() {
            val action = createAutoActionDescriptor(
                key = "model_create",
                group = "model",
                paramNames = listOf("modelKey", "name", "description", "version")
            )

            val semantics = semantics(action)

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
            assertEquals(emptyList(), semantics.returns)
        }

        @Test
        fun `auto should infer model update from model_update_name`() {


            val action = createAutoActionDescriptor(
                key = "model_update_name",
                group = "model",
                paramNames = listOf("modelRef", "value")
            )

            val semantics = semantics(action)

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
            assertEquals(emptyList(), semantics.returns)
        }

        @Test
        fun `auto should infer model delete from model_delete`() {
            val action = createAutoActionDescriptor(
                key = "model_delete",
                group = "model",
                paramNames = listOf("modelRef")
            )

            val semantics = semantics(action)

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
            assertEquals(emptyList(), semantics.returns)
        }

        @Test
        fun `auto should throw when subject is not explicit in action key`() {
            assertFailsWith<ActionSemanticsAutoInferenceException> {
                val action = createAutoActionDescriptor(
                    key = "search",
                    group = "model",
                    paramNames = listOf("filters", "fields")
                )
                semantics(action)
            }
        }

        @Test
        fun `auto should infer tag free create from tag_free_create`() {
            val action = createAutoActionDescriptor(
                key = "tag_free_create",
                group = "tag",
                paramNames = listOf("scopeRef", "key", "name", "description")
            )

            val semantics = semantics(action)

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
            assertEquals(emptyList(), semantics.returns)
        }

        @Test
        fun `auto should infer tag read from tag_search`() {
            val action = createAutoActionDescriptor(
                key = "tag_search",
                group = "tag",
                paramNames = listOf("filters")
            )

            val semantics = semantics(action)

            assertEquals(ActionDocSemanticsIntent.READ, semantics.intent)
            assertEquals(emptyList(), semantics.subjects)
            assertEquals(listOf("tag"), semantics.returns)
        }

        @Test
        fun `auto should throw when verb is unknown`() {

            assertFailsWith<ActionSemanticsAutoInferenceException> {
                val action = createAutoActionDescriptor(
                    key = "model_reconcile",
                    group = "model",
                    paramNames = listOf("modelRef")
                )

                semantics(action)
            }
        }
    }

    private class StaticActionProvider(
        private val actions: List<ActionDescriptor>
    ) : ActionProvider<Any> {
        override val actionGroupKey: String = "test"
        override fun findCommandClass(): KClass<Any>? = null
        override fun dispatch(cmd: Any, actionCtx: ActionCtx): Any? = null
        override fun findActions(): List<ActionDescriptor> = actions
    }

    private fun envWithActionDescriptors(actions: List<ActionDescriptor>): ActionTestEnv {
        val extension = object : MedatarunExtension {
            override val id = "action-test-env"
            override fun initContributions(ctx: MedatarunExtensionCtx) {
                ctx.registerContribution(ActionProvider::class, StaticActionProvider(actions))
            }
        }
        return ActionTestEnv(listOf(extension))
    }

}
