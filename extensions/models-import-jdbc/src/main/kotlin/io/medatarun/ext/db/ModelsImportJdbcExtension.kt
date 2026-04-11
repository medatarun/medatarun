package io.medatarun.ext.db

import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.ext.db.actions.DatabasesActionProvider
import io.medatarun.ext.db.domain.DbDriverManager
import io.medatarun.ext.db.domain.DbConnectionRegistry
import io.medatarun.ext.db.internal.connection.DbConnectionRegistryImpl
import io.medatarun.ext.db.internal.drivers.DbDriverManagerImpl
import io.medatarun.ext.db.internal.modelimport.DbModelImporter
import io.medatarun.model.ports.needs.ModelImporter
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx
import org.slf4j.LoggerFactory

class ModelsImportJdbcExtension(
    private val config: Config? = null
) : MedatarunExtension {

    override val id = "models-import-jdbc"
    override fun initContributions(ctx: MedatarunExtensionCtx) {
        // read configuration
        val datasourcesPath = ctx.resolveApplicationHomePath("config/datasources")
        logger.debug("datasourcesPath: {}", datasourcesPath)
        val driversJsonPath = datasourcesPath.resolve("drivers.json")
        logger.debug("driversJsonPath: {}", driversJsonPath)
        val driversPath = datasourcesPath.resolve("jdbc-drivers")
        logger.debug("driversPath: {}", driversPath)
        val connexionsJsonPath = datasourcesPath.resolve("datasources.json")
        logger.debug("datasourcesJsonPath: {}", connexionsJsonPath)

        val dbDriverManager: DbDriverManager = config?.customDriverManager ?: DbDriverManagerImpl(driversJsonPath, driversPath)
        val dbConnectionRegistry = config?.customRegistry ?: DbConnectionRegistryImpl(connexionsJsonPath)

        val dbModelImporter = DbModelImporter(dbDriverManager, dbConnectionRegistry)
        val dbActionProvider = DatabasesActionProvider(dbDriverManager, dbConnectionRegistry)

        ctx.registerContribution(ModelImporter::class, dbModelImporter)
        ctx.registerContribution(ActionProvider::class, dbActionProvider)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ModelsImportJdbcExtension::class.java)
    }

    class Config(
        val customRegistry: DbConnectionRegistry,
        val customDriverManager: DbDriverManager
    )

}