package io.medatarun.tags.core.adapters

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.tags.core.domain.*
import io.medatarun.tags.core.internal.TagGroupInMemory
import io.medatarun.tags.core.internal.TagInMemory
import io.medatarun.tags.core.ports.needs.TagRepoCmd
import io.medatarun.tags.core.ports.needs.TagStorage
import io.medatarun.type.commons.id.Id
import io.medatarun.type.commons.key.Key
import org.intellij.lang.annotations.Language
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.slf4j.LoggerFactory

class TagStorageSQLite(private val dbConnectionFactory: DbConnectionFactory): TagStorage {
    private class TagStorageSQLiteInvalidLookupException(message: String) : MedatarunException(message)

    fun initSchema() {
        dbConnectionFactory.withConnection { connection ->
            SCHEMA.split(";")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .forEach { stmt ->
                    connection.createStatement().execute(stmt)
                }
        }
    }

    override fun findAllTag(): List<Tag> {
        return dbConnectionFactory.withExposed {
            TagTable.selectAll()
                .map { tagFromRow(it) }
        }
    }

    override fun findTagByKeyOptional(scope: TagScopeRef, groupId: TagGroupId?, key: TagKey): Tag? {
        return dbConnectionFactory.withExposed {
            when (scope) {
                is TagScopeRef.Local -> {
                    TagTable.selectAll().where {
                        (TagTable.scopeType eq scope.type.value) and
                            (TagTable.scopeId eq scope.localScopeId.asString()) and
                            TagTable.tagGroupId.isNull() and
                            (TagTable.key eq key.value)
                    }.singleOrNull()?.let { tagFromRow(it) }
                }

                is TagScopeRef.Global -> {
                    val effectiveGroupId = groupId
                        ?: throw TagStorageSQLiteInvalidLookupException("Global tag lookup requires groupId")
                    TagTable.selectAll().where {
                        (TagTable.scopeType eq scope.type.value) and
                            TagTable.scopeId.isNull() and
                            (TagTable.tagGroupId eq effectiveGroupId.asString()) and
                            (TagTable.key eq key.value)
                    }.singleOrNull()?.let { tagFromRow(it) }
                }
            }
        }
    }

    override fun findTagByIdOptional(id: TagId): Tag? {
        return dbConnectionFactory.withExposed {
            TagTable.selectAll().where { TagTable.id eq id.asString() }
                .singleOrNull()
                ?.let { tagFromRow(it) }
        }
    }

    override fun findAllTagGroup(): List<TagGroup> {
        return dbConnectionFactory.withExposed {
            TagGroupTable.selectAll()
                .map { tagGroupFromRow(it) }
        }
    }

    override fun findTagGroupByIdOptional(id: TagGroupId): TagGroup? {
        return dbConnectionFactory.withExposed {
            TagGroupTable.selectAll().where { TagGroupTable.id eq id.asString() }
                .singleOrNull()
                ?.let { tagGroupFromRow(it) }
        }
    }

    override fun findTagGroupByKeyOptional(key: TagGroupKey): TagGroup? {
        return dbConnectionFactory.withExposed {
            TagGroupTable.selectAll().where { TagGroupTable.key eq key.value }
                .singleOrNull()
                ?.let { tagGroupFromRow(it) }
        }
    }

