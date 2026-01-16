package io.medatarun.security

import io.medatarun.lang.exceptions.MedatarunException

class SecurityRuleEvaluatorNotFoundException(ruleKey: String) : MedatarunException("Security rule not found with key [$ruleKey]")