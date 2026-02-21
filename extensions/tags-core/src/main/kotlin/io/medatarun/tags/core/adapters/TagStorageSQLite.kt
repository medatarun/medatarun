package io.medatarun.tags.core.adapters

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.tags.core.domain.*
import io.medatarun.tags.core.internal.TagFreeInMemory
import io.medatarun.tags.core.internal.TagGroupInMemory
import io.medatarun.tags.core.internal.TagManagedInMemory
import io.medatarun.tags.core.ports.needs.TagRepoCmd
import io.medatarun.tags.core.ports.needs.TagStorage
import org.intellij.lang.annotations.Language
import org.slf4j.LoggerFactory

class TagStorageSQLite(private val dbConnectionFactory: DbConnectionFactory): TagStorage {

    fun initSchema() {
        dbConnectionFactory.getConnection().use { connection ->
            SCHEMA.split(";")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .forEach { stmt ->
                    connection.createStatement().execute(stmt)
                }
        }
    }

    override fun findTagFreeByKeyOptional(key: TagFreeKey): TagFree? {
        dbConnectionFactory.getConnection().use { c ->
            c.prepareStatement(
                "SELECT id, key, name, description FROM tag_free WHERE key = ?"
            ).use { ps ->
                ps.setString(1, key.value)
                ps.executeQuery().use { rs ->
                    if (!rs.next()) return null
                    return TagFreeInMemory(
                        id = tagFreeIdFromSql(rs.getString("id")),
                        key = TagFreeKey(rs.getString("key")),
                        name = rs.getString("name"),
                        description = rs.getString("description")
                    )
                }
            }
        }
    }

    override fun findTagFreeByIdOptional(id: TagFreeId): TagFree? {
        dbConnectionFactory.getConnection().use { c ->
            c.prepareStatement(
                "SELECT id, key, name, description FROM tag_free WHERE id = ?"
            ).use { ps ->
                ps.setString(1, id.asString())
                ps.executeQuery().use { rs ->
                    if (!rs.next()) return null
                    return TagFreeInMemory(
                        id = tagFreeIdFromSql(rs.getString("id")),
                        key = TagFreeKey(rs.getString("key")),
                        name = rs.getString("name"),
                        description = rs.getString("description")
                    )
                }
            }
        }
    }

    override fun findTagGroupByIdOptional(id: TagGroupId): TagGroup? {
        dbConnectionFactory.getConnection().use { c ->
            c.prepareStatement(
                "SELECT id, key, name, description FROM tag_group WHERE id = ?"
            ).use { ps ->
                ps.setString(1, id.asString())
                ps.executeQuery().use { rs ->
                    if (!rs.next()) return null
                    return TagGroupInMemory(
                        id = tagGroupIdFromSql(rs.getString("id")),
                        key = TagGroupKey(rs.getString("key")),
                        name = rs.getString("name"),
                        description = rs.getString("description")
                    )
                }
            }
        }
    }

    override fun findTagGroupByKeyOptional(key: TagGroupKey): TagGroup? {
        dbConnectionFactory.getConnection().use { c ->
            c.prepareStatement(
                "SELECT id, key, name, description FROM tag_group WHERE key = ?"
            ).use { ps ->
                ps.setString(1, key.value)
                ps.executeQuery().use { rs ->
                    if (!rs.next()) return null
                    return TagGroupInMemory(
                        id = tagGroupIdFromSql(rs.getString("id")),
                        key = TagGroupKey(rs.getString("key")),
                        name = rs.getString("name"),
                        description = rs.getString("description")
                    )
                }
            }
        }
    }

    override fun findTagManagedByIdOptional(id: TagManagedId): TagManaged? {
        dbConnectionFactory.getConnection().use { c ->
            c.prepareStatement(
                "SELECT id, tag_group_id, key, name, description FROM tag_managed WHERE id = ?"
            ).use { ps ->
                ps.setString(1, id.asString())
                ps.executeQuery().use { rs ->
                    if (!rs.next()) return null
                    return TagManagedInMemory(
                        id = tagManagedIdFromSql(rs.getString("id")),
                        groupId = tagGroupIdFromSql(rs.getString("tag_group_id")),
                        key = TagManagedKey(rs.getString("key")),
                        name = rs.getString("name"),
                        description = rs.getString("description")
                    )
                }
            }
        }
    }

