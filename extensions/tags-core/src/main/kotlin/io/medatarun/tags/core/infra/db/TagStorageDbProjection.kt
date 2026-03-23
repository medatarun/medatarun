package io.medatarun.tags.core.infra.db

import io.medatarun.tags.core.domain.TagScopeRef
import io.medatarun.tags.core.infra.db.tables.TagGroupTable
import io.medatarun.tags.core.infra.db.tables.TagTable
import io.medatarun.tags.core.ports.needs.TagStorageCmd
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.update

internal class TagStorageDbProjection {

    fun projectCommand(cmd: TagStorageCmd) {
        val scope = cmd.scope
        when (cmd) {
            is TagStorageCmd.TagCreate -> {
                TagTable.insert { row ->
                    row[TagTable.id] = cmd.tagId.asString()
                    row[TagTable.scopeType] = cmd.scope.type.value
                    when (scope) {
                        is TagScopeRef.Global -> row[TagTable.scopeId] = null
                        is TagScopeRef.Local -> row[TagTable.scopeId] = scope.localScopeId.asString()
                    }
                    row[TagTable.tagGroupId] = cmd.groupId?.asString()
                    row[TagTable.key] = cmd.key.asString()
                    row[TagTable.name] = cmd.name
                    row[TagTable.description] = cmd.description
                }
            }

            is TagStorageCmd.TagUpdateKey -> {
                TagTable.update(where = { TagTable.id eq cmd.tagId.asString() }) { row ->
                    row[TagTable.key] = cmd.key.asString()
                }
            }

            is TagStorageCmd.TagUpdateName -> {
                TagTable.update(where = { TagTable.id eq cmd.tagId.asString() }) { row ->
                    row[TagTable.name] = cmd.name
                }
            }

            is TagStorageCmd.TagUpdateDescription -> {
                TagTable.update(where = { TagTable.id eq cmd.tagId.asString() }) { row ->
                    row[TagTable.description] = cmd.description
                }
            }

            is TagStorageCmd.TagDelete -> {
                TagTable.deleteWhere { id eq cmd.tagId.asString() }
            }

            is TagStorageCmd.TagGroupCreate -> {
                TagGroupTable.insert { row ->
                    row[TagGroupTable.id] = cmd.tagGroupId.asString()
                    row[TagGroupTable.key] = cmd.key.asString()
                    row[TagGroupTable.name] = cmd.name
                    row[TagGroupTable.description] = cmd.description
                }
            }

            is TagStorageCmd.TagGroupUpdateKey -> {
                TagGroupTable.update(where = { TagGroupTable.id eq cmd.tagGroupId.asString() }) { row ->
                    row[TagGroupTable.key] = cmd.key.asString()
                }
            }

            is TagStorageCmd.TagGroupUpdateName -> {
                TagGroupTable.update(where = { TagGroupTable.id eq cmd.tagGroupId.asString() }) { row ->
                    row[TagGroupTable.name] = cmd.name
                }
            }

            is TagStorageCmd.TagGroupUpdateDescription -> {
                TagGroupTable.update(where = { TagGroupTable.id eq cmd.tagGroupId.asString() }) { row ->
                    row[TagGroupTable.description] = cmd.description
                }
            }

            is TagStorageCmd.TagGroupDelete -> {
                TagGroupTable.deleteWhere { id eq cmd.tagGroupId.asString() }
            }
        }
    }
}

