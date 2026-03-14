package io.medatarun.actions.infra.db

import io.medatarun.actions.ports.needs.ActionAuditRecorder
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.platform.db.DbMigration
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx
import io.medatarun.platform.kernel.MedatarunServiceCtx
import io.medatarun.platform.kernel.getService
import java.time.Instant

class PlatformActionsStorageDbExtension(
    val config: PlatformActionsStorageDbExtensionConfig
) : MedatarunExtension {
    constructor() : this(PlatformActionsStorageDbExtensionConfigProd())

    override val id: String = "platform-actions-storage-db"

    override fun initServices(ctx: MedatarunServiceCtx) {
        val dbConnectionFactory = ctx.getService(DbConnectionFactory::class)
        ctx.register(ActionAuditRecorderDb::class, ActionAuditRecorderDb(dbConnectionFactory, config.actionAuditClock))
    }

    override fun initContributions(ctx: MedatarunExtensionCtx) {
        ctx.registerContribution(ActionAuditRecorder::class, ctx.getService<ActionAuditRecorderDb>())
        ctx.registerContribution(DbMigration::class, ActionAuditRecorderDbMigration(id))
    }
}

interface PlatformActionsStorageDbExtensionConfig {
    val actionAuditClock: ActionAuditClock
}

class PlatformActionsStorageDbExtensionConfigProd : PlatformActionsStorageDbExtensionConfig {
    override val actionAuditClock = object : ActionAuditClock {
        override fun now(): Instant = Instant.now()
    }
}