    override fun findTagManagedByKeyOptional(
        id: TagGroupId,
        key: TagManagedKey
    ): TagManaged? {
        dbConnectionFactory.getConnection().use { c ->
            c.prepareStatement(
                "SELECT id, tag_group_id, key, name, description FROM tag_managed WHERE tag_group_id = ? AND key = ?"
            ).use { ps ->
                ps.setString(1, id.asString())
                ps.setString(2, key.value)
                ps.executeQuery().use { rs ->
                    if (!rs.next()) return null
                    return TagManagedInMemory(
                        id = tagManagedIdFromSql(rs.getString("id")),
                        groupId = tagGroupIdFromSql(rs.getString("tag_group_id")),
                        key = TagManagedKey(rs.getString("key")),
                        name = rs.getString("name"),
                        description = rs.getString("description")
                    )
                }
            }
        }
    }

    override fun dispatch(cmd: TagRepoCmd) {
        logger.debug(cmd.toString())
        dbConnectionFactory.getConnection().use { c ->
            when (cmd) {
                is TagRepoCmd.TagFreeCreate -> {
                    c.prepareStatement(
                        "INSERT INTO tag_free(id, key, name, description) VALUES (?, ?, ?, ?)"
                    ).use { ps ->
                        ps.setString(1, cmd.item.id.asString())
                        ps.setString(2, cmd.item.key.value)
                        ps.setString(3, cmd.item.name)
                        ps.setString(4, cmd.item.description)
                        ps.executeUpdate()
                    }
                }

                is TagRepoCmd.TagFreeUpdateKey -> {
                    c.prepareStatement(
                        "UPDATE tag_free SET key = ? WHERE id = ?"
                    ).use { ps ->
                        ps.setString(1, cmd.value.value)
                        ps.setString(2, cmd.tagFreeId.asString())
                        ps.executeUpdate()
                    }
                }

                is TagRepoCmd.TagFreeUpdateName -> {
                    c.prepareStatement(
                        "UPDATE tag_free SET name = ? WHERE id = ?"
                    ).use { ps ->
                        ps.setString(1, cmd.value)
                        ps.setString(2, cmd.tagFreeId.asString())
                        ps.executeUpdate()
                    }
                }

                is TagRepoCmd.TagFreeUpdateDescription -> {
                    c.prepareStatement(
                        "UPDATE tag_free SET description = ? WHERE id = ?"
                    ).use { ps ->
                        ps.setString(1, cmd.value)
                        ps.setString(2, cmd.tagFreeId.asString())
                        ps.executeUpdate()
                    }
                }

                is TagRepoCmd.TagFreeDelete -> {
                    c.prepareStatement(
                        "DELETE FROM tag_free WHERE id = ?"
                    ).use { ps ->
                        ps.setString(1, cmd.tagFreeId.asString())
                        ps.executeUpdate()
                    }
                }

                is TagRepoCmd.TagGroupCreate -> {
                    c.prepareStatement(
                        "INSERT INTO tag_group(id, key, name, description) VALUES (?, ?, ?, ?)"
                    ).use { ps ->
                        ps.setString(1, cmd.item.id.asString())
                        ps.setString(2, cmd.item.key.value)
                        ps.setString(3, cmd.item.name)
                        ps.setString(4, cmd.item.description)
                        ps.executeUpdate()
                    }
                }

                is TagRepoCmd.TagGroupUpdateKey -> {
                    c.prepareStatement(
                        "UPDATE tag_group SET key = ? WHERE id = ?"
                    ).use { ps ->
                        ps.setString(1, cmd.value.value)
                        ps.setString(2, cmd.tagGroupId.asString())
                        ps.executeUpdate()
                    }
                }

                is TagRepoCmd.TagGroupUpdateName -> {
                    c.prepareStatement(
                        "UPDATE tag_group SET name = ? WHERE id = ?"
                    ).use { ps ->
                        ps.setString(1, cmd.value)
                        ps.setString(2, cmd.tagGroupId.asString())
                        ps.executeUpdate()
                    }
                }

                is TagRepoCmd.TagGroupUpdateDescription -> {
                    c.prepareStatement(
                        "UPDATE tag_group SET description = ? WHERE id = ?"
                    ).use { ps ->
                        ps.setString(1, cmd.value)
                        ps.setString(2, cmd.tagGroupId.asString())
                        ps.executeUpdate()
                    }
                }

                is TagRepoCmd.TagGroupDelete -> {
                    c.prepareStatement(
                        "DELETE FROM tag_group WHERE id = ?"
                    ).use { ps ->
                        ps.setString(1, cmd.tagGroupId.asString())
                        ps.executeUpdate()
                    }
                }

                is TagRepoCmd.TagManagedCreate -> {
                    c.prepareStatement(
                        "INSERT INTO tag_managed(id, tag_group_id, key, name, description) VALUES (?, ?, ?, ?, ?)"
                    ).use { ps ->
                        ps.setString(1, cmd.item.id.asString())
                        ps.setString(2, cmd.item.groupId.asString())
                        ps.setString(3, cmd.item.key.value)
                        ps.setString(4, cmd.item.name)
                        ps.setString(5, cmd.item.description)
                        ps.executeUpdate()
                    }
                }

                is TagRepoCmd.TagManagedUpdateKey -> {
                    c.prepareStatement(
                        "UPDATE tag_managed SET key = ? WHERE id = ?"
                    ).use { ps ->
                        ps.setString(1, cmd.value.value)
                        ps.setString(2, cmd.tagManagedId.asString())
                        ps.executeUpdate()
                    }
                }

                is TagRepoCmd.TagManagedUpdateName -> {
                    c.prepareStatement(
                        "UPDATE tag_managed SET name = ? WHERE id = ?"
                    ).use { ps ->
                        ps.setString(1, cmd.value)
                        ps.setString(2, cmd.tagManagedId.asString())
                        ps.executeUpdate()
                    }
                }

                is TagRepoCmd.TagManagedUpdateDescription -> {
                    c.prepareStatement(
                        "UPDATE tag_managed SET description = ? WHERE id = ?"
                    ).use { ps ->
                        ps.setString(1, cmd.value)
                        ps.setString(2, cmd.tagManagedId.asString())
                        ps.executeUpdate()
                    }
                }

                is TagRepoCmd.TagManagedDelete -> {
                    c.prepareStatement(
                        "DELETE FROM tag_managed WHERE id = ?"
                    ).use { ps ->
                        ps.setString(1, cmd.tagManagedId.asString())
                        ps.executeUpdate()
                    }
                }
            }
        }
    }

    private fun tagFreeIdFromSql(value: String): TagFreeId {
        return TagFreeId(UuidUtils.fromString(value))
    }

    private fun tagGroupIdFromSql(value: String): TagGroupId {
        return TagGroupId(UuidUtils.fromString(value))
    }

    private fun tagManagedIdFromSql(value: String): TagManagedId {
        return TagManagedId(UuidUtils.fromString(value))
    }

    companion object {
        @Language("SQLite")
        private const val SCHEMA = """
CREATE TABLE IF NOT EXISTS tag_free (
  id TEXT PRIMARY KEY UNIQUE,
  key TEXT NOT NULL UNIQUE,
  name TEXT,
  description TEXT
);

CREATE TABLE IF NOT EXISTS tag_group (
  id TEXT PRIMARY KEY UNIQUE,
  key TEXT NOT NULL UNIQUE,
  name TEXT,
  description TEXT
);

CREATE TABLE IF NOT EXISTS tag_managed (
  id TEXT PRIMARY KEY UNIQUE,
  tag_group_id TEXT NOT NULL,
  key TEXT NOT NULL UNIQUE,
  name TEXT,
  description TEXT
);

"""

        private val logger = LoggerFactory.getLogger(TagStorageSQLite::class.java)
    }

}
