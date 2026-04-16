package io.medatarun.actions.internal

import io.medatarun.actions.domain.ActionInvocationException
import io.medatarun.actions.ports.needs.*
import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.http.StatusCode
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx
import io.medatarun.platform.kernel.MedatarunServiceCtx
import io.medatarun.security.SecurityRuleCtx
import io.medatarun.security.SecurityRuleEvaluator
import io.medatarun.security.SecurityRuleEvaluatorResult
import io.medatarun.security.SecurityRulesProvider
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonEquiv
import kotlinx.serialization.json.*
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant
import java.time.LocalDate
import kotlin.reflect.KClass
import kotlin.test.*

class ActionInvokerTest {

    // ------------------------------------------------------------------------
    // Dispatch tests
    // ------------------------------------------------------------------------

    @Test
    fun `dispatch uses the action class resolved from json`() {
        val runtime = TestRuntime()

        val result = runtime.invoke(ACTION_NAME_ALPHA, buildJsonObject {
            put("name", JsonPrimitive("My Name"))
            put("count", JsonPrimitive(2))
        })

        assertEquals("ok", result)
        val resultPayload = runtime.lastCommand()
        assertTrue(resultPayload is TestAction.Alpha)
        assertEquals("My Name", resultPayload.name)
        assertEquals(2, resultPayload.count)
        assertEquals(null, resultPayload.note)
        assertNotNull(runtime.lastActionCtx())
    }

    // ------------------------------------------------------------------------
    // Optional params
    // ------------------------------------------------------------------------

