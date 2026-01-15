package io.medatarun.actions.runtime

import io.medatarun.actions.adapters.SecurityRuleCtxAction
import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.security.SecurityRuleEvaluator
import io.medatarun.security.SecurityRuleEvaluatorNotFoundException
import io.medatarun.security.SecurityRuleEvaluatorResult

class ActionSecurityRuleEvaluators(ruleList: List<SecurityRuleEvaluator>) {

    private val rules = ruleList.associateBy { it.key }

    fun findEvaluatorOptional(ruleKey: String): SecurityRuleEvaluator? {
        return rules[ruleKey]
    }

    fun evaluateSecurity(ruleKey: String, actionCtx: ActionCtx): SecurityRuleEvaluatorResult {
        val e = rules[ruleKey] ?: throw SecurityRuleEvaluatorNotFoundException(ruleKey)
        val securityCtx = SecurityRuleCtxAction(actionCtx)
        return e.evaluate(securityCtx)
    }
}

