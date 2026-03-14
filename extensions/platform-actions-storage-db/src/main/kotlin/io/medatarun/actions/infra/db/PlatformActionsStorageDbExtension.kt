package io.medatarun.actions.infra.db

import io.medatarun.actions.ports.needs.ActionAuditRecorder
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.platform.db.DbMigration
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx
import io.medatarun.platform.kernel.MedatarunServiceCtx
import io.medatarun.platform.kernel.getService

class PlatformActionsStorageDbExtension : MedatarunExtension {
    override val id: String = "platform-actions-storage-db"

    override fun initServices(ctx: MedatarunServiceCtx) {
        val dbConnectionFactory = ctx.getService(DbConnectionFactory::class)
        ctx.register(ActionAuditRecorderDb::class, ActionAuditRecorderDb(dbConnectionFactory))
    }

    override fun initContributions(ctx: MedatarunExtensionCtx) {
        ctx.registerContribution(ActionAuditRecorder::class, ctx.getService<ActionAuditRecorderDb>())
        ctx.registerContribution(DbMigration::class, ActionAuditRecorderDbMigration(id))
    }
}
