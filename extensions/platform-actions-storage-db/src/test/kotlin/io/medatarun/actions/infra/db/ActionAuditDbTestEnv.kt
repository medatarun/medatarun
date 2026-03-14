package io.medatarun.actions.infra.db

import com.google.common.jimfs.Jimfs
import io.medatarun.actions.ActionsExtension
import io.medatarun.actions.adapters.ActionPlatform
import io.medatarun.actions.infra.db.records.ActionAuditEventRecord
import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionDoc
import io.medatarun.actions.ports.needs.ActionDocSemantics
import io.medatarun.actions.ports.needs.ActionDocSemanticsMode
import io.medatarun.actions.ports.needs.ActionPrincipalCtx
import io.medatarun.actions.ports.needs.ActionPayload
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.actions.ports.needs.ActionRequest
import io.medatarun.actions.ports.needs.ActionRequestCtx
import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.platform.db.DbMigrationChecker
import io.medatarun.platform.db.PlatformStorageDbExtension
import io.medatarun.platform.db.sqlite.DbProviderSqlite
import io.medatarun.platform.db.sqlite.PlatformStorageDbSqliteExtension
import io.medatarun.platform.kernel.MedatarunConfig
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx
import io.medatarun.platform.kernel.MedatarunServiceCtx
import io.medatarun.platform.kernel.PlatformBuilder
import io.medatarun.platform.kernel.getService
import io.medatarun.security.AppPrincipal
import io.medatarun.security.AppPrincipalId
import io.medatarun.security.AppPrincipalRole
import io.medatarun.security.SecurityExtension
import io.medatarun.security.SecurityRuleCtx
import io.medatarun.security.SecurityRuleEvaluator
import io.medatarun.security.SecurityRuleEvaluatorResult
import io.medatarun.security.SecurityRulesProvider
import io.medatarun.types.TypeSystemExtension
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

class ActionAuditDbTestEnv {
    private val extensions = listOf(
        TypeSystemExtension(),
        SecurityExtension(),
        ActionsExtension(),
        PlatformStorageDbExtension(),
        PlatformStorageDbSqliteExtension(),
        PlatformActionsStorageDbExtension(),
        TestActionsStorageDbExtension()
    )

    val platform = PlatformBuilder(
        config = MedatarunConfig.createTempConfig(
            Jimfs.newFileSystem(),
            mapOf(
                PlatformStorageDbSqliteExtension.JDBC_URL_PROPERTY to DbProviderSqlite.randomDbUrl()
            )
        ),
        extensions = extensions
    ).buildAndStart()

    val dbMigrationChecker: DbMigrationChecker
        get() = platform.services.getService<DbMigrationChecker>()

    private val actionPlatform: ActionPlatform
        get() = platform.services.getService<ActionPlatform>()

    fun dispatch(action: TestDbAction): Any? {
        val request = ActionRequest(
            TestActionProvider.ACTION_GROUP_KEY,
            action::class.findAnnotation<ActionDoc>()!!.key,
            ActionPayload.AsRaw(action)
        )
        return actionPlatform.invoker.handleInvocation(request, testActionRequestContext)
    }

    fun auditRows(): List<ActionAuditEventRecord> {
        val dbConnectionFactory = platform.services.getService<DbConnectionFactory>()
        val actionAuditRecorderDb = ActionAuditRecorderDb(dbConnectionFactory)
        return actionAuditRecorderDb.findAll()
    }

    private class TestActionsStorageDbExtension : MedatarunExtension {
        override val id: String = "platform-actions-storage-db-test"

        override fun initContributions(ctx: MedatarunExtensionCtx) {
            ctx.registerContribution(ActionProvider::class, TestActionProvider())
            ctx.registerContribution(SecurityRulesProvider::class, TestSecurityRulesProvider)
        }
    }

    private object TestSecurityRulesProvider : SecurityRulesProvider {
        override fun getRules(): List<SecurityRuleEvaluator> {
            return listOf(AllowSecurityRuleEvaluator(), DenySecurityRuleEvaluator())
        }
    }

    sealed interface TestDbAction {
        @ActionDoc(
            key = "business-ok",
            title = "Business ok",
            description = "Successful action",
            uiLocations = [""],
            securityRule = RULE_ALLOW,
            semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
        )
        data object Ok : TestDbAction

        @ActionDoc(
            key = "security-denied",
            title = "Security denied",
            description = "Rejected by security",
            uiLocations = [""],
            securityRule = RULE_DENY,
            semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
        )
        data object Denied : TestDbAction

        @ActionDoc(
            key = "business-fails",
            title = "Business fails",
            description = "Fails during business invoke",
            uiLocations = [""],
            securityRule = RULE_ALLOW,
            semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
        )
        data object BusinessFails : TestDbAction
    }

    private class TestActionProvider : ActionProvider<TestDbAction> {
        override val actionGroupKey: String = ACTION_GROUP_KEY

        override fun findCommandClass(): KClass<TestDbAction> {
            return TestDbAction::class
        }

        override fun dispatch(action: TestDbAction, actionCtx: ActionCtx): Any {
            if (action is TestDbAction.BusinessFails) {
                throw TestActionBusinessFailureException()
            }
            return "ok"
        }

        companion object {
            const val ACTION_GROUP_KEY = "test-db-audit"
        }
    }

    private class AllowSecurityRuleEvaluator : SecurityRuleEvaluator {
        override val key: String = RULE_ALLOW
        override val name: String = "Allow"
        override val description: String = "Allow all in tests."

        override fun evaluate(ctx: SecurityRuleCtx): SecurityRuleEvaluatorResult {
            return SecurityRuleEvaluatorResult.Ok()
        }
    }

    private class DenySecurityRuleEvaluator : SecurityRuleEvaluator {
        override val key: String = RULE_DENY
        override val name: String = "Deny"
        override val description: String = "Deny all in tests."

        override fun evaluate(ctx: SecurityRuleCtx): SecurityRuleEvaluatorResult {
            return SecurityRuleEvaluatorResult.Error("blocked")
        }
    }

    class TestActionBusinessFailureException : MedatarunException("boom")

    companion object {
        const val RULE_ALLOW = "allow"
        const val RULE_DENY = "deny"

        private val testPrincipal = object : AppPrincipal {
            override val id: AppPrincipalId = AppPrincipalId("user")
            override val issuer: String = ""
            override val subject: String = ""
            override val isAdmin: Boolean = false
            override val fullname: String = "user"
            override val roles: List<AppPrincipalRole> = emptyList()
        }

        private val testPrincipalCtx = object : ActionPrincipalCtx {
            override fun ensureIsAdmin() {
            }

            override fun ensureSignedIn(): AppPrincipal {
                return testPrincipal
            }

            override val principal: AppPrincipal
                get() = testPrincipal
        }

        val testActionRequestContext = object : ActionRequestCtx {
            override val principal: ActionPrincipalCtx = testPrincipalCtx
            override val source: String = "test"
        }
    }
}
