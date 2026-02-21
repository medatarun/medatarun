package io.medatarun.tags.core.adapters

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.tags.core.domain.*
import io.medatarun.tags.core.internal.TagFreeInMemory
import io.medatarun.tags.core.internal.TagGroupInMemory
import io.medatarun.tags.core.internal.TagManagedInMemory
import io.medatarun.tags.core.ports.needs.TagRepoCmd
import io.medatarun.tags.core.ports.needs.TagStorage
import io.medatarun.type.commons.id.Id
import io.medatarun.type.commons.key.Key
import org.intellij.lang.annotations.Language
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.util.UUID

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

    override fun findAllTagFree(): List<TagFree> {
        dbConnectionFactory.getConnection().use { c ->
            c.prepareStatement(
                "SELECT id, key, name, description FROM tag_free"
            ).use { ps ->
                ps.executeQuery().use { rs ->
                    val items = mutableListOf<TagFree>()
                    while (rs.next()) {
                        items.add(tagFreeFromRow(rs))
                    }
                    return items
                }
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
                    return tagFreeFromRow(rs)
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
                    return tagFreeFromRow(rs)
                }
            }
        }
    }

    override fun findAllTagGroup(): List<TagGroup> {
        dbConnectionFactory.getConnection().use { c ->
            c.prepareStatement(
                "SELECT id, key, name, description FROM tag_group"
            ).use { ps ->
                ps.executeQuery().use { rs ->
                    val items = mutableListOf<TagGroup>()
                    while (rs.next()) {
                        items.add(tagGroupFromRow(rs))
                    }
                    return items
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
                    return tagGroupFromRow(rs)
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
                    return tagGroupFromRow(rs)
                }
            }
        }
    }

    override fun findAllTagManaged(): List<TagManaged> {
        dbConnectionFactory.getConnection().use { c ->
            c.prepareStatement(
                "SELECT id, tag_group_id, key, name, description FROM tag_managed"
            ).use { ps ->
                ps.executeQuery().use { rs ->
                    val items = mutableListOf<TagManaged>()
                    while (rs.next()) {
                        items.add(tagManagedFromRow(rs))
                    }
                    return items
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
                    return tagManagedFromRow(rs)
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
                ps.setString(2, key.asString())
                ps.executeQuery().use { rs ->
                    if (!rs.next()) return null
                    return tagManagedFromRow(rs)
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
                        ps.setString(2, cmd.item.key.asString())
                        ps.setString(3, cmd.item.name)
                        ps.setString(4, cmd.item.description)
                        ps.executeUpdate()
                    }
                }

                is TagRepoCmd.TagFreeUpdateKey -> {
                    c.prepareStatement(
                        "UPDATE tag_free SET key = ? WHERE id = ?"
                    ).use { ps ->
                        ps.setString(1, cmd.value.asString())
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
                        ps.setString(2, cmd.item.key.asString())
                        ps.setString(3, cmd.item.name)
                        ps.setString(4, cmd.item.description)
                        ps.executeUpdate()
                    }
                }

                is TagRepoCmd.TagGroupUpdateKey -> {
                    c.prepareStatement(
                        "UPDATE tag_group SET key = ? WHERE id = ?"
                    ).use { ps ->
                        ps.setString(1, cmd.value.asString())
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
                        ps.setString(3, cmd.item.key.asString())
                        ps.setString(4, cmd.item.name)
                        ps.setString(5, cmd.item.description)
                        ps.executeUpdate()
                    }
                }

                is TagRepoCmd.TagManagedUpdateKey -> {
                    c.prepareStatement(
                        "UPDATE tag_managed SET key = ? WHERE id = ?"
                    ).use { ps ->
                        ps.setString(1, cmd.value.asString())
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


    private fun tagFreeFromRow(rs: ResultSet): TagFree {
        return TagFreeInMemory(
            id = Id.fromString(rs.getString("id"), ::TagFreeId),
            key = Key.fromString(rs.getString("key"), ::TagFreeKey),
            name = rs.getString("name"),
            description = rs.getString("description")
        )
    }

    private fun tagGroupFromRow(rs: ResultSet): TagGroup {
        return TagGroupInMemory(
            id = Id.fromString(rs.getString("id"), ::TagGroupId),
            key = Key.fromString(rs.getString("key"), ::TagGroupKey),
            name = rs.getString("name"),
            description = rs.getString("description")
        )
    }

    private fun tagManagedFromRow(rs: ResultSet): TagManaged {
        return TagManagedInMemory(
            id = Id.fromString(rs.getString("id"), ::TagManagedId),
            groupId = Id.fromString(rs.getString("tag_group_id"), ::TagGroupId),
            key = TagManagedKey(rs.getString("key")),
            name = rs.getString("name"),
            description = rs.getString("description")
        )
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
  key TEXT NOT NULL,
  name TEXT,
  description TEXT
);

"""

        private val logger = LoggerFactory.getLogger(TagStorageSQLite::class.java)
    }

}
