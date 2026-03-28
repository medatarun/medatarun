package io.medatarun.tags.core.infra.db

import io.medatarun.tags.core.domain.TagLocalScopeDeleteGlobalScopeException
import io.medatarun.tags.core.domain.TagScopeRef
import io.medatarun.tags.core.infra.db.tables.TagGroupProjectionTable
import io.medatarun.tags.core.infra.db.tables.TagProjectionTable
import io.medatarun.tags.core.ports.needs.TagStorageCmd
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.update

internal class TagStorageDbProjection {
    fun projectCommand(cmd: TagStorageCmd) {
        val scope = cmd.scope
        when (cmd) {
            is TagStorageCmd.TagCreate -> {
                TagProjectionTable.insert { row ->
                    row[TagProjectionTable.id] = cmd.tagId
                    row[TagProjectionTable.scopeType] = cmd.scope.type.value
                    when (scope) {
                        is TagScopeRef.Global -> row[TagProjectionTable.scopeId] = null
                        is TagScopeRef.Local -> row[TagProjectionTable.scopeId] = scope.localScopeId
                    }
                    row[TagProjectionTable.tagGroupId] = cmd.groupId
                    row[TagProjectionTable.key] = cmd.key
                    row[TagProjectionTable.name] = cmd.name
                    row[TagProjectionTable.description] = cmd.description
                }
            }

            is TagStorageCmd.TagUpdateKey -> {
                TagProjectionTable.update(where = { TagProjectionTable.id eq cmd.tagId }) { row ->
                    row[TagProjectionTable.key] = cmd.key
                }
            }

            is TagStorageCmd.TagUpdateName -> {
                TagProjectionTable.update(where = { TagProjectionTable.id eq cmd.tagId }) { row ->
                    row[TagProjectionTable.name] = cmd.name
                }
            }

            is TagStorageCmd.TagUpdateDescription -> {
                TagProjectionTable.update(where = { TagProjectionTable.id eq cmd.tagId }) { row ->
                    row[TagProjectionTable.description] = cmd.description
                }
            }

            is TagStorageCmd.TagDelete -> {
                TagProjectionTable.deleteWhere { id eq cmd.tagId }
            }

            is TagStorageCmd.TagLocalScopeDelete -> {
                val cmdScope = cmd.scope as? TagScopeRef.Local
                    ?: throw TagLocalScopeDeleteGlobalScopeException(cmd.scope.asString())
                TagProjectionTable.deleteWhere {
                    (scopeType eq cmdScope.type.value) and (scopeId eq cmdScope.localScopeId)
                }
            }

            is TagStorageCmd.TagGroupCreate -> {
                TagGroupProjectionTable.insert { row ->
                    row[TagGroupProjectionTable.id] = cmd.tagGroupId
                    row[TagGroupProjectionTable.key] = cmd.key
                    row[TagGroupProjectionTable.name] = cmd.name
                    row[TagGroupProjectionTable.description] = cmd.description
                }
            }

            is TagStorageCmd.TagGroupUpdateKey -> {
                TagGroupProjectionTable.update(where = { TagGroupProjectionTable.id eq cmd.tagGroupId }) { row ->
                    row[TagGroupProjectionTable.key] = cmd.key
                }
            }

            is TagStorageCmd.TagGroupUpdateName -> {
                TagGroupProjectionTable.update(where = { TagGroupProjectionTable.id eq cmd.tagGroupId }) { row ->
                    row[TagGroupProjectionTable.name] = cmd.name
                }
            }

            is TagStorageCmd.TagGroupUpdateDescription -> {
                TagGroupProjectionTable.update(where = { TagGroupProjectionTable.id eq cmd.tagGroupId }) { row ->
                    row[TagGroupProjectionTable.description] = cmd.description
                }
            }

            is TagStorageCmd.TagGroupDelete -> {
                TagGroupProjectionTable.deleteWhere { id eq cmd.tagGroupId }
            }
        }
    }
}
