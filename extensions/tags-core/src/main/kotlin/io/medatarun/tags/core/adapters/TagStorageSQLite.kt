package io.medatarun.tags.core.adapters

import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.tags.core.domain.*
import io.medatarun.tags.core.internal.TagGroupInMemory
import io.medatarun.tags.core.internal.TagInMemory
import io.medatarun.tags.core.ports.needs.TagRepoCmd
import io.medatarun.tags.core.ports.needs.TagStorage
import io.medatarun.type.commons.id.Id
import io.medatarun.type.commons.key.Key
import org.intellij.lang.annotations.Language
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.sql.Types

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

    override fun findAllTag(): List<Tag> {
        dbConnectionFactory.getConnection().use { c ->
            c.prepareStatement(
                "SELECT id, tag_group_id, key, name, description FROM tag"
            ).use { ps ->
                ps.executeQuery().use { rs ->
                    val items = mutableListOf<Tag>()
                    while (rs.next()) {
                        items.add(tagFromRow(rs))
                    }
                    return items
                }
            }
        }
    }

    override fun findTagByKeyOptional(groupId: TagGroupId?, key: TagKey): Tag? {
        dbConnectionFactory.getConnection().use { c ->
            if (groupId == null) {
                c.prepareStatement(
                    "SELECT id, tag_group_id, key, name, description FROM tag WHERE tag_group_id IS NULL AND key = ?"
                ).use { ps ->
                    ps.setString(1, key.value)
                    ps.executeQuery().use { rs ->
                        if (!rs.next()) return null
                        return tagFromRow(rs)
                    }
                }
            } else {
                c.prepareStatement(
                    "SELECT id, tag_group_id, key, name, description FROM tag WHERE tag_group_id = ? AND key = ?"
                ).use { ps ->
                    ps.setString(1, groupId.asString())
                    ps.setString(2, key.value)
                    ps.executeQuery().use { rs ->
                        if (!rs.next()) return null
                        return tagFromRow(rs)
                    }
                }
            }
        }
    }

    override fun findTagByIdOptional(id: TagId): Tag? {
        dbConnectionFactory.getConnection().use { c ->
            c.prepareStatement(
                "SELECT id, tag_group_id, key, name, description FROM tag WHERE id = ?"
            ).use { ps ->
                ps.setString(1, id.asString())
                ps.executeQuery().use { rs ->
                    if (!rs.next()) return null
                    return tagFromRow(rs)
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

    override fun dispatch(cmd: TagRepoCmd) {
        logger.debug(cmd.toString())
        dbConnectionFactory.getConnection().use { c ->
            when (cmd) {
                is TagRepoCmd.TagFreeCreate -> {
                    c.prepareStatement(
                        "INSERT INTO tag(id, tag_group_id, key, name, description) VALUES (?, ?, ?, ?, ?)"
                    ).use { ps ->
                        ps.setString(1, cmd.item.id.asString())
                        ps.setNull(2, Types.VARCHAR)
                        ps.setString(3, cmd.item.key.asString())
                        ps.setString(4, cmd.item.name)
                        ps.setString(5, cmd.item.description)
                        ps.executeUpdate()
                    }
                }

                is TagRepoCmd.TagFreeUpdateKey -> {
                    c.prepareStatement(
                        "UPDATE tag SET key = ? WHERE tag_group_id IS NULL AND id = ?"
                    ).use { ps ->
                        ps.setString(1, cmd.value.asString())
                        ps.setString(2, cmd.tagFreeId.asString())
                        ps.executeUpdate()
                    }
                }

                is TagRepoCmd.TagFreeUpdateName -> {
                    c.prepareStatement(
                        "UPDATE tag SET name = ? WHERE tag_group_id IS NULL AND id = ?"
                    ).use { ps ->
                        ps.setString(1, cmd.value)
                        ps.setString(2, cmd.tagFreeId.asString())
                        ps.executeUpdate()
                    }
                }

                is TagRepoCmd.TagFreeUpdateDescription -> {
                    c.prepareStatement(
                        "UPDATE tag SET description = ? WHERE tag_group_id IS NULL AND id = ?"
                    ).use { ps ->
                        ps.setString(1, cmd.value)
                        ps.setString(2, cmd.tagFreeId.asString())
                        ps.executeUpdate()
                    }
                }

                is TagRepoCmd.TagFreeDelete -> {
                    c.prepareStatement(
                        "DELETE FROM tag WHERE tag_group_id IS NULL AND id = ?"
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
                        "INSERT INTO tag(id, tag_group_id, key, name, description) VALUES (?, ?, ?, ?, ?)"
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
                        "UPDATE tag SET key = ? WHERE id = ?"
                    ).use { ps ->
                        ps.setString(1, cmd.value.asString())
                        ps.setString(2, cmd.tagManagedId.asString())
                        ps.executeUpdate()
                    }
                }

                is TagRepoCmd.TagManagedUpdateName -> {
                    c.prepareStatement(
                        "UPDATE tag SET name = ? WHERE id = ?"
                    ).use { ps ->
                        ps.setString(1, cmd.value)
                        ps.setString(2, cmd.tagManagedId.asString())
                        ps.executeUpdate()
                    }
                }

                is TagRepoCmd.TagManagedUpdateDescription -> {
                    c.prepareStatement(
                        "UPDATE tag SET description = ? WHERE id = ?"
                    ).use { ps ->
                        ps.setString(1, cmd.value)
                        ps.setString(2, cmd.tagManagedId.asString())
                        ps.executeUpdate()
                    }
                }

                is TagRepoCmd.TagManagedDelete -> {
                    c.prepareStatement(
                        "DELETE FROM tag WHERE id = ?"
                    ).use { ps ->
                        ps.setString(1, cmd.tagManagedId.asString())
                        ps.executeUpdate()
                    }
                }
            }
        }
    }


    private fun tagGroupFromRow(rs: ResultSet): TagGroup {
        return TagGroupInMemory(
            id = Id.fromString(rs.getString("id"), ::TagGroupId),
            key = Key.fromString(rs.getString("key"), ::TagGroupKey),
            name = rs.getString("name"),
            description = rs.getString("description")
        )
    }

    private fun tagFromRow(rs: ResultSet): Tag {
        val groupIdString = rs.getString("tag_group_id")
        val groupId = if (groupIdString == null) null else Id.fromString(groupIdString, ::TagGroupId)
        return TagInMemory(
            id = Id.fromString(rs.getString("id"), ::TagId),
            groupId = groupId,
            key = Key.fromString(rs.getString("key"), ::TagKey),
            name = rs.getString("name"),
            description = rs.getString("description")
        )
    }

    companion object {
        @Language("SQLite")
        private const val SCHEMA = """
CREATE TABLE IF NOT EXISTS tag_group (
  id TEXT PRIMARY KEY UNIQUE,
  key TEXT NOT NULL UNIQUE,
  name TEXT,
  description TEXT
);

CREATE TABLE IF NOT EXISTS tag (
  id TEXT PRIMARY KEY UNIQUE,
  tag_group_id TEXT,
  key TEXT,
  name TEXT,
  description TEXT,
  FOREIGN KEY (tag_group_id) REFERENCES tag_group(id) ON DELETE CASCADE
);

"""

        private val logger = LoggerFactory.getLogger(TagStorageSQLite::class.java)
    }

}
