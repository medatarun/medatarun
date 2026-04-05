package io.medatarun.tags.core.infra.db.tables

import io.medatarun.tags.core.domain.TagGroupId
import io.medatarun.tags.core.domain.TagGroupKey
import io.medatarun.platform.db.exposed.IdTransformer
import io.medatarun.platform.db.exposed.KeyTransformer
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID

internal object TagViewCurrent_TagGroup_Table : Table("tag_view_current_tag_group") {
    val id = javaUUID("id").transform(IdTransformer(::TagGroupId))
    val key = text("key").transform(KeyTransformer(::TagGroupKey))
    val name = text("name").nullable()
    val description = text("description").nullable()

    override val primaryKey = PrimaryKey(id)
}