    @Test
    fun `optional params are respected when provided`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("name", JsonPrimitive("My Name"))
            put("count", JsonPrimitive(2))
            put("note", JsonPrimitive("memo"))
        }

        runtime.invoke(ACTION_NAME_ALPHA, payload)

        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.Alpha)
        assertEquals("memo", cmd.note)
    }

    // ------------------------------------------------------------------------
    // List and map
    // ------------------------------------------------------------------------

    @Test
    fun `list and map parameters are converted`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("names", buildJsonArray {
                add(JsonPrimitive("Marie"))
                add(JsonPrimitive("Jean"))
            })
            put("counts", buildJsonObject {
                put("Pierre", JsonPrimitive(1))
                put("Marcel", JsonPrimitive(2))
            })
        }

        runtime.invoke(ACTION_NAME_COLLECTIONS, payload)

        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.WithCollections)
        assertEquals(listOf("Marie", "Jean"), cmd.names)
        assertEquals(mapOf("Pierre" to 1, "Marcel" to 2), cmd.counts)
    }

    @Test
    fun `list when empty are accepted`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("names", buildJsonArray {
            })
            put("counts", buildJsonObject {
                put("Pierre", JsonPrimitive(1))
                put("Marcel", JsonPrimitive(2))
            })
        }

        runtime.invoke(ACTION_NAME_COLLECTIONS, payload)

        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.WithCollections)
        assertEquals(emptyList<String>(), cmd.names)
        assertEquals(mapOf("Pierre" to 1, "Marcel" to 2), cmd.counts)
    }

    // ------------------------------------------------------------------------
    // String
    // ------------------------------------------------------------------------

    @Test
    fun `string optional when undefined then null`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
        }

        runtime.invoke(ACTION_NAME_STRING_OPTIONAL, payload)

        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.WithStringOptional)
        assertNull(cmd.value)
    }

    @Test
    fun `string optional when null then null`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("value", JsonNull)
        }

        runtime.invoke(ACTION_NAME_STRING_OPTIONAL, payload)

        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.WithStringOptional)
        assertNull(cmd.value)
    }

    @Test
    fun `string optional when provided then found`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("value", JsonPrimitive("this is a string"))
        }

        runtime.invoke(ACTION_NAME_STRING_OPTIONAL, payload)

        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.WithStringOptional)
        assertEquals("this is a string", cmd.value)
    }


    // ------------------------------------------------------------------------
    // Big decimal
    // ------------------------------------------------------------------------


    @Test
    fun `big decimal parameters are converted from string`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("price", JsonPrimitive("12.50"))
        }

        runtime.invoke(ACTION_NAME_DECIMAL, payload)

        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.WithDecimal)
        assertEquals(0, BigDecimal("12.50").compareTo(cmd.price))
    }

    @Test
    fun `big decimal parameters are converted from number`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("price", JsonPrimitive(12.50))
        }

        runtime.invoke(ACTION_NAME_DECIMAL, payload)

        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.WithDecimal)
        assertEquals(0, BigDecimal("12.5").compareTo(cmd.price))
    }

    @Test
    fun `big integer parameters are converted from string`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("value", JsonPrimitive("12345678901234567890"))
        }

        runtime.invoke(ACTION_NAME_BIG_INTEGER, payload)

        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.WithBigInteger)
        assertEquals(BigInteger("12345678901234567890"), cmd.value)
    }

    @Test
    fun `big integer parameters are converted from number`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("value", JsonPrimitive(1234567890))
        }

        runtime.invoke(ACTION_NAME_BIG_INTEGER, payload)

        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.WithBigInteger)
        assertEquals(BigInteger("1234567890"), cmd.value)
    }

    @Test
    fun `value class required parameters are converted when specified`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("value", JsonPrimitive("abcd"))
        }

        runtime.invoke(ACTION_NAME_ABBREVIATION, payload)

        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.WithAbbreviation)
        assertEquals(Abbreviation("abcd"), cmd.value)
    }

    @Test
    fun `value class required parameters are rejected when missing`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {

        }
        assertThrows<ActionInvocationException> {
            runtime.invoke(ACTION_NAME_ABBREVIATION, payload)
        }
    }

    @Test
    fun `value class required parameters are rejected when null`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("value", JsonNull)
        }
        assertThrows<ActionInvocationException> {
            runtime.invoke(ACTION_NAME_ABBREVIATION, payload)
        }
    }

    @Test
    fun `value class optional parameters are converted to null when missing`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject { }

        runtime.invoke(ACTION_NAME_OPTIONAL_ABBREVIATION, payload)

        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.WithOptionalAbbreviation)
        assertEquals(null, cmd.value)
    }

    @Test
    fun `value class optional parameters are converted to null when null`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("value", JsonNull)
        }

        runtime.invoke(ACTION_NAME_OPTIONAL_ABBREVIATION, payload)

        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.WithOptionalAbbreviation)
        assertEquals(null, cmd.value)
    }

    @Test
    fun `value class optional parameters are converted when specified`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("value", JsonPrimitive("abcd"))
        }

        runtime.invoke(ACTION_NAME_OPTIONAL_ABBREVIATION, payload)

        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.WithOptionalAbbreviation)
        assertEquals(Abbreviation("abcd"), cmd.value)
    }

    @Test
    fun `double parameters are converted from string`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("value", JsonPrimitive("12.75"))
        }

        runtime.invoke(ACTION_NAME_DOUBLE, payload)

        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.WithDouble)
        assertEquals(12.75, cmd.value)
    }

    @Test
    fun `double parameters are converted from number`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("value", JsonPrimitive(12.75))
        }

        runtime.invoke(ACTION_NAME_DOUBLE, payload)

        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.WithDouble)
        assertEquals(12.75, cmd.value)
    }

    @Test
    fun `instant parameters are converted from string`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("at", JsonPrimitive("2023-11-14T00:00:00Z"))
        }

        runtime.invoke(ACTION_NAME_INSTANT, payload)

        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.WithInstant)
        assertEquals(Instant.parse("2023-11-14T00:00:00Z"), cmd.at)
    }

    @Test
    fun `instant parameters are converted from number`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("at", JsonPrimitive(1700000000123))
        }

        runtime.invoke(ACTION_NAME_INSTANT, payload)

        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.WithInstant)
        assertEquals(Instant.ofEpochMilli(1700000000123), cmd.at)
    }

    @Test
    fun `local date parameters are converted from string`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("date", JsonPrimitive("2023-11-14"))
        }

        runtime.invoke(ACTION_NAME_LOCAL_DATE, payload)

        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.WithLocalDate)
        assertEquals(LocalDate.parse("2023-11-14"), cmd.date)
    }

    @Test
    fun `type validation error returns bad request with details`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("value", JsonPrimitive("TOOLONG"))
        }

        val ex = assertThrows<ActionInvocationException> {
            runtime.invoke(ACTION_NAME_ABBREVIATION, payload)
        }

        assertEquals(StatusCode.BAD_REQUEST, ex.status)
        assertEquals("Abbreviation must be at most 4 chars", ex.payload["details"])
    }

    @Test
    fun `complex object parameters are converted`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("payload", buildJsonObject {
                put("name", JsonPrimitive("pack"))
                put("amount", JsonPrimitive("9.99"))
                put("tags", buildJsonArray {
                    add(JsonPrimitive("t1"))
                    add(JsonPrimitive("t2"))
                })
                put("meta", buildJsonObject {
                    put("a", JsonPrimitive(7))
                    put("b", JsonPrimitive(8))
                })
                put("note", JsonPrimitive("note"))
            })
        }

        runtime.invoke(ACTION_NAME_COMPLEX, payload)

        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.WithComplex)
        assertEquals("pack", cmd.payload.name)
        assertEquals(0, BigDecimal("9.99").compareTo(cmd.payload.amount))
        assertEquals(listOf("t1", "t2"), cmd.payload.tags)
        assertEquals(mapOf("a" to 7, "b" to 8), cmd.payload.meta)
        assertEquals("note", cmd.payload.note)
    }

    @Test
    fun `unknown action group throws not found`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject { }

        val ex = assertThrows<ActionInvocationException> {
            runtime.invokeWithGroup("missing", "alpha", payload)
        }

        assertEquals(StatusCode.NOT_FOUND, ex.status)
    }

    @Test
    fun `unknown action key throws not found`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject { }

        val ex = assertThrows<ActionInvocationException> {
            runtime.invoke(ACTION_NAME_MISSING, payload)
        }

        assertEquals(StatusCode.NOT_FOUND, ex.status)
    }

    @Test
    fun `authentication security rule error returns unauthorized`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject { }

        val ex = assertThrows<ActionInvocationException> {
            runtime.invoke(ACTION_NAME_DENIED_AUTHENTICATION, payload)
        }

        assertEquals(StatusCode.UNAUTHORIZED, ex.status)
    }

    @Test
    fun `authorization security rule error returns forbidden`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject { }

        val ex = assertThrows<ActionInvocationException> {
            runtime.invoke(ACTION_NAME_DENIED_AUTHORIZATION, payload)
        }

        assertEquals(StatusCode.FORBIDDEN, ex.status)
    }

    @Test
    fun `missing required param is rejected`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("name", JsonPrimitive("My Name"))
        }

        val ex = assertThrows<ActionInvocationException> {
            runtime.invoke(ACTION_NAME_ALPHA, payload)
        }

        assertEquals(StatusCode.BAD_REQUEST, ex.status)
    }

    @Test
    fun `invalid parameter type is rejected`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("name", JsonPrimitive("my name"))
            put("count", JsonPrimitive("wrong"))
        }

        val ex = assertThrows<ActionInvocationException> {
            runtime.invoke(ACTION_NAME_ALPHA, payload)
        }

        assertEquals(StatusCode.BAD_REQUEST, ex.status)
    }

    @Test
    fun `audit records received and succeeded`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("name", JsonPrimitive("my name"))
            put("count", JsonPrimitive(2))
        }

        runtime.invoke(ACTION_NAME_ALPHA, payload)

        val recorder = runtime.auditRecorder()
        assertEquals(1, recorder.received.size)
        assertEquals(1, recorder.succeeded.size)
        assertEquals(0, recorder.rejected.size)
        assertEquals(0, recorder.failed.size)
        assertEquals("test", recorder.received.single().source)
        assertEquals(payload.toString(), recorder.received.single().payloadSerialized)
        assertEquals(recorder.received.single().actionInstanceId, recorder.succeeded.single().actionInstanceId)
    }

    @Test
    fun `audit records rejected before business invoke`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject { }

        assertThrows<ActionInvocationException> {
            runtime.invoke(ACTION_NAME_DENIED_AUTHENTICATION, payload)
        }

        val recorder = runtime.auditRecorder()
        assertEquals(1, recorder.received.size)
        assertEquals(1, recorder.rejected.size)
        assertEquals(0, recorder.succeeded.size)
        assertEquals(0, recorder.failed.size)
        assertEquals(recorder.received.single().actionInstanceId, recorder.rejected.single().actionInstanceId)
    }

    @Test
    fun `audit records failed when business invoke throws`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject { }

        assertThrows<TestActionDispatchFailedException> {
            runtime.invoke(ACTION_NAME_CRASH, payload)
        }

        val recorder = runtime.auditRecorder()
        assertEquals(1, recorder.received.size)
        assertEquals(0, recorder.rejected.size)
        assertEquals(0, recorder.succeeded.size)
        assertEquals(1, recorder.failed.size)
        assertEquals(recorder.received.single().actionInstanceId, recorder.failed.single().actionInstanceId)
        assertEquals(TestActionDispatchFailedException::class.simpleName, recorder.failed.single().code)
    }

    private class TestRuntime {

        private val env = ActionTestEnv(listOf(TestActionsExtension()))

        fun invoke(actionKey: String, payload: JsonObject): Any? {
            return env.actionPlatform.invoker.handleInvocation(
                ActionRequest("test", actionKey, ActionPayload.AsJson(payload)), env.actionCtx)
        }

        fun invokeWithGroup(actionGroupKey: String, actionKey: String, payload: JsonObject): Any? {
            val request = ActionRequest(actionGroupKey, actionKey, ActionPayload.AsJson(payload))
            return env.actionPlatform.invoker.handleInvocation(request, env.actionCtx)
        }

        fun lastCommand(): TestAction? {
            return env.runtime.services.getService(TestActionProvider::class).lastCommand
        }

        fun lastActionCtx(): ActionCtx? {
            return env.runtime.services.getService(TestActionProvider::class).lastActionCtx
        }

        fun auditRecorder(): TestActionAuditRecorder {
            return env.runtime.services.getService(TestActionAuditRecorder::class)
        }
    }

    private class TestActionsExtension : MedatarunExtension {
        override val id: String = "platform-actions-test"

        override fun initServices(ctx: MedatarunServiceCtx) {
            val actionProvider = TestActionProvider()
            val actionAuditRecorder = TestActionAuditRecorder()
            ctx.register(TestActionProvider::class, actionProvider)
            ctx.register(TestActionAuditRecorder::class, actionAuditRecorder)
        }

        override fun initContributions(ctx: MedatarunExtensionCtx) {
            ctx.registerContribution(ActionProvider::class, ctx.getService(TestActionProvider::class))
            ctx.registerContribution(ActionAuditRecorder::class, ctx.getService(TestActionAuditRecorder::class))
            ctx.registerContribution(TypeDescriptor::class, ComplexPayloadTypeDescriptor)
            ctx.registerContribution(TypeDescriptor::class, AbbreviationTypeDescriptor)
            ctx.registerContribution(SecurityRulesProvider::class, DefaultTestSecurityRulesProvider)
        }
    }

    private object DefaultTestSecurityRulesProvider : SecurityRulesProvider {
        override fun getRules(): List<SecurityRuleEvaluator> {
            return listOf(
                AllowSecurityRuleEvaluator(),
                DenyAuthorizationSecurityRuleEvaluator(),
                DenyAuthenticationSecurityRuleEvaluator()
            )
        }
    }

    @Suppress("unused") // Remove unused because "key" launches actions, doesn't mean the action is not used.
    private sealed interface TestAction {
        @ActionDoc(
            key = ACTION_NAME_ALPHA,
            title = "Alpha",
            description = "Test action",
            uiLocations = [""],
            securityRule = RULE_ALLOW,
            semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
        )
        class Alpha(
            @ActionParamDoc(
                name = "name",
                description = "Name",
                order = 1
            )
            val name: String,
            @ActionParamDoc(
                name = "count",
                description = "Count",
                order = 2
            )
            val count: Int,
            @ActionParamDoc(
                name = "note",
                description = "Note",
                order = 3
            )
            val note: String?
        ) : TestAction

        @ActionDoc(
            key = ACTION_NAME_DENIED_AUTHORIZATION,
            title = "Denied authorization",
            description = "Action denied by security",
            uiLocations = [""],
            securityRule = RULE_DENY_AUTHORIZATION,
            semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
        )
        class DeniedAuthorization : TestAction

        @ActionDoc(
            key = ACTION_NAME_DENIED_AUTHENTICATION,
            title = "Denied authentication",
            description = "Action denied by security",
            uiLocations = [""],
            securityRule = RULE_DENY_AUTHENTICATION,
            semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
        )
        class DeniedAuthentication : TestAction

        @ActionDoc(
            key = ACTION_NAME_CRASH,
            title = "Crash",
            description = "Action that throws during business invoke",
            uiLocations = [""],
            securityRule = RULE_ALLOW,
            semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
        )
        class Crash : TestAction

        @ActionDoc(
            key = ACTION_NAME_COLLECTIONS,
            title = "Collections",
            description = "Action with list and map",
            uiLocations = [""],
            securityRule = RULE_ALLOW,
            semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
        )
        class WithCollections(
            @ActionParamDoc(
                name = "names",
                description = "Names list",
                order = 1
            )
            val names: List<String>,
            @ActionParamDoc(
                name = "counts",
                description = "Counts map",
                order = 2
            )
            val counts: Map<String, Int>
        ) : TestAction

        @ActionDoc(
            key = ACTION_NAME_DECIMAL,
            title = "Decimal",
            description = "Action with BigDecimal",
            uiLocations = [""],
            securityRule = RULE_ALLOW,
            semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
        )
        class WithDecimal(
            @ActionParamDoc(
                name = "price",
                description = "Price",
                order = 1
            )
            val price: BigDecimal
        ) : TestAction

        @ActionDoc(
            key = ACTION_NAME_BIG_INTEGER,
            title = "BigInteger",
            description = "Action with BigInteger",
            uiLocations = [""],
            securityRule = RULE_ALLOW,
            semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
        )
        class WithBigInteger(
            @ActionParamDoc(
                name = "value",
                description = "Value",
                order = 1
            )
            val value: BigInteger
        ) : TestAction

        @ActionDoc(
            key = ACTION_NAME_DOUBLE,
            title = "Double",
            description = "Action with Double",
            uiLocations = [""],
            securityRule = RULE_ALLOW,
            semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
        )
        class WithDouble(
            @ActionParamDoc(
                name = "value",
                description = "Value",
                order = 1
            )
            val value: Double
        ) : TestAction

        @ActionDoc(
            key = ACTION_NAME_INSTANT,
            title = "Instant",
            description = "Action with Instant",
            uiLocations = [""],
            securityRule = RULE_ALLOW,
            semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
        )
        class WithInstant(
            @ActionParamDoc(
                name = "at",
                description = "Instant",
                order = 1
            )
            val at: Instant
        ) : TestAction

        @ActionDoc(
            key = ACTION_NAME_LOCAL_DATE,
            title = "LocalDate",
            description = "Action with LocalDate",
            uiLocations = [""],
            securityRule = RULE_ALLOW,
            semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
        )
        class WithLocalDate(
            @ActionParamDoc(
                name = "date",
                description = "Date",
                order = 1
            )
            val date: LocalDate
        ) : TestAction

        @ActionDoc(
            key = ACTION_NAME_STRING_OPTIONAL,
            title = "String",
            description = "Action with String",
            uiLocations = [""],
            securityRule = RULE_ALLOW,
            semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
        )
        class WithStringOptional(
            @ActionParamDoc(
                name = "value",
                description = "Value",
                order = 1
            )
            val value: String?
        ) : TestAction

        @ActionDoc(
            key = ACTION_NAME_ABBREVIATION,
            title = "Abbreviation",
            description = "Action with validated abbreviation",
            uiLocations = [""],
            securityRule = RULE_ALLOW,
            semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
        )
        class WithAbbreviation(
            @ActionParamDoc(
                name = "value",
                description = "Abbreviation value",
                order = 1
            )
            val value: Abbreviation
        ) : TestAction

        @ActionDoc(
            key = ACTION_NAME_OPTIONAL_ABBREVIATION,
            title = "Optional abbreviation",
            description = "Action with optional abbreviation",
            uiLocations = [""],
            securityRule = RULE_ALLOW,
            semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
        )
        class WithOptionalAbbreviation(
            @ActionParamDoc(
                name = "value",
                description = "Optional abbreviation value",
                order = 1
            )
            val value: Abbreviation?
        ) : TestAction

        @ActionDoc(
            key = ACTION_NAME_COMPLEX,
            title = "Complex",
            description = "Action with complex payload",
            uiLocations = [""],
            securityRule = RULE_ALLOW,
            semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
        )
        class WithComplex(
            @ActionParamDoc(
                name = "payload",
                description = "Payload",
                order = 1
            )
            val payload: ComplexPayload
        ) : TestAction
    }

    data class ComplexPayload(
        val name: String,
        val amount: BigDecimal,
        val tags: List<String>,
        val meta: Map<String, Int>,
        val note: String?
    )

    private object ComplexPayloadTypeDescriptor : TypeDescriptor<ComplexPayload> {
        override val target: KClass<ComplexPayload> = ComplexPayload::class
        override val equivMultiplatorm: String = "ComplexPayload"
        override val equivJson: TypeJsonEquiv = TypeJsonEquiv.OBJECT

        override fun validate(value: ComplexPayload): ComplexPayload {
            return value
        }
    }

    @JvmInline
    value class Abbreviation(val value: String)

    private object AbbreviationTypeDescriptor : TypeDescriptor<Abbreviation> {
        override val target: KClass<Abbreviation> = Abbreviation::class
        override val equivMultiplatorm: String = "Abbreviation"
        override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING

        override fun validate(value: Abbreviation): Abbreviation {
            if (value.value.length > 4) {
                throw AbbreviationTooLongException()
            }
            return value
        }
    }

    private class AbbreviationTooLongException :
        MedatarunException("Abbreviation must be at most 4 chars")

    private class TestActionDispatchFailedException :
        MedatarunException("boom")

    private class TestActionProvider : ActionProvider<TestAction> {
        override val actionGroupKey: String = "test"
        var lastCommand: TestAction? = null
        var lastActionCtx: ActionCtx? = null

        override fun findCommandClass(): KClass<TestAction> {
            return TestAction::class
        }

        override fun dispatch(action: TestAction, actionCtx: ActionCtx): Any {
            lastCommand = action
            lastActionCtx = actionCtx
            if (action is TestAction.Crash) {
                throw TestActionDispatchFailedException()
            }
            return "ok"
        }
    }

    private class TestActionAuditRecorder : ActionAuditRecorder {
        val received = mutableListOf<ActionAuditReceived>()
        val rejected = mutableListOf<ActionAuditRejected>()
        val succeeded = mutableListOf<ActionAuditSucceeded>()
        val failed = mutableListOf<ActionAuditFailed>()

        override fun onActionReceived(event: ActionAuditReceived) {
            received.add(event)
        }

        override fun onActionRejected(event: ActionAuditRejected) {
            rejected.add(event)
        }

        override fun onActionSucceeded(event: ActionAuditSucceeded) {
            succeeded.add(event)
        }

        override fun onActionFailed(event: ActionAuditFailed) {
            failed.add(event)
        }
    }



    private class AllowSecurityRuleEvaluator : SecurityRuleEvaluator {
        override val key: String = RULE_ALLOW
        override val name: String = "Allow"
        override val description: String = "Allow all in tests."

        override fun evaluate(ctx: SecurityRuleCtx): SecurityRuleEvaluatorResult {
            return SecurityRuleEvaluatorResult.Ok()
        }
    }

    private class DenyAuthenticationSecurityRuleEvaluator : SecurityRuleEvaluator {
        override val key: String = RULE_DENY_AUTHENTICATION
        override val name: String = "Deny by authentication"
        override val description: String = "Deny all in tests."

        override fun evaluate(ctx: SecurityRuleCtx): SecurityRuleEvaluatorResult {
            return SecurityRuleEvaluatorResult.AuthenticationError("blocked")
        }
    }

    private class DenyAuthorizationSecurityRuleEvaluator : SecurityRuleEvaluator {
        override val key: String = RULE_DENY_AUTHORIZATION
        override val name: String = "Deny by authorization"
        override val description: String = "Deny all in tests."

        override fun evaluate(ctx: SecurityRuleCtx): SecurityRuleEvaluatorResult {
            return SecurityRuleEvaluatorResult.AuthorizationError("blocked")
        }
    }



    private companion object {
        const val RULE_ALLOW = "allow"
        const val RULE_DENY_AUTHORIZATION = "deny-authorization"
        const val RULE_DENY_AUTHENTICATION = "deny-authentication"
        const val ACTION_NAME_ALPHA = "alpha"
        const val ACTION_NAME_COLLECTIONS = "collections"
        const val ACTION_NAME_STRING_OPTIONAL = "string_optional"
        const val ACTION_NAME_DECIMAL = "decimal"
        const val ACTION_NAME_BIG_INTEGER = "big_integer"
        const val ACTION_NAME_ABBREVIATION = "abbreviation"
        const val ACTION_NAME_DENIED_AUTHORIZATION = "denied_authorization"
        const val ACTION_NAME_OPTIONAL_ABBREVIATION = "optional_abbreviation"
        const val ACTION_NAME_COMPLEX = "complex"
        const val ACTION_NAME_LOCAL_DATE = "local_date"
        const val ACTION_NAME_DOUBLE = "double"
        const val ACTION_NAME_INSTANT = "instant"
        const val ACTION_NAME_DENIED_AUTHENTICATION = "denied_authentication"
        const val ACTION_NAME_CRASH = "crash"

        // There is no action with this name because it is used to test not found actions
        const val ACTION_NAME_MISSING = "missing"

    }
}
