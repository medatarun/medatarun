package io.medatarun.ext.modeljson

import io.medatarun.ext.modeljson.internal.ModelsStorageJsonFiles
import io.medatarun.ext.modeljson.internal.ModelsStorageJsonMigrations
import io.medatarun.model.domain.ModelId
import io.medatarun.model.ports.exposed.ModelCmd
import io.medatarun.model.ports.exposed.ModelCmds
import io.medatarun.model.ports.needs.ModelTagResolver.Companion.modelTagScopeRef
import io.medatarun.tags.core.domain.*
import io.medatarun.type.commons.id.Id
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ModelsStorageJsonMigrationsTest {

    private val parser = Json { prettyPrint = true }

    @Test
    fun `migrates schema from 1_0 to 2_0 through 1_1`() {
        val fs = ModelJsonFilesystemFixture()
        val files = ModelsStorageJsonFiles(fs.modelsDirectory())
        files.save("legacy-model", createSchema_1_0_ModelJson().toString())
        val tagStore = InMemoryTagStore()
        val modelCmds = CollectingModelCmds()

        ModelsStorageJsonMigrations(files, prettyPrint = true).start(tagStore, tagStore, modelCmds)

        val migrated = parser.parseToJsonElement(files.load(io.medatarun.model.domain.ModelKey("legacy-model"))).jsonObject
        assertEquals(
            ModelJsonSchemas.forVersion(ModelJsonSchemas.v_2_0),
            migrated[$$"$schema"]?.jsonPrimitive?.content
        )
        assertNotNull(migrated["id"]?.jsonPrimitive?.content)
        assertEquals("legacy-model", migrated["key"]?.jsonPrimitive?.content)
    }

    @Test
    fun `migrates schema from 1_1 to 2_0`() {
        val fs = ModelJsonFilesystemFixture()
        val files = ModelsStorageJsonFiles(fs.modelsDirectory())
        val modelId = "44444444-4444-4444-4444-444444444444"
        files.save("current-model", createSchema_1_1_ModelJson(modelId).toString())
        val tagStore = InMemoryTagStore()
        val modelCmds = CollectingModelCmds()

        ModelsStorageJsonMigrations(files, prettyPrint = true).start(tagStore, tagStore, modelCmds)

        val migrated = parser.parseToJsonElement(files.load(io.medatarun.model.domain.ModelKey("current-model"))).jsonObject
        assertEquals(
            ModelJsonSchemas.forVersion(ModelJsonSchemas.v_2_0),
            migrated[$$"$schema"]?.jsonPrimitive?.content
        )
        assertEquals(modelId, migrated["id"]?.jsonPrimitive?.content)
        assertEquals("current-model", migrated["key"]?.jsonPrimitive?.content)
    }

    @Test
    fun `migrates hashtags to local and global tag ids`() {
        val fs = ModelJsonFilesystemFixture()
        val files = ModelsStorageJsonFiles(fs.modelsDirectory())
        val modelId = "11111111-1111-1111-1111-111111111111"
        files.save("tagged-model", createSchema_1_1_WithLegacyHashtags(modelId).toString())
        val tagStore = InMemoryTagStore()
        val modelCmds = CollectingModelCmds()

        ModelsStorageJsonMigrations(files, prettyPrint = true).start(tagStore, tagStore, modelCmds)

        val migrated = parser.parseToJsonElement(files.load(io.medatarun.model.domain.ModelKey("tagged-model"))).jsonObject
        assertFalse(migrated.containsKey("hashtags"))

        val rootTags = migrated.getArrayValues("tags")
        val entityTags = migrated.getObject("entities", 0).getArrayValues("tags")
        val attributeTags = migrated.getObject("entities", 0).getObject("attributes", 0).getArrayValues("tags")
        val relationshipTags = migrated.getObject("relationships", 0).getArrayValues("tags")
        val relationshipAttributeTags = migrated.getObject("relationships", 0).getObject("attributes", 0).getArrayValues("tags")

        assertTrue(rootTags.isEmpty())
        assertTrue(entityTags.isEmpty())
        assertTrue(attributeTags.isEmpty())
        assertTrue(relationshipTags.isEmpty())
        assertTrue(relationshipAttributeTags.isEmpty())

        assertEquals(1, tagStore.groupsByKey.size)
        assertNotNull(tagStore.groupsByKey[TagGroupKey("security")])
        assertNotNull(tagStore.findTagByRefOptional(tagStore.globalRef("security", "Confidential")))
        assertNotNull(tagStore.findTagByRefOptional(tagStore.localRef(modelId, "localtag")))
        assertNotNull(tagStore.findTagByRefOptional(tagStore.localRef(modelId, "screenonly")))
        assertEquals(7, modelCmds.commands.size)
    }

    @Test
    fun `ignores invalid hashtags and keeps existing tag ids`() {
        val fs = ModelJsonFilesystemFixture()
        val files = ModelsStorageJsonFiles(fs.modelsDirectory())
        val modelId = "22222222-2222-2222-2222-222222222222"
        val existingTagId = "33333333-3333-3333-3333-333333333333"
        files.save("mixed-tags", createSchema_1_1_WithExistingTagIds(modelId, existingTagId).toString())
        val tagStore = InMemoryTagStore()
        val modelCmds = CollectingModelCmds()

        ModelsStorageJsonMigrations(files, prettyPrint = true).start(tagStore, tagStore, modelCmds)

        val migrated = parser.parseToJsonElement(files.load(io.medatarun.model.domain.ModelKey("mixed-tags"))).jsonObject
        val rootTags = migrated.getArrayValues("tags")
        assertTrue(rootTags.isEmpty())
        assertEquals(0, tagStore.tagsById.size)
        assertTrue(modelCmds.commands.isEmpty())
    }

    private fun createSchema_1_0_ModelJson() = buildJsonObject {
        put($$"$schema", ModelJsonSchemas.forVersion(ModelJsonSchemas.v_1_0))
        put("id", "legacy-model")
        put("version", "1.0.0")
        put("hashtags", buildJsonArray { add(JsonPrimitive("tag:legacy")) })
        putJsonArray("types") {
            add(buildJsonObject {
                put("id", "string")
            })
        }
        putJsonArray("entities") {
            add(buildJsonObject {
                put("id", "customer")
                put("identifierAttribute", "id")
                put("hashtags", buildJsonArray { add(JsonPrimitive("tag:entity")) })
                putJsonArray("attributes") {
                    add(buildJsonObject {
                        put("id", "id")
                        put("type", "string")
                    })
                }
            })
        }
    }

    private fun createSchema_1_1_ModelJson(modelId: String) = buildJsonObject {
        put($$"$schema", ModelJsonSchemas.forVersion(ModelJsonSchemas.v_1_1))
        put("id", modelId)
        put("key", "current-model")
        put("version", "1.0.0")
        put("tags", buildJsonArray { add(JsonPrimitive("tag:current")) })
        putJsonArray("types") {
            add(buildJsonObject {
                put("id", "type-id")
                put("key", "string")
            })
        }
        putJsonArray("entities") {
            add(buildJsonObject {
                put("id", "entity-id")
                put("key", "customer")
                put("identifierAttribute", "id")
                put("tags", buildJsonArray { add(JsonPrimitive("tag:entity")) })
                putJsonArray("attributes") {
                    add(buildJsonObject {
                        put("id", "attribute-id")
                        put("key", "id")
                        put("type", "string")
                    })
                }
            })
        }
    }

    private fun createSchema_1_1_WithLegacyHashtags(modelId: String) = buildJsonObject {
        put($$"$schema", ModelJsonSchemas.forVersion(ModelJsonSchemas.v_1_1))
        put("id", modelId)
        put("key", "tagged-model")
        put("version", "1.0.0")
        put("hashtags", buildJsonArray {
            add(JsonPrimitive("security:Confidential"))
            add(JsonPrimitive("local tag"))
            add(JsonPrimitive("###"))
        })
        putJsonArray("types") {
            add(buildJsonObject {
                put("id", "type-id")
                put("key", "string")
            })
        }
        putJsonArray("entities") {
            add(buildJsonObject {
                put("id", "entity-id")
                put("key", "customer")
                put("identifierAttribute", "id")
                put("hashtags", buildJsonArray {
                    add(JsonPrimitive("security:Confi:dential"))
                    add(JsonPrimitive("screen only"))
                })
                putJsonArray("attributes") {
                    add(buildJsonObject {
                        put("id", "attribute-id")
                        put("key", "id")
                        put("type", "string")
                        put("tags", buildJsonArray {
                            add(JsonPrimitive("screen only"))
                        })
                    })
                }
            })
        }
        putJsonArray("relationships") {
            add(buildJsonObject {
                put("id", "relationship-id")
                put("key", "customer_company")
                putJsonArray("roles") {}
                put("hashtags", buildJsonArray {
                    add(JsonPrimitive("security:Confidential"))
                })
                putJsonArray("attributes") {
                    add(buildJsonObject {
                        put("id", "relationship-attribute-id")
                        put("key", "source")
                        put("type", "string")
                        put("hashtags", buildJsonArray {
                            add(JsonPrimitive("screen only"))
                        })
                    })
                }
            })
        }
    }

    private fun createSchema_1_1_WithExistingTagIds(modelId: String, existingTagId: String) = buildJsonObject {
        put($$"$schema", ModelJsonSchemas.forVersion(ModelJsonSchemas.v_1_1))
        put("id", modelId)
        put("key", "mixed-tags")
        put("version", "1.0.0")
        put("tags", buildJsonArray {
            add(JsonPrimitive(existingTagId))
        })
        putJsonArray("types") {
            add(buildJsonObject {
                put("id", "type-id")
                put("key", "string")
            })
        }
        putJsonArray("entities") {
            add(buildJsonObject {
                put("id", "entity-id")
                put("key", "customer")
                put("identifierAttribute", "id")
                put("hashtags", buildJsonArray {
                    add(JsonPrimitive("###"))
                    add(JsonPrimitive(":"))
                })
                putJsonArray("attributes") {
                    add(buildJsonObject {
                        put("id", "attribute-id")
                        put("key", "id")
                        put("type", "string")
                    })
                }
            })
        }
    }

    private fun JsonObject.getObject(key: String, index: Int): JsonObject {
        val array = this[key] as JsonArray
        return array[index].jsonObject
    }

    private fun JsonObject.getArrayValues(key: String): List<String> {
        val array = this[key] as JsonArray
        return array.map { it.jsonPrimitive.content }
    }

    private class InMemoryTagStore : TagCmds, TagQueries {
        val groupsByKey = LinkedHashMap<TagGroupKey, TagGroupData>()
        val tagsById = LinkedHashMap<TagId, TagData>()

        override fun dispatch(cmd: TagCmd) {
            when (cmd) {
                is TagCmd.TagGroupCreate -> {
                    if (!groupsByKey.containsKey(cmd.key)) {
                        val group = TagGroupData(TagGroupId(UUID.randomUUID()), cmd.key, cmd.name, cmd.description)
                        groupsByKey[cmd.key] = group
                    }
                }

                is TagCmd.TagManagedCreate -> {
                    val group = findGroupByRef(cmd.groupRef) ?: return
                    val existing = findTagByRefOptional(
                        TagRef.ByKey(TagScopeRef.Global, group.key, cmd.key)
                    )
                    if (existing == null) {
                        val tag = TagData(
                            id = TagId(UUID.randomUUID()),
                            key = cmd.key,
                            scope = TagScopeRef.Global,
                            groupId = group.id,
                            name = cmd.name,
                            description = cmd.description
                        )
                        tagsById[tag.id] = tag
                    }
                }

                is TagCmd.TagFreeCreate -> {
                    val existing = findTagByRefOptional(TagRef.ByKey(cmd.scopeRef, null, cmd.key))
                    if (existing == null) {
                        val tag = TagData(
                            id = TagId(UUID.randomUUID()),
                            key = cmd.key,
                            scope = cmd.scopeRef,
                            groupId = null,
                            name = cmd.name,
                            description = cmd.description
                        )
                        tagsById[tag.id] = tag
                    }
                }

                else -> {}
            }
        }

        override fun findAllTags(): List<Tag> = tagsById.values.toList()

        override fun search(query: TagSearchFilters): List<Tag> = tagsById.values.toList()

        override fun findAllTagGroup(): List<TagGroup> = groupsByKey.values.toList()

        override fun findTagByRefOptional(tagRef: TagRef): Tag? {
            return when (tagRef) {
                is TagRef.ById -> tagsById[tagRef.id]
                is TagRef.ByKey -> tagsById.values.firstOrNull { matches(it, tagRef) }
            }
        }

        override fun findTagByRef(tagRef: TagRef): Tag {
            return findTagByRefOptional(tagRef) ?: error("Missing tag ${tagRef.asString()}")
        }

        override fun findTagByKeyOptional(id: TagGroupId, managedKey: TagKey): Tag? {
            return tagsById.values.firstOrNull { it.groupId == id && it.key == managedKey }
        }

        override fun findTagGroupByKeyOptional(groupKey: TagGroupKey): TagGroup? {
            return groupsByKey[groupKey]
        }

        override fun findTagByIdOptional(id: TagId): Tag? = tagsById[id]

        override fun findTagGroupByIdOptional(id: TagGroupId): TagGroup? {
            return groupsByKey.values.firstOrNull { it.id == id }
        }

        fun globalRef(groupKey: String, tagKey: String): TagRef.ByKey {
            return TagRef.ByKey(TagScopeRef.Global, TagGroupKey(groupKey), TagKey(tagKey))
        }

        fun localRef(modelId: String, tagKey: String): TagRef.ByKey {
            return TagRef.ByKey(
                modelTagScopeRef(Id.fromString(modelId, ::ModelId)),
                null,
                TagKey(tagKey)
            )
        }

        private fun findGroupByRef(groupRef: TagGroupRef): TagGroupData? {
            return when (groupRef) {
                is TagGroupRef.ById -> groupsByKey.values.firstOrNull { it.id == groupRef.id }
                is TagGroupRef.ByKey -> groupsByKey[groupRef.key]
            }
        }

        private fun matches(tag: TagData, tagRef: TagRef.ByKey): Boolean {
            if (tag.key != tagRef.key) {
                return false
            }
            if (tag.scope != tagRef.scopeRef) {
                return false
            }
            if (tag.scope.isGlobal) {
                val group = groupsByKey.values.firstOrNull { it.id == tag.groupId } ?: return false
                return group.key == tagRef.groupKey
            }
            return true
        }
    }

    private class CollectingModelCmds : ModelCmds {
        val commands = ArrayList<ModelCmd>()

        override fun dispatch(cmd: ModelCmd) {
            commands.add(cmd)
        }
    }

    private data class TagData(
        override val id: TagId,
        override val key: TagKey,
        override val scope: TagScopeRef,
        override val groupId: TagGroupId?,
        override val name: String?,
        override val description: String?
    ) : Tag

    private data class TagGroupData(
        override val id: TagGroupId,
        override val key: TagGroupKey,
        override val name: String?,
        override val description: String?
    ) : TagGroup
}
