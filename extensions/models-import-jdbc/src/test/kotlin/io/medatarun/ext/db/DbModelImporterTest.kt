package io.medatarun.ext.db

import io.medatarun.ext.db.domain.DbConnectionNotFoundException
import io.medatarun.ext.db.fixtures.ModelImportJdbcTestEnv
import io.medatarun.model.domain.EntityAttributeRef.Companion.attributeRefKey
import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.RelationshipCardinality
import io.medatarun.model.domain.RelationshipRoleKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
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
    fun `that table with one primary key we have primary key in model`() {
        val dbPath = ModelImportJdbcTestEnv.createSqliteDatabaseFile()
        val datasource = ModelImportJdbcTestEnv.sqliteDatasource("demo", dbPath)
        val env = ModelImportJdbcTestEnv(
            datasources = listOf(datasource),
            schemaStatements = listOf(
                "CREATE TABLE books (identifier INTEGER PRIMARY KEY, title TEXT NOT NULL)",
                "CREATE TABLE author (identifier INTEGER PRIMARY KEY, name TEXT NOT NULL)"
            )
        )

        val imported = env.importer.toModel(
            path = "datasource:demo",
            resourceLocator = env.resourceLocator,
            modelKeyChoosen = ModelKey("jdbc-pk-single"),
            modelNameChoosen = "JDBC PK Single"
        )

        val books = imported.model.findEntity(EntityKey("books"))
        val booksPK = imported.model.findEntityPrimaryKeyOptional(books.id)
        val bookIdentifier = imported.model.findEntityAttribute(books.ref, attributeRefKey("identifier"))
        assertEquals(2, imported.model.entityPrimaryKeys.size)
        assertNotNull(booksPK)
        assertTrue(booksPK.participants.any { it.position == 0 && it.attributeId == bookIdentifier.id})

        val author = imported.model.findEntity(EntityKey("author"))
        val authorPK = imported.model.findEntityPrimaryKeyOptional(author.id)
        val authorIdentifier = imported.model.findEntityAttribute(author.ref, attributeRefKey("identifier"))
        assertNotNull(authorPK)
        assertEquals(2, imported.model.entityPrimaryKeys.size)
        assertTrue(authorPK.participants.any { it.position == 0 && it.attributeId == authorIdentifier.id})
    }

    @Test
    fun `that table with composite primary key we have primary key in model`() {
        val dbPath = ModelImportJdbcTestEnv.createSqliteDatabaseFile()
        val datasource = ModelImportJdbcTestEnv.sqliteDatasource("demo", dbPath)
        val env = ModelImportJdbcTestEnv(
            datasources = listOf(datasource),
            schemaStatements = listOf(
                "CREATE TABLE user_roles (user_id INTEGER NOT NULL, role_id INTEGER NOT NULL, PRIMARY KEY (user_id, role_id))"
            )
        )

        val imported = env.importer.toModel(
            path = "datasource:demo",
            resourceLocator = env.resourceLocator,
            modelKeyChoosen = ModelKey("jdbc-pk-composite"),
            modelNameChoosen = "JDBC PK Composite"
        )

        val userRoles = imported.model.findEntity(EntityKey("user_roles"))
        val userRolesPrimaryKey = imported.model.entityPrimaryKeys.first { it.entityId == userRoles.id }
        val userRolesAttributes = imported.model.findEntityAttributes(userRoles.ref)
        val pkAttributeNames = userRolesPrimaryKey.participants.map { participant ->
            userRolesAttributes.first { it.id == participant.attributeId }.key.value
        }.toSet()

        assertEquals(1, imported.model.entityPrimaryKeys.size)
        assertEquals(2, userRolesPrimaryKey.participants.size)
        assertEquals(setOf("user_id", "role_id"), pkAttributeNames)
    }

    @Test
    fun `toModel imports schema from sqlite datasource`() {
        // TODO this tes must be split in multiple tests, too big
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
            modelKeyChoosen = ModelKey("jdbc-test-model"),
            modelNameChoosen = "JDBC Test Model"
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

        assertTrue(imported.model.isEntityPrimaryKey(users.id, usersId.id))
        assertTrue(imported.model.isEntityPrimaryKey(orders.id, ordersId.id))
        assertTrue(imported.model.isEntityPrimaryKey(profiles.id, profilesId.id))

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
                modelKeyChoosen = ModelKey("ignored"),
                modelNameChoosen = "Ignored"
            )
        }
    }
}
