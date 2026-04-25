package io.medatarun.actions.infra.db

import com.google.common.jimfs.Jimfs
import io.medatarun.actions.ActionsExtension
import io.medatarun.actions.adapters.ActionPlatform
import io.medatarun.actions.infra.db.records.ActionAuditEventRecord
import io.medatarun.actions.ports.needs.*
import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.platform.db.DbMigrationChecker
import io.medatarun.platform.db.PlatformStorageDbExtension
import io.medatarun.platform.db.postgresql.PlatformStorageDbPostgresqlExtension
import io.medatarun.platform.db.sqlite.PlatformStorageDbSqliteExtension
import io.medatarun.platform.db.testkit.TestDbConfig
import io.medatarun.platform.kernel.*
import io.medatarun.security.*
import io.medatarun.type.commons.id.Id
import io.medatarun.types.TypeSystemExtension
import java.time.Instant
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

class ActionAuditDbTestEnv {
    val actionAuditClockTests = ActionAuditClockTester()

    private val extensions = listOf(
        TypeSystemExtension(),
        SecurityExtension(),
        ActionsExtension(),
        PlatformStorageDbExtension(),
        PlatformStorageDbSqliteExtension(),
        PlatformStorageDbPostgresqlExtension(),
        PlatformActionsStorageDbExtension(
            object : PlatformActionsStorageDbExtensionConfig {
                override val actionAuditClock: ActionAuditClock
                    get() = actionAuditClockTests
            }
        ),
        TestActionsStorageDbExtension()
    )

    val platform = PlatformBuilder(
        config = MedatarunConfig.createTempConfig(
            Jimfs.newFileSystem(),
            TestDbConfig().testDatabaseProperties()
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
        val actionAuditRecorderDb = ActionAuditRecorderDb(dbConnectionFactory, actionAuditClockTests)
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
            securityRule = RULE_ALLOW,
            semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
        )
        data object Ok : TestDbAction {
            // Actions sent "asRaw" must have a string representation to be auditable because the database stores the original Json payload
            override fun toString(): String {
                return "{\"action\":\"business-ok\"}"
            }
        }

        @ActionDoc(
            key = "security-denied",
            title = "Security denied",
            description = "Rejected by security",
            securityRule = RULE_DENY,
            semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
        )
        data object Denied : TestDbAction {
            // Actions sent "asRaw" must have a string representation to be auditable because the database stores the original Json payload
            override fun toString(): String {
                return "{\"action\":\"security-denied\"}"
            }
        }

        @ActionDoc(
            key = "business-fails",
            title = "Business fails",
            description = "Fails during business invoke",
            securityRule = RULE_ALLOW,
            semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
        )
        data object BusinessFails : TestDbAction {
            // Actions sent "asRaw" must have a string representation to be auditable because the database stores the original Json payload
            override fun toString(): String {
                return "{\"action\":\"business-fails\"}"
            }
        }
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
            return SecurityRuleEvaluatorResult.AuthorizationError("blocked")
        }
    }

    class TestActionBusinessFailureException : MedatarunException("boom")

    companion object {
        const val RULE_ALLOW = "allow"
        const val RULE_DENY = "deny"

        private val testPrincipal = object : AppPrincipal {
            override val id: AppActorId = Id.generate(::AppActorId)
            override val issuer: String = ""
            override val subject: String = ""
            override val isAdmin: Boolean = false
            override val fullname: String = "user"
            override val permissions: Set<AppPermissionKey> = emptySet()
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
            override val principalCtx: ActionPrincipalCtx = testPrincipalCtx
            override val source: String = "test"
        }
    }

    class ActionAuditClockTester(
        var staticNow: Instant = Instant.parse("2026-01-02T03:04:05Z")
    ) : ActionAuditClock {
        override fun now(): Instant {
            return staticNow
        }
    }
}
