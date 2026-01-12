package io.medatarun.security

interface SecurityRulesProvider {
    fun getRules(): List<SecurityRuleEvaluator>
}