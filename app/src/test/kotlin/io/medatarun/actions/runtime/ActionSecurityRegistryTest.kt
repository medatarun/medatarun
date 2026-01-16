package io.medatarun.actions.runtime

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionPrincipalCtx
import io.medatarun.actions.ports.needs.ActionRequest
import io.medatarun.security.*
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.reflect.KClass

class ActionSecurityRuleEvaluatorsTest {

    @Test
    fun `findEvaluatorOptional returns evaluator when present`() {
        val evaluator = DummyEvaluator("rule.test")
        val registry = ActionSecurityRuleEvaluators(listOf(evaluator))

        assertSame(evaluator, registry.findEvaluatorOptional("rule.test"))
    }

    @Test
    fun `evaluateSecurity throws when rule is missing`() {
        val registry = ActionSecurityRuleEvaluators(emptyList())

        assertThrows<SecurityRuleEvaluatorNotFoundException> {
            registry.evaluateSecurity("missing", dummyActionCtx())
        }
    }

    @Test
    fun `evaluateSecurity delegates to evaluator`() {
        val evaluator = RecordingEvaluator("rule.test")
        val registry = ActionSecurityRuleEvaluators(listOf(evaluator))

        val result = registry.evaluateSecurity("rule.test", dummyActionCtx())

        assertTrue(result is SecurityRuleEvaluatorResult.Ok)
        assertTrue(evaluator.called)
    }

    private class DummyEvaluator(override val key: String) : SecurityRuleEvaluator {
        override fun evaluate(ctx: SecurityRuleCtx) =
            SecurityRuleEvaluatorResult.Ok()
    }

    private class RecordingEvaluator(override val key: String) : SecurityRuleEvaluator {
        var called = false
        override fun evaluate(ctx: SecurityRuleCtx): SecurityRuleEvaluatorResult {
            called = true
            return SecurityRuleEvaluatorResult.Ok()
        }
    }

    private fun dummyActionCtx(): ActionCtx =
        object : ActionCtx {
            override val principal = object : ActionPrincipalCtx {
                override val principal: AppPrincipal? = null
                override fun ensureIsAdmin() = error("not used")
                override fun ensureSignedIn(): AppPrincipal = error("not used")
            }

            override val extensionRegistry get() = error("not used")
            override fun dispatchAction(req: ActionRequest): Any = error("not used")
            override fun <T : Any> getService(type: KClass<T>): T = error("not used")
        }
}
