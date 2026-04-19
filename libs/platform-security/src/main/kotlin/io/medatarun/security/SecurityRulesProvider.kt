package io.medatarun.security

import io.medatarun.platform.kernel.ServiceContributionPoint

interface SecurityRulesProvider: ServiceContributionPoint {
    fun getRules(): List<SecurityRuleEvaluator>
}