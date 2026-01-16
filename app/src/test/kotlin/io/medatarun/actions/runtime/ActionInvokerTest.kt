package io.medatarun.actions.runtime

import io.ktor.http.*
import io.medatarun.actions.ports.needs.*
import io.medatarun.kernel.ExtensionRegistry
import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.security.AppPrincipal
import io.medatarun.security.SecurityRuleCtx
import io.medatarun.security.SecurityRuleEvaluator
import io.medatarun.security.SecurityRuleEvaluatorResult
import io.medatarun.types.JsonTypeEquiv
import io.medatarun.types.TypeDescriptor
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant
import java.time.LocalDate
import kotlin.reflect.KClass
import kotlin.test.Test

class ActionInvokerTest {

    @Test
    fun `dispatch uses the action class resolved from json`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("name", JsonPrimitive("alpha"))
            put("count", JsonPrimitive(2))
        }

        val result = runtime.invoke("alpha", payload)

        assertEquals("ok", result)
        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.Alpha)
        val alpha = cmd as TestAction.Alpha
        assertEquals("alpha", alpha.name)
        assertEquals(2, alpha.count)
        assertEquals(null, alpha.note)
        assertNotNull(runtime.lastActionCtx())
    }

    @Test
    fun `optional params are respected when provided`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("name", JsonPrimitive("alpha"))
            put("count", JsonPrimitive(2))
            put("note", JsonPrimitive("memo"))
        }

        runtime.invoke("alpha", payload)

        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.Alpha)
        val alpha = cmd as TestAction.Alpha
        assertEquals("memo", alpha.note)
    }

    @Test
    fun `list and map parameters are converted`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("names", buildJsonArray {
                add(JsonPrimitive("alpha"))
                add(JsonPrimitive("beta"))
            })
            put("counts", buildJsonObject {
                put("alpha", JsonPrimitive(1))
                put("beta", JsonPrimitive(2))
            })
        }

        runtime.invoke("collections", payload)

        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.WithCollections)
        val collections = cmd as TestAction.WithCollections
        assertEquals(listOf("alpha", "beta"), collections.names)
        assertEquals(mapOf("alpha" to 1, "beta" to 2), collections.counts)
    }

    @Test
    fun `big decimal parameters are converted from string`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("price", JsonPrimitive("12.50"))
        }

        runtime.invoke("decimal", payload)

        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.WithDecimal)
        val decimal = cmd as TestAction.WithDecimal
        assertEquals(0, BigDecimal("12.50").compareTo(decimal.price))
    }

    @Test
    fun `big decimal parameters are converted from number`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("price", JsonPrimitive(12.50))
        }

        runtime.invoke("decimal", payload)

        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.WithDecimal)
        val decimal = cmd as TestAction.WithDecimal
        assertEquals(0, BigDecimal("12.5").compareTo(decimal.price))
    }

    @Test
    fun `big integer parameters are converted from string`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("value", JsonPrimitive("12345678901234567890"))
        }

        runtime.invoke("big_integer", payload)

        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.WithBigInteger)
        val big = cmd as TestAction.WithBigInteger
        assertEquals(BigInteger("12345678901234567890"), big.value)
    }

    @Test
    fun `big integer parameters are converted from number`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("value", JsonPrimitive(1234567890))
        }

        runtime.invoke("big_integer", payload)

        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.WithBigInteger)
        val big = cmd as TestAction.WithBigInteger
        assertEquals(BigInteger("1234567890"), big.value)
    }

    @Test
    fun `double parameters are converted from string`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("value", JsonPrimitive("12.75"))
        }

        runtime.invoke("double", payload)

        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.WithDouble)
        val dbl = cmd as TestAction.WithDouble
        assertEquals(12.75, dbl.value)
    }

    @Test
    fun `double parameters are converted from number`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("value", JsonPrimitive(12.75))
        }

        runtime.invoke("double", payload)

        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.WithDouble)
        val dbl = cmd as TestAction.WithDouble
        assertEquals(12.75, dbl.value)
    }

    @Test
    fun `instant parameters are converted from string`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("at", JsonPrimitive("2023-11-14T00:00:00Z"))
        }

        runtime.invoke("instant", payload)

        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.WithInstant)
        val instant = cmd as TestAction.WithInstant
        assertEquals(Instant.parse("2023-11-14T00:00:00Z"), instant.at)
    }

    @Test
    fun `instant parameters are converted from number`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("at", JsonPrimitive(1700000000123))
        }

        runtime.invoke("instant", payload)

        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.WithInstant)
        val instant = cmd as TestAction.WithInstant
        assertEquals(Instant.ofEpochMilli(1700000000123), instant.at)
    }

    @Test
    fun `local date parameters are converted from string`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("date", JsonPrimitive("2023-11-14"))
        }

        runtime.invoke("local_date", payload)

        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.WithLocalDate)
        val date = cmd as TestAction.WithLocalDate
        assertEquals(LocalDate.parse("2023-11-14"), date.date)
    }

    @Test
    fun `type validation error returns bad request with details`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("value", JsonPrimitive("TOOLONG"))
        }

        val ex = assertThrows<ActionInvocationException> {
            runtime.invoke("abbreviation", payload)
        }

        assertEquals(HttpStatusCode.BadRequest, ex.status)
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

        runtime.invoke("complex", payload)

        val cmd = runtime.lastCommand()
        assertTrue(cmd is TestAction.WithComplex)
        val complex = cmd as TestAction.WithComplex
        assertEquals("pack", complex.payload.name)
        assertEquals(0, BigDecimal("9.99").compareTo(complex.payload.amount))
        assertEquals(listOf("t1", "t2"), complex.payload.tags)
        assertEquals(mapOf("a" to 7, "b" to 8), complex.payload.meta)
        assertEquals("note", complex.payload.note)
    }

    @Test
    fun `unknown action group throws not found`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject { }

        val ex = assertThrows<ActionInvocationException> {
            runtime.invokeWithGroup("missing", "alpha", payload)
        }

        assertEquals(HttpStatusCode.NotFound, ex.status)
    }

    @Test
    fun `unknown action key throws not found`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject { }

        val ex = assertThrows<ActionInvocationException> {
            runtime.invoke("missing", payload)
        }

        assertEquals(HttpStatusCode.NotFound, ex.status)
    }

    @Test
    fun `security rule error returns unauthorized`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject { }

        val ex = assertThrows<ActionInvocationException> {
            runtime.invoke("denied", payload)
        }

        assertEquals(HttpStatusCode.Unauthorized, ex.status)
    }

    @Test
    fun `missing required param is rejected`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("name", JsonPrimitive("alpha"))
        }

        val ex = assertThrows<ActionInvocationException> {
            runtime.invoke("alpha", payload)
        }

        assertEquals(HttpStatusCode.BadRequest, ex.status)
    }

    @Test
    fun `invalid parameter type is rejected`() {
        val runtime = TestRuntime()
        val payload = buildJsonObject {
            put("name", JsonPrimitive("alpha"))
            put("count", JsonPrimitive("wrong"))
        }

        val ex = assertThrows<ActionInvocationException> {
            runtime.invoke("alpha", payload)
        }

        assertEquals(HttpStatusCode.BadRequest, ex.status)
    }

    private class TestRuntime(
        private val actionProvider: TestActionProvider = TestActionProvider(),
        evaluators: List<SecurityRuleEvaluator> = listOf(AllowSecurityRuleEvaluator(), DenySecurityRuleEvaluator())
    ) {
        private val actionTypesRegistry = ActionTypesRegistry(
            listOf(
                ComplexPayloadTypeDescriptor,
                AbbreviationTypeDescriptor
            )
        )
        private val actionSecurityRuleEvaluators = ActionSecurityRuleEvaluators(evaluators)
        private val actionRegistry = ActionRegistry(
            actionSecurityRuleEvaluators,
            actionTypesRegistry,
            listOf(actionProvider)
        )
        private val actionInvoker = ActionInvoker(
            actionRegistry,
            actionTypesRegistry,
            actionSecurityRuleEvaluators
        )
        private val actionCtx = TestActionCtx()

        fun invoke(actionKey: String, payload: JsonObject): Any? {
            return invokeWithGroup(actionProvider.actionGroupKey, actionKey, payload)
        }

        fun invokeWithGroup(actionGroupKey: String, actionKey: String, payload: JsonObject): Any? {
            val request = ActionRequest(actionGroupKey, actionKey, payload)
            return actionInvoker.handleInvocation(request, actionCtx)
        }

        fun lastCommand(): TestAction? {
            return actionProvider.lastCommand
        }

        fun lastActionCtx(): ActionCtx? {
            return actionProvider.lastActionCtx
        }
    }

    @Suppress("unused") // Remove unused because "key" launches actions, doesn't mean the action is not used.
    private sealed interface TestAction {
        @ActionDoc(
            key = "alpha",
            title = "Alpha",
            description = "Test action",
            uiLocation = "",
            securityRule = RULE_ALLOW
        )
        class Alpha(
            @ActionParamDoc(
                name = "name",
                description = "Action name",
                order = 1
            )
            val name: String,
            @ActionParamDoc(
                name = "count",
                description = "Action count",
                order = 2
            )
            val count: Int,
            @ActionParamDoc(
                name = "note",
                description = "Optional note",
                order = 3
            )
            val note: String? = null
        ) : TestAction

        @ActionDoc(
            key = "denied",
            title = "Denied",
            description = "Action denied by security",
            uiLocation = "",
            securityRule = RULE_DENY
        )
        class Denied : TestAction

        @ActionDoc(
            key = "collections",
            title = "Collections",
            description = "Action with list and map",
            uiLocation = "",
            securityRule = RULE_ALLOW
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
            key = "decimal",
            title = "Decimal",
            description = "Action with BigDecimal",
            uiLocation = "",
            securityRule = RULE_ALLOW
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
            key = "big_integer",
            title = "BigInteger",
            description = "Action with BigInteger",
            uiLocation = "",
            securityRule = RULE_ALLOW
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
            key = "double",
            title = "Double",
            description = "Action with Double",
            uiLocation = "",
            securityRule = RULE_ALLOW
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
            key = "instant",
            title = "Instant",
            description = "Action with Instant",
            uiLocation = "",
            securityRule = RULE_ALLOW
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
            key = "local_date",
            title = "LocalDate",
            description = "Action with LocalDate",
            uiLocation = "",
            securityRule = RULE_ALLOW
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
            key = "abbreviation",
            title = "Abbreviation",
            description = "Action with validated abbreviation",
            uiLocation = "",
            securityRule = RULE_ALLOW
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
            key = "complex",
            title = "Complex",
            description = "Action with complex payload",
            uiLocation = "",
            securityRule = RULE_ALLOW
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
        val note: String? = null
    )

    private object ComplexPayloadTypeDescriptor : TypeDescriptor<ComplexPayload> {
        override val target: KClass<ComplexPayload> = ComplexPayload::class
        override val equivMultiplatorm: String = "ComplexPayload"
        override val equivJson: JsonTypeEquiv = JsonTypeEquiv.OBJECT

        override fun validate(value: ComplexPayload): ComplexPayload {
            return value
        }
    }

    @JvmInline
    value class Abbreviation(val value: String)

    private object AbbreviationTypeDescriptor : TypeDescriptor<Abbreviation> {
        override val target: KClass<Abbreviation> = Abbreviation::class
        override val equivMultiplatorm: String = "Abbreviation"
        override val equivJson: JsonTypeEquiv = JsonTypeEquiv.STRING

        override fun validate(value: Abbreviation): Abbreviation {
            if (value.value.length > 4) {
                throw AbbreviationTooLongException()
            }
            return value
        }
    }

    private class AbbreviationTooLongException :
        MedatarunException("Abbreviation must be at most 4 chars")

    private class TestActionProvider : ActionProvider<TestAction> {
        override val actionGroupKey: String = "test"
        var lastCommand: TestAction? = null
        var lastActionCtx: ActionCtx? = null

        override fun findCommandClass(): KClass<TestAction> {
            return TestAction::class
        }

        override fun dispatch(cmd: TestAction, actionCtx: ActionCtx): Any {
            lastCommand = cmd
            lastActionCtx = actionCtx
            return "ok"
        }
    }

    private class TestActionCtx : ActionCtx {
        override val extensionRegistry: ExtensionRegistry = object : ExtensionRegistry {
            override fun <CONTRIB : Any> findContributionsFlat(api: KClass<CONTRIB>): List<CONTRIB> {
                return emptyList()
            }

            override fun inspectHumanReadable(): String {
                return ""
            }

            override fun inspectJson(): JsonObject {
                return buildJsonObject { }
            }
        }

        override fun dispatchAction(req: ActionRequest): Any? {
            throw TestActionCtxDispatchException()
        }

        override fun <T : Any> getService(type: KClass<T>): T {
            throw TestActionCtxServiceNotFoundException(type)
        }

        override val principal: ActionPrincipalCtx = TestActionPrincipalCtx(null)
    }

    private class TestActionPrincipalCtx(private val providedPrincipal: AppPrincipal?) : ActionPrincipalCtx {
        override val principal: AppPrincipal?
            get() = providedPrincipal

        override fun ensureIsAdmin() {
            if (providedPrincipal == null || !providedPrincipal.isAdmin) {
                throw TestPrincipalNotAdminException()
            }
        }

        override fun ensureSignedIn(): AppPrincipal {
            val principal = providedPrincipal ?: throw TestPrincipalMissingException()
            return principal
        }
    }

    private class AllowSecurityRuleEvaluator : SecurityRuleEvaluator {
        override val key: String = RULE_ALLOW

        override fun evaluate(ctx: SecurityRuleCtx): SecurityRuleEvaluatorResult {
            return SecurityRuleEvaluatorResult.Ok()
        }
    }

    private class DenySecurityRuleEvaluator : SecurityRuleEvaluator {
        override val key: String = RULE_DENY

        override fun evaluate(ctx: SecurityRuleCtx): SecurityRuleEvaluatorResult {
            return SecurityRuleEvaluatorResult.Error("blocked")
        }
    }

    private class TestActionCtxDispatchException : MedatarunException("dispatch not supported for tests")
    private class TestActionCtxServiceNotFoundException(type: KClass<*>) :
        MedatarunException("Service not found for ${type.simpleName}")

    private class TestPrincipalNotAdminException : MedatarunException("Principal is not admin")
    private class TestPrincipalMissingException : MedatarunException("Principal is missing")

    private companion object {
        const val RULE_ALLOW = "allow"
        const val RULE_DENY = "deny"
    }
}
