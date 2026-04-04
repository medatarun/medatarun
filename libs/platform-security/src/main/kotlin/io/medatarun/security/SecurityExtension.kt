package io.medatarun.security

import io.medatarun.platform.kernel.ExtensionId
import io.medatarun.platform.kernel.ExtensionRegistry
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx
import io.medatarun.platform.kernel.MedatarunServiceCtx

class SecurityExtension(
    val config: SecurityExtensionConfig? = null
) : MedatarunExtension {
    override val id: ExtensionId = "platform-security"

    override fun initContributions(ctx: MedatarunExtensionCtx) {
        ctx.registerContributionPoint(this.id + ".security_rules_providers", SecurityRulesProvider::class)
        ctx.registerContributionPoint(this.id + ".security_permissions_providers", SecurityPermissionsProvider::class)
        ctx.registerContribution(SecurityRulesProvider::class, SecurityRulesProviderBase())
    }

    override fun initServices(ctx: MedatarunServiceCtx) {
        val extensionRegistry = ctx.getService(ExtensionRegistry::class)
        val securityRolesRegistry = SecurityRolesRegistryImpl(extensionRegistry)
        ctx.register(SecurityRolesRegistry::class, securityRolesRegistry)
        if (config?.appActorResolver != null) {
            ctx.register(AppActorResolver::class, config.appActorResolver)
        }
    }
}

data class SecurityExtensionConfig(
    val appActorResolver: AppActorResolver?
)

class SecurityRulesProviderBase : SecurityRulesProvider {

    override fun getRules(): List<SecurityRuleEvaluator> {
        val rulePublic: SecurityRuleEvaluator = object : SecurityRuleEvaluator {
            override val key: String = SecurityRuleNames.PUBLIC
            override val name: String = "Public"
            override val description: String =
                "No authentication is required.\n\n" +
                    "Intended for software discovery (help and documentation)."
            override fun evaluate(ctx: SecurityRuleCtx) = SecurityRuleEvaluatorResult.Ok()
        }
        val ruleSignedIn: SecurityRuleEvaluator = object : SecurityRuleEvaluator {
            override val key: String = SecurityRuleNames.SIGNED_IN
            override val name: String = "Signed In"
            override val description: String =
                "Authenticated actors (users or tools) are authorized."
            override fun evaluate(ctx: SecurityRuleCtx) =
                if (ctx.isSignedIn()) SecurityRuleEvaluatorResult.Ok() else SecurityRuleEvaluatorResult.AuthenticationError("You must be signed in.")
        }
        val ruleAdmin: SecurityRuleEvaluator = object : SecurityRuleEvaluator {
            override val key: String = SecurityRuleNames.ADMIN
            override val name: String = "Administrator"
            override val description: String =
                "Only actors (users or tools) with the administrator privileges are authorized."
            override fun evaluate(ctx: SecurityRuleCtx): SecurityRuleEvaluatorResult {
                if (!ctx.isSignedIn()) return SecurityRuleEvaluatorResult.AuthenticationError("You must be signed in.")
                if (!ctx.isAdmin()) return SecurityRuleEvaluatorResult.AuthorizationError("You must have an administrator privilege.")
                return SecurityRuleEvaluatorResult.Ok()
            }
        }
        return listOf(rulePublic, ruleAdmin, ruleSignedIn)
    }

}
