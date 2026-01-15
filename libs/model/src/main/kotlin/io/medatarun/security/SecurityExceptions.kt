package io.medatarun.security

import io.medatarun.model.domain.MedatarunException

class SecurityRuleEvaluatorNotFoundException(ruleKey: String) : MedatarunException("Security rule not found with key [$ruleKey]") {}