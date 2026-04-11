package io.medatarun.ext.db

import io.medatarun.ext.db.domain.DbConnectionNotFoundException
import io.medatarun.ext.db.fixtures.ModelImportJdbcTestEnv
import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.RelationshipCardinality
import io.medatarun.model.domain.RelationshipRoleKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DbModelImporterTest {
    @Test
    fun `accept returns true for datasource prefix`() {
        val dbPath = ModelImportJdbcTestEnv.createSqliteDatabaseFile()
        val datasource = ModelImportJdbcTestEnv.sqliteDatasource("demo", dbPath)
        val env = ModelImportJdbcTestEnv(
            datasources = listOf(datasource),
            schemaStatements = emptyList()
        )

        assertTrue(env.importer.accept("datasource:demo", env.resourceLocator))
        assertFalse(env.importer.accept("file:demo", env.resourceLocator))
    }

    @Test
    fun `toModel imports schema from sqlite datasource`() {
        val dbPath = ModelImportJdbcTestEnv.createSqliteDatabaseFile()
        val datasource = ModelImportJdbcTestEnv.sqliteDatasource("demo", dbPath)
        val env = ModelImportJdbcTestEnv(
            datasources = listOf(datasource),
            schemaStatements = listOf(
                "CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT NOT NULL)",
                "CREATE TABLE orders (id INTEGER PRIMARY KEY, user_id INTEGER NOT NULL, amount REAL NOT NULL, FOREIGN KEY (user_id) REFERENCES users(id))",
                "CREATE TABLE profiles (id INTEGER PRIMARY KEY, user_id INTEGER, FOREIGN KEY (user_id) REFERENCES users(id))"
            )
        )

        val imported = env.importer.toModel(
            path = "datasource:demo",
            resourceLocator = env.resourceLocator,
            modelKey = ModelKey("jdbc-test-model"),
            modelName = "JDBC Test Model"
        )

        assertEquals(ModelKey("jdbc-test-model"), imported.model.key)
        assertEquals("JDBC Test Model", imported.model.name?.name)
        assertEquals(3, imported.model.entities.size)
        assertEquals(7, imported.model.attributes.size)
        assertEquals(2, imported.model.relationships.size)

        val users = imported.model.findEntity(EntityKey("users"))
        val orders = imported.model.findEntity(EntityKey("orders"))
        val profiles = imported.model.findEntity(EntityKey("profiles"))

        val usersAttributes = imported.model.findEntityAttributes(users.ref)
        val ordersAttributes = imported.model.findEntityAttributes(orders.ref)
        val profilesAttributes = imported.model.findEntityAttributes(profiles.ref)

        val usersId = usersAttributes.first { it.key.value == "id" }
        val ordersId = ordersAttributes.first { it.key.value == "id" }
        val profilesId = profilesAttributes.first { it.key.value == "id" }

        assertEquals(usersId.id, users.identifierAttributeId)
        assertEquals(ordersId.id, orders.identifierAttributeId)
        assertEquals(profilesId.id, profiles.identifierAttributeId)

        val orderRelationship = imported.model.relationships.first { relationship ->
            relationship.roles.any { role -> role.key == RelationshipRoleKey("orders.user_id") }
        }
        val profileRelationship = imported.model.relationships.first { relationship ->
            relationship.roles.any { role -> role.key == RelationshipRoleKey("profiles.user_id") }
        }

        val ordersRole = orderRelationship.roles.first { role -> role.key == RelationshipRoleKey("orders.user_id") }
        val profilesRole = profileRelationship.roles.first { role -> role.key == RelationshipRoleKey("profiles.user_id") }

        assertEquals(RelationshipCardinality.One, ordersRole.cardinality)
        assertEquals(RelationshipCardinality.ZeroOrOne, profilesRole.cardinality)
    }

    @Test
    fun `toModel throws when datasource is unknown`() {
        val env = ModelImportJdbcTestEnv(
            datasources = emptyList(),
            schemaStatements = emptyList()
        )

        assertFailsWith<DbConnectionNotFoundException> {
            env.importer.toModel(
                path = "datasource:unknown",
                resourceLocator = env.resourceLocator,
                modelKey = ModelKey("ignored"),
                modelName = "Ignored"
            )
        }
    }
}
