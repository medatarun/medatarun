package io.medatarun.ext.db

import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.ext.db.actions.DatabasesActionProvider
import io.medatarun.ext.db.internal.connection.DbConnectionRegistry
import io.medatarun.ext.db.internal.drivers.DbDriverManager
import io.medatarun.ext.db.internal.modelimport.DbModelImporter
import io.medatarun.kernel.MedatarunExtension
import io.medatarun.kernel.MedatarunExtensionCtx
import io.medatarun.model.ports.needs.ModelImporter
import org.slf4j.LoggerFactory

class DbExtension : MedatarunExtension {

    override val id = "db"
    override fun init(ctx: MedatarunExtensionCtx) {
        val datasourcesPath = ctx.resolveApplicationHomePath("config/datasources")
        logger.debug("datasourcesPath: {}", datasourcesPath)
        val driversJsonPath = datasourcesPath.resolve("drivers.json")
        logger.debug("driversJsonPath: {}", driversJsonPath)
        val driversPath = datasourcesPath.resolve("jdbc-drivers")
        logger.debug("driversPath: {}", driversPath)
        val dbDriverManager = DbDriverManager(driversJsonPath, driversPath)
        val connexionsJsonPath = datasourcesPath.resolve("datasources.json")
        logger.debug("datasourcesJsonPath: {}", connexionsJsonPath)
        val dbConnectionRegistry = DbConnectionRegistry(connexionsJsonPath)
        ctx.register(ModelImporter::class, DbModelImporter(dbDriverManager, dbConnectionRegistry))
        ctx.register(ActionProvider::class, DatabasesActionProvider(dbDriverManager, dbConnectionRegistry))
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DbExtension::class.java)
    }
}