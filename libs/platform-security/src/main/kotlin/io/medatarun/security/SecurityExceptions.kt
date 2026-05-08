package io.medatarun.security

import io.medatarun.lang.exceptions.MedatarunTechnicalException

class SecurityRuleEvaluatorNotFoundException(ruleKey: String) : MedatarunTechnicalException("Security rule not found with key [$ruleKey]")