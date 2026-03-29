package io.medatarun.tags.core.infra.db

import io.medatarun.tags.core.domain.TagLocalScopeDeleteGlobalScopeException
import io.medatarun.tags.core.domain.TagScopeRef
import io.medatarun.tags.core.infra.db.tables.TagViewCurrent_TagGroup_Table
import io.medatarun.tags.core.infra.db.tables.TagViewCurrent_Tag_Table
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
                TagViewCurrent_Tag_Table.insert { row ->
                    row[TagViewCurrent_Tag_Table.id] = cmd.tagId
                    row[TagViewCurrent_Tag_Table.scopeType] = cmd.scope.type.value
                    when (scope) {
                        is TagScopeRef.Global -> row[TagViewCurrent_Tag_Table.scopeId] = null
                        is TagScopeRef.Local -> row[TagViewCurrent_Tag_Table.scopeId] = scope.localScopeId
                    }
                    row[TagViewCurrent_Tag_Table.tagGroupId] = cmd.groupId
                    row[TagViewCurrent_Tag_Table.key] = cmd.key
                    row[TagViewCurrent_Tag_Table.name] = cmd.name
                    row[TagViewCurrent_Tag_Table.description] = cmd.description
                }
            }

            is TagStorageCmd.TagUpdateKey -> {
                TagViewCurrent_Tag_Table.update(where = { TagViewCurrent_Tag_Table.id eq cmd.tagId }) { row ->
                    row[TagViewCurrent_Tag_Table.key] = cmd.key
                }
            }

            is TagStorageCmd.TagUpdateName -> {
                TagViewCurrent_Tag_Table.update(where = { TagViewCurrent_Tag_Table.id eq cmd.tagId }) { row ->
                    row[TagViewCurrent_Tag_Table.name] = cmd.name
                }
            }

            is TagStorageCmd.TagUpdateDescription -> {
                TagViewCurrent_Tag_Table.update(where = { TagViewCurrent_Tag_Table.id eq cmd.tagId }) { row ->
                    row[TagViewCurrent_Tag_Table.description] = cmd.description
                }
            }

            is TagStorageCmd.TagDelete -> {
                TagViewCurrent_Tag_Table.deleteWhere { id eq cmd.tagId }
            }

            is TagStorageCmd.TagLocalScopeDelete -> {
                val cmdScope = cmd.scope as? TagScopeRef.Local
                    ?: throw TagLocalScopeDeleteGlobalScopeException(cmd.scope.asString())
                TagViewCurrent_Tag_Table.deleteWhere {
                    (scopeType eq cmdScope.type.value) and (scopeId eq cmdScope.localScopeId)
                }
            }

            is TagStorageCmd.TagGroupCreate -> {
                TagViewCurrent_TagGroup_Table.insert { row ->
                    row[TagViewCurrent_TagGroup_Table.id] = cmd.tagGroupId
                    row[TagViewCurrent_TagGroup_Table.key] = cmd.key
                    row[TagViewCurrent_TagGroup_Table.name] = cmd.name
                    row[TagViewCurrent_TagGroup_Table.description] = cmd.description
                }
            }

            is TagStorageCmd.TagGroupUpdateKey -> {
                TagViewCurrent_TagGroup_Table.update(where = { TagViewCurrent_TagGroup_Table.id eq cmd.tagGroupId }) { row ->
                    row[TagViewCurrent_TagGroup_Table.key] = cmd.key
                }
            }

            is TagStorageCmd.TagGroupUpdateName -> {
                TagViewCurrent_TagGroup_Table.update(where = { TagViewCurrent_TagGroup_Table.id eq cmd.tagGroupId }) { row ->
                    row[TagViewCurrent_TagGroup_Table.name] = cmd.name
                }
            }

            is TagStorageCmd.TagGroupUpdateDescription -> {
                TagViewCurrent_TagGroup_Table.update(where = { TagViewCurrent_TagGroup_Table.id eq cmd.tagGroupId }) { row ->
                    row[TagViewCurrent_TagGroup_Table.description] = cmd.description
                }
            }

            is TagStorageCmd.TagGroupDelete -> {
                TagViewCurrent_TagGroup_Table.deleteWhere { id eq cmd.tagGroupId }
            }
        }
    }
}