    override fun dispatch(cmd: TagRepoCmd) {
        logger.debug(cmd.toString())
        dbConnectionFactory.withExposed {
            when (cmd) {
                is TagRepoCmd.TagCreate -> {
                    TagTable.insert { row ->
                        row[id] = cmd.item.id.asString()
                        row[scopeType] = cmd.item.scope.type.value
                        when (val scope = cmd.item.scope) {
                            is TagScopeRef.Global -> row[scopeId] = null
                            is TagScopeRef.Local -> row[scopeId] = scope.localScopeId.asString()
                        }
                        row[tagGroupId] = cmd.item.groupId?.asString()
                        row[key] = cmd.item.key.asString()
                        row[name] = cmd.item.name
                        row[description] = cmd.item.description
                    }
                }

                is TagRepoCmd.TagUpdateKey -> {
                    TagTable.update(where = { TagTable.id eq cmd.tagId.asString() }) { row ->
                        row[key] = cmd.value.asString()
                    }
                }

                is TagRepoCmd.TagUpdateName -> {
                    TagTable.update(where = { TagTable.id eq cmd.tagId.asString() }) { row ->
                        row[name] = cmd.value
                    }
                }

                is TagRepoCmd.TagUpdateDescription -> {
                    TagTable.update(where = { TagTable.id eq cmd.tagId.asString() }) { row ->
                        row[description] = cmd.value
                    }
                }

                is TagRepoCmd.TagDelete -> {
                    TagTable.deleteWhere { id eq cmd.tagId.asString() }
                }

                is TagRepoCmd.TagGroupCreate -> {
                    TagGroupTable.insert { row ->
                        row[id] = cmd.item.id.asString()
                        row[key] = cmd.item.key.asString()
                        row[name] = cmd.item.name
                        row[description] = cmd.item.description
                    }
                }

                is TagRepoCmd.TagGroupUpdateKey -> {
                    TagGroupTable.update(where = { TagGroupTable.id eq cmd.tagGroupId.asString() }) { row ->
                        row[key] = cmd.value.asString()
                    }
                }

                is TagRepoCmd.TagGroupUpdateName -> {
                    TagGroupTable.update(where = { TagGroupTable.id eq cmd.tagGroupId.asString() }) { row ->
                        row[name] = cmd.value
                    }
                }

                is TagRepoCmd.TagGroupUpdateDescription -> {
                    TagGroupTable.update(where = { TagGroupTable.id eq cmd.tagGroupId.asString() }) { row ->
                        row[description] = cmd.value
                    }
                }

                is TagRepoCmd.TagGroupDelete -> {
                    TagGroupTable.deleteWhere { id eq cmd.tagGroupId.asString() }
                }
            }
        }
    }

    private fun tagGroupFromRow(row: ResultRow): TagGroup {
        return TagGroupInMemory(
            id = Id.fromString(row[TagGroupTable.id], ::TagGroupId),
            key = Key.fromString(row[TagGroupTable.key], ::TagGroupKey),
            name = row[TagGroupTable.name],
            description = row[TagGroupTable.description]
        )
    }

    private fun tagFromRow(row: ResultRow): Tag {
        val scopeType = TagScopeType(row[TagTable.scopeType])
        val scopeIdString = row[TagTable.scopeId]
        val scope = if (scopeType.value == TagScopeRef.Global.type.value) {
            TagScopeRef.Global
        } else {
            val localScopeId = requireNotNull(scopeIdString) {
                "Local tag row missing scope_id"
            }
            TagScopeRef.Local(scopeType, Id.fromString(localScopeId, ::TagScopeId))
        }
        val groupIdString = row[TagTable.tagGroupId]
        val groupId = if (groupIdString == null) null else Id.fromString(groupIdString, ::TagGroupId)
        return TagInMemory(
            id = Id.fromString(row[TagTable.id], ::TagId),
            scope = scope,
            groupId = groupId,
            key = Key.fromString(row[TagTable.key], ::TagKey),
            name = row[TagTable.name],
            description = row[TagTable.description]
        )
    }

    companion object {
        private object TagGroupTable : Table("tag_group") {
            val id = text("id")
            val key = text("key")
            val name = text("name").nullable()
            val description = text("description").nullable()

            override val primaryKey = PrimaryKey(id)
        }

        private object TagTable : Table("tag") {
            val id = text("id")
            val scopeType = text("scope_type")
            val scopeId = text("scope_id").nullable()
            val tagGroupId = text("tag_group_id").nullable()
            val key = text("key")
            val name = text("name").nullable()
            val description = text("description").nullable()

            override val primaryKey = PrimaryKey(id)
        }

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
  scope_type TEXT NOT NULL,
  scope_id TEXT,
  tag_group_id TEXT,
  key TEXT NOT NULL,
  name TEXT,
  description TEXT,
  FOREIGN KEY (tag_group_id) REFERENCES tag_group(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_tag_scope_key
ON tag(scope_type, scope_id, key);

CREATE INDEX IF NOT EXISTS idx_tag_group_key
ON tag(tag_group_id, key);

"""

        private val logger = LoggerFactory.getLogger(TagStorageSQLite::class.java)
    }

}
