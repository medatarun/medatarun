package io.medatarun.model.actions.history

import io.medatarun.model.infra.db.events.ModelEventSystem
import io.medatarun.model.ports.needs.ModelStorageCmd
import io.medatarun.tags.core.domain.TagId
import io.medatarun.tags.core.domain.TagQueries
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant

class ModelChangeEventDisplayResolver(
    private val tagQueries: TagQueries
) {

    private val sys = ModelEventSystem()

    fun resolve(eventType: String, eventVersion: Int, eventDate: Instant, payloadJson: JsonObject): JsonObject {
        val payload = sys.codec.decode(eventType, eventVersion, payloadJson)
        return when (payload) {
            is ModelStorageCmd.UpdateModelTagDelete -> resolveTagId(payload.tagId, eventDate)
            is ModelStorageCmd.UpdateModelTagAdd -> resolveTagId(payload.tagId, eventDate)
            is ModelStorageCmd.UpdateEntityTagAdd -> resolveTagId(payload.tagId, eventDate)
            is ModelStorageCmd.UpdateEntityTagDelete -> resolveTagId(payload.tagId, eventDate)
            is ModelStorageCmd.UpdateEntityAttributeTagAdd -> resolveTagId(payload.tagId, eventDate)
            is ModelStorageCmd.UpdateEntityAttributeTagDelete -> resolveTagId(payload.tagId, eventDate)
            is ModelStorageCmd.UpdateRelationshipTagAdd -> resolveTagId(payload.tagId, eventDate)
            is ModelStorageCmd.UpdateRelationshipTagDelete -> resolveTagId(payload.tagId, eventDate)
            is ModelStorageCmd.UpdateRelationshipAttributeTagAdd -> resolveTagId(payload.tagId, eventDate)
            is ModelStorageCmd.UpdateRelationshipAttributeTagDelete -> resolveTagId(payload.tagId, eventDate)

            else -> buildJsonObject {}
        }
    }

    private fun resolveTagId(paramValue: TagId, eventDate: Instant): JsonObject {
        val tagAsOf = tagQueries.findTagByIdAsOfOptional(paramValue, eventDate)
        val groupIdAsOf = tagAsOf?.groupId
        val groupAsOf = if (groupIdAsOf != null) {
            tagQueries.findTagGroupByIdAsOfOptional(groupIdAsOf, eventDate)
        } else null
        val tag = tagQueries.findTagByIdOptional(paramValue)
        val groupId = tag?.groupId
        val group = if (groupId != null) tagQueries.findTagGroupByIdOptional(groupId) else null
        return buildJsonObject {
            put("tagId", buildJsonObject {
                put("tagLabelAsOf", tagAsOf?.name ?: tagAsOf?.key?.asString() ?: paramValue.asString())
                put("groupLabelAsOf", groupAsOf?.name ?: groupAsOf?.key?.asString() ?: groupIdAsOf?.asString())
                put("tagLabelNow", tag?.name ?: tag?.key?.asString())
                put("groupLabelNow", group?.name ?: group?.key?.asString())
            })
        }
    }
}