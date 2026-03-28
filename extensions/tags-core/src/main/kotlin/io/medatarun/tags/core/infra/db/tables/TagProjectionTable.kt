package io.medatarun.tags.core.infra.db.tables

import io.medatarun.tags.core.domain.TagGroupId
import io.medatarun.tags.core.domain.TagId
import io.medatarun.tags.core.domain.TagKey
import io.medatarun.tags.core.domain.TagScopeId
import io.medatarun.tags.core.infra.db.types.IdTransformer
import io.medatarun.tags.core.infra.db.types.KeyTransformer
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID

internal object TagProjectionTable : Table("tag_projection") {
    val id = javaUUID("id").transform(IdTransformer(::TagId))
    val scopeType = text("scope_type")
    val scopeId = javaUUID("scope_id").transform(IdTransformer(::TagScopeId)).nullable()
    val tagGroupId = javaUUID("tag_group_id").transform(IdTransformer(::TagGroupId)).nullable()
    val key = text("key").transform(KeyTransformer(::TagKey))
    val name = text("name").nullable()
    val description = text("description").nullable()

    override val primaryKey = PrimaryKey(id)
}
