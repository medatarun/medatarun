package io.medatarun.model.security

import io.medatarun.security.*

class ModelSecurityRulesProvider : SecurityRulesProvider {

    val list = listOf<SecurityRuleEvaluator>(
        fromPermission(ModelSecurityRules.MODEL_COPY, ModelCopyPermission),
        fromPermission(ModelSecurityRules.MODEL_CREATE_MANUAL, ModelCreatePermission),
        fromPermission(ModelSecurityRules.MODEL_DELETE, ModelDeletePermission),
        fromPermission(ModelSecurityRules.MODEL_IMPORT, ModelImportPermission),
        fromPermission(ModelSecurityRules.MODEL_READ, ModelReadPermission),
        fromPermission(ModelSecurityRules.MODEL_RELEASE, ModelReleasePermission),
        fromPermission(ModelSecurityRules.MODEL_UPDATE_AUTHORITY, ModelUpdateAuthorityPermission),
        fromPermission(ModelSecurityRules.MODEL_WRITE, ModelWritePermission),
    )

    fun fromPermission(code: String, permission: AppPermission): SecurityRuleEvaluator {
        return object : SecurityRuleEvaluator {
            override val key: String = code
            override val name: String = permission.name ?: code
            override val description: String = permission.description ?: code
            override fun evaluate(ctx: SecurityRuleCtx): SecurityRuleEvaluatorResult =
                ctx.ensurePermission(permission)

            override fun associatedRequiredPermissions(): List<AppPermission> {
                return listOf(permission)
            }
        }
    }

    override fun getRules(): List<SecurityRuleEvaluator> = list

}