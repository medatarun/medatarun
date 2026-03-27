package io.medatarun.tags.core.ports.needs

import io.medatarun.storage.eventsourcing.StorageCmd
import io.medatarun.storage.eventsourcing.StorageEventContract
import io.medatarun.tags.core.domain.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface TagStorageCmd: StorageCmd {
    val scope: TagScopeRef

    @Serializable
    @StorageEventContract(eventType = "tag_created", eventVersion = 1)
    data class TagCreate(
        @Contextual
        @SerialName("tagId")
        val tagId: TagId,
        @Contextual
        @SerialName("scope")
        override val scope: TagScopeRef,
        @Contextual
        @SerialName("groupId")
        val groupId: TagGroupId?,
        @Contextual
        @SerialName("key")
        val key: TagKey,
        @SerialName("name")
        val name: String?,
        @SerialName("description")
        val description: String?
    ) : TagStorageCmd

    @Serializable
    @StorageEventContract(eventType = "tag_key_updated", eventVersion = 1)
    data class TagUpdateKey(
        @Contextual
        @SerialName("tagId")
        val tagId: TagId,
        @Contextual
        @SerialName("scope")
        override val scope: TagScopeRef,
        @Contextual
        @SerialName("key")
        val key: TagKey
    ) : TagStorageCmd

    @Serializable
    @StorageEventContract(eventType = "tag_name_updated", eventVersion = 1)
    data class TagUpdateName(
        @Contextual
        @SerialName("tagId")
        val tagId: TagId,
        @Contextual
        @SerialName("scope")
        override val scope: TagScopeRef,
        @SerialName("name")
        val name: String?
    ) : TagStorageCmd

    @Serializable
    @StorageEventContract(eventType = "tag_description_updated", eventVersion = 1)
    data class TagUpdateDescription(
        @Contextual
        @SerialName("tagId")
        val tagId: TagId,
        @Contextual
        @SerialName("scope")
        override val scope: TagScopeRef,
        @SerialName("description")
        val description: String?
    ) : TagStorageCmd

    @Serializable
    @StorageEventContract(eventType = "tag_deleted", eventVersion = 1)
    data class TagDelete(
        @Contextual
        @SerialName("tagId")
        val tagId: TagId,
        @Contextual
        @SerialName("scope")
        override val scope: TagScopeRef
    ) : TagStorageCmd

    @Serializable
    @StorageEventContract(eventType = "tag_local_scope_deleted", eventVersion = 1)
    data class TagLocalScopeDelete(
        @Contextual
        @SerialName("scope")
        override val scope: TagScopeRef
    ) : TagStorageCmd

    @Serializable
    @StorageEventContract(eventType = "tag_group_created", eventVersion = 1)
    data class TagGroupCreate(
        @Contextual
        @SerialName("tagGroupId")
        val tagGroupId: TagGroupId,
        @Contextual
        @SerialName("key")
        val key: TagGroupKey,
        @SerialName("name")
        val name: String?,
        @SerialName("description")
        val description: String?
    ) : TagStorageCmd {
        override val scope: TagScopeRef
            get() = TagScopeRef.Global
    }

    @Serializable
    @StorageEventContract(eventType = "tag_group_key_updated", eventVersion = 1)
    data class TagGroupUpdateKey(
        @Contextual
        @SerialName("tagGroupId")
        val tagGroupId: TagGroupId,
        @Contextual
        @SerialName("key")
        val key: TagGroupKey
    ) : TagStorageCmd {
        override val scope: TagScopeRef
            get() = TagScopeRef.Global
    }

    @Serializable
    @StorageEventContract(eventType = "tag_group_name_updated", eventVersion = 1)
    data class TagGroupUpdateName(
        @Contextual
        @SerialName("tagGroupId")
        val tagGroupId: TagGroupId,
        @SerialName("name")
        val name: String?
    ) : TagStorageCmd {
        override val scope: TagScopeRef
            get() = TagScopeRef.Global
    }

    @Serializable
    @StorageEventContract(eventType = "tag_group_description_updated", eventVersion = 1)
    data class TagGroupUpdateDescription(
        @Contextual
        @SerialName("tagGroupId")
        val tagGroupId: TagGroupId,
        @SerialName("description")
        val description: String?
    ) : TagStorageCmd {
        override val scope: TagScopeRef
            get() = TagScopeRef.Global
    }

    @Serializable
    @StorageEventContract(eventType = "tag_group_deleted", eventVersion = 1)
    data class TagGroupDelete(
        @Contextual
        @SerialName("tagGroupId")
        val tagGroupId: TagGroupId
    ) : TagStorageCmd {
        override val scope: TagScopeRef
            get() = TagScopeRef.Global
    }
}
