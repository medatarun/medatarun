package io.medatarun.ext.db.fixtures

import com.google.common.jimfs.Jimfs
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.ext.db.ModelsImportJdbcExtension
import io.medatarun.ext.db.domain.DbConnectionRegistry
import io.medatarun.ext.db.domain.DbConnectionSecret
import io.medatarun.ext.db.domain.DbDatasource
import io.medatarun.ext.db.domain.DbDriverInfo
import io.medatarun.ext.db.domain.DbDriverManager
import io.medatarun.model.ports.needs.ModelImporter
import io.medatarun.platform.kernel.ExtensionId
import io.medatarun.platform.kernel.MedatarunConfig
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx
import io.medatarun.platform.kernel.PlatformBuilder
import io.medatarun.platform.kernel.PlatformRuntime
import io.medatarun.platform.kernel.ResourceLocator
import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager

class ModelImportJdbcTestEnv(
    val datasources: List<DbDatasource>,
    val schemaStatements: List<String>
) {
    val resourceLocator: ResourceLocator
    val runtime: PlatformRuntime
    val importer: ModelImporter

    init {
        if (datasources.isNotEmpty() && schemaStatements.isNotEmpty()) {
            initializeSchema(datasources.first(), schemaStatements)
        }

        val extension = ModelsImportJdbcExtension(
            ModelsImportJdbcExtension.Config(
                customRegistry = TestDbConnectionRegistry(datasources),
                customDriverManager = TestDbDriverManager()
            )
        )
        runtime = PlatformBuilder(
            config = MedatarunConfig.createTempConfig(
                fs = Jimfs.newFileSystem(),
                props = emptyMap()
            ),
            extensions = listOf(
                TestContributionPointsExtension(),
                extension
            )
        ).buildAndStart()
        importer = runtime.extensions.findContributionsFlat(ModelImporter::class).single()
        resourceLocator = runtime.config.createResourceLocator()
    }

    companion object {
        fun sqliteDatasource(name: String, dbPath: Path): DbDatasource {
            return DbDatasource(
                name = name,
                driver = "sqlite",
                url = "jdbc:sqlite:${dbPath.toAbsolutePath()}",
                username = "",
                secret = DbConnectionSecret(storage = "RAW", value = ""),
                properties = emptyMap()
            )
        }

        fun createSqliteDatabaseFile(): Path {
            return Files.createTempFile("medatarun-modelimport-jdbc-test-", ".sqlite")
        }
    }

    private fun initializeSchema(datasource: DbDatasource, statements: List<String>) {
        TestDbDriverManager().getConnection(datasource).use { connection ->
            executeStatements(connection, statements)
        }
    }

    private fun executeStatements(connection: Connection, statements: List<String>) {
        for (statement in statements) {
            connection.createStatement().use { sql ->
                sql.execute(statement)
            }
        }
    }

    private class TestDbConnectionRegistry(datasources: List<DbDatasource>) : DbConnectionRegistry {
        private val byName = datasources.associateBy { it.name }

        override fun findByNameOptional(connectionName: String): DbDatasource? {
            return byName[connectionName]
        }

        override fun listConnections(): List<DbDatasource> {
            return byName.values.toList()
        }
    }

    private class TestDbDriverManager : DbDriverManager {
        override fun getConnection(connection: DbDatasource): Connection {
            Class.forName("org.sqlite.JDBC")
            return DriverManager.getConnection(connection.url)
        }

        override fun listDrivers(): List<DbDriverInfo> {
            return emptyList()
        }
    }

    private class TestContributionPointsExtension : MedatarunExtension {
        override val id: ExtensionId = "models-import-jdbc-test-contributions"

        override fun initContributions(ctx: MedatarunExtensionCtx) {
            ctx.registerContributionPoint(this.id + ".model-importer", ModelImporter::class)
            ctx.registerContributionPoint(this.id + ".action-provider", ActionProvider::class)
        }
    }
}
