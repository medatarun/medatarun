package io.medatarun.model.domain

import io.medatarun.model.actions.ModelAction
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.infra.*
import io.medatarun.model.internal.ModelValidationImpl
import io.medatarun.model.ports.exposed.*
import io.medatarun.tags.core.actions.TagAction
import io.medatarun.tags.core.domain.TagGroupKey
import io.medatarun.tags.core.domain.TagGroupRef
import io.medatarun.tags.core.domain.Tag
import io.medatarun.tags.core.domain.TagKey
import io.medatarun.tags.core.domain.TagRef
import io.medatarun.tags.core.domain.TagScopeRef
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.net.URI
import kotlin.test.*

class ModelTest {

    private fun createManagedTag(env: ModelTestEnv, groupKeyValue: String, tagKeyValue: String): Tag {
        val groupKey = TagGroupKey(groupKeyValue)
        val tagKey = TagKey(tagKeyValue)
        val tagRef = TagRef.ByKey(
            scopeRef = TagScopeRef.Global,
            groupKey = groupKey,
            key = tagKey
        )

        env.dispatchTag(TagAction.TagGroupCreate(groupKey, null, null))
        env.dispatchTag(TagAction.TagManagedCreate(TagGroupRef.ByKey(groupKey), tagKey, null, null))

        return env.tagQueries.findTagByRef(tagRef)
    }

    @Test
    fun `can not instantiate storages without repositories`() {
        assertFailsWith(ModelStoragesCompositeNoRepositoryException::class) {
            val c = ModelStoragesComposite({ emptyList() }, ModelValidationImpl())
            c.findAllModelIds()
        }
    }

    // ------------------------------------------------------------------------
    // Create models
    // ------------------------------------------------------------------------

    @Test
    fun `create model fail with ambiguous storage`() {
        val repo1 = ModelRepositoryInMemory("repo1")
        val repo2 = ModelRepositoryInMemory("repo2")
        val env = createEnv(repositories = listOf(repo1, repo2))
        assertFailsWith(ModelStoragesAmbiguousRepositoryException::class) {
            env.dispatch(
                ModelAction.Model_Create(
                    modelKey = ModelKey("m1"),
                    name = LocalizedTextNotLocalized("M1"),
                    description = null,
                    version = ModelVersion("1.0.0")
                )
            )
        }
    }

    @Test
    fun `create model ok with one storage mode auto`() {
        val repo1 = ModelRepositoryInMemory("repo1")
        val env = createEnv(repositories = listOf(repo1))
        val modelKey = ModelKey("m1")
        assertDoesNotThrow {
            env.dispatch(
                ModelAction.Model_Create(
                    modelKey = modelKey,
                    name = LocalizedTextNotLocalized("M1"),
                    description = null,
                    version = ModelVersion("1.0.0")
                )
            )
        }
        assertNotNull(repo1.findModelByKeyOptional(modelKey))
    }

    @Test
    fun `create model with name description and version when present`() {
        val env = createEnv()
        val query: ModelQueries = env.queries

        val modelKey = ModelKey("m-")
        val name = LocalizedTextNotLocalized("Model name")
        val description = LocalizedMarkdownNotLocalized("Model description")
        val version = ModelVersion("2.0.0")

        env.dispatch(ModelAction.Model_Create(modelKey, name, description, version))

        val reloaded = query.findModelByKey(modelKey)
        assertEquals(name, reloaded.name)
        assertEquals(description, reloaded.description)
        assertEquals(version, reloaded.version)
    }

    @Test
    fun `create model keeps values without optional description`() {

        val env = createEnv()
        val query: ModelQueries = env.queries

        val modelKey = ModelKey("m")
        val name = LocalizedTextNotLocalized("Model without description")
        val version = ModelVersion("3.0.0")

        env.dispatch(ModelAction.Model_Create(modelKey, name, null, version))

        val reloaded = query.findModelByKey(modelKey)
        assertEquals(name, reloaded.name)
        assertNull(reloaded.description)
        assertEquals(version, reloaded.version)
    }

    // ------------------------------------------------------------------------
    // Update models
    // ------------------------------------------------------------------------

    @Test
    fun `updates on model fails if model not found`() {
        val env = createEnv()
        val query: ModelQueries = env.queries

        val modelKey = ModelKey("m1")
        env.dispatch(
            ModelAction.Model_Create(
                modelKey = modelKey,
                name = LocalizedTextNotLocalized("Model name"),
                description = null,
                version = ModelVersion("2.0.0")
            )
        )
        val modelKeyWrong = modelRefKey("m2")
        assertFailsWith(ModelNotFoundException::class) {
            env.dispatch(
                ModelAction.Model_UpdateName(
                    modelRef = modelKeyWrong,
                    value = LocalizedTextNotLocalized("other")
                )
            )
        }
        assertFailsWith(ModelNotFoundException::class) {
            env.dispatch(
                ModelAction.Model_UpdateDescription(
                    modelRef = modelKeyWrong,
                    value = LocalizedMarkdownNotLocalized("other description")
                )
            )
        }
        assertFailsWith(ModelNotFoundException::class) {
            env.dispatch(
                ModelAction.Model_UpdateVersion(
                    modelRef = modelKeyWrong,
                    value = ModelVersion("3.0.0")
                )
            )
        }
        env.dispatch(
            ModelAction.Model_UpdateName(
                modelRef = modelRefKey(modelKey),
                value = LocalizedTextNotLocalized("Model name 2")
            )
        )
        assertEquals(LocalizedTextNotLocalized("Model name 2"), query.findModelByKey(modelKey).name)
    }

    class TestEnvOneModel {
        private val env = createEnv()
        val runtime: ModelTestEnv
            get() = env
        val query: ModelQueries = env.queries
        private val modelKey = ModelKey("m1")
        val modelRef = modelRef(modelKey)
        val dispatch = env::dispatch
        init {
            env.dispatch(
                ModelAction.Model_Create(
                    modelKey = modelKey,
                    name = LocalizedTextNotLocalized("Model name"),
                    description = null,
                    version = ModelVersion("2.0.0")
                )
            )
            env.dispatch(
                ModelAction.Type_Create(
                    modelRef = modelRef,
                    typeKey = TypeKey("String"),
                    name = null,
                    description = null
                )
            )
        }
    }


    @Test
    fun `updates on model name persists the name`() {
        val env = TestEnvOneModel()
        env.dispatch(
            ModelAction.Model_UpdateName(
                modelRef = env.modelRef,
                value = LocalizedTextNotLocalized("Model name 2")
            )
        )
        assertEquals(LocalizedTextNotLocalized("Model name 2"), env.query.findModel(env.modelRef).name)
    }

    @Test
    fun `updates on model description persists the description`() {
        val env = TestEnvOneModel()
        env.dispatch(
            ModelAction.Model_UpdateDescription(
                modelRef = env.modelRef,
                value = LocalizedMarkdownNotLocalized("Model description 2")
            )
        )
        assertEquals(
            LocalizedMarkdownNotLocalized("Model description 2"),
            env.query.findModel(env.modelRef).description
        )
    }

    @Test
    fun `updates on model description to null persists the description`() {
        val env = TestEnvOneModel()
        env.dispatch(
            ModelAction.Model_UpdateDescription(
                modelRef = env.modelRef,
                value = LocalizedMarkdownNotLocalized("Model description 2")
            )
        )
        env.dispatch(ModelAction.Model_UpdateDescription(env.modelRef, null))
        assertNull(env.query.findModel(env.modelRef).description)
    }

    @Test
    fun `updates on model version persists the version`() {
        val env = TestEnvOneModel()
        env.dispatch(ModelAction.Model_UpdateVersion(env.modelRef, ModelVersion("4.5.6")))
        assertEquals(ModelVersion("4.5.6"), env.query.findModel(env.modelRef).version)
    }

    @Test
    fun `update documentation home with value then updated`() {
        val env = TestEnvOneModel()
        val url = URI("https://some.url/index.html").toURL()
        env.dispatch(ModelAction.Model_UpdateDocumentationHome(env.modelRef, url.toString()))
        assertEquals(url, env.query.findModel(env.modelRef).documentationHome)
    }

    @Test
    fun `update documentation home with null then updated to null`() {
        val env = TestEnvOneModel()
        val url = URI("https://some.url/index.html").toURL()
        env.dispatch(ModelAction.Model_UpdateDocumentationHome(env.modelRef, url.toString()))
        env.dispatch(ModelAction.Model_UpdateDocumentationHome(env.modelRef, null))
        assertNull(env.query.findModel(env.modelRef).documentationHome)
    }

    @Test
    fun `add and delete tag on model persists tag ids`() {
        val env = TestEnvOneModel()
        val managedTag = createManagedTag(env.runtime, "g-model", "t-model")

        env.dispatch(ModelAction.Model_AddTag(env.modelRef, managedTag.ref))
        assertEquals(listOf(managedTag.id), env.query.findModel(env.modelRef).tags)

        env.dispatch(ModelAction.Model_DeleteTag(env.modelRef, managedTag.ref))
        assertTrue(env.query.findModel(env.modelRef).tags.isEmpty())
    }

    // ------------------------------------------------------------------------
    // Delete models
    // ------------------------------------------------------------------------

    @Test
    fun `delete model fails if model Id not found in any storage`() {
        val env = createEnv()
        env.dispatch(ModelAction.Model_Create(ModelKey("m-to-delete-1"), LocalizedTextNotLocalized("Model to delete"), null, ModelVersion("0.0.1")))
        env.dispatch(ModelAction.Model_Create(ModelKey("m-to-delete-2"), LocalizedTextNotLocalized("Model to delete 2"), null, ModelVersion("0.0.1")))
        assertThrows<ModelNotFoundException> {
            env.dispatch(ModelAction.Model_Delete(modelRefKey("m-to-delete-3")))
        }
    }

    @Test
    fun `delete model removes it from storage`() {
        val env = createEnv()
        val query: ModelQueries = env.queries

        env.dispatch(ModelAction.Model_Create(ModelKey("m-to-delete-1"), LocalizedTextNotLocalized("Model to delete"), null, ModelVersion("0.0.1")))
        env.dispatch(ModelAction.Model_Create(ModelKey("m-to-keep-1"), LocalizedTextNotLocalized("Model to preserve"), null, ModelVersion("0.1.0")))
        env.dispatch(ModelAction.Model_Create(ModelKey("m-to-delete-2"), LocalizedTextNotLocalized("Model to delete 2"), null, ModelVersion("0.0.1")))
        env.dispatch(ModelAction.Model_Create(ModelKey("m-to-keep-2"), LocalizedTextNotLocalized("Model to preserve 2"), null, ModelVersion("0.1.0")))

        env.dispatch(ModelAction.Model_Delete(modelRef("m-to-delete-1")))
        assertNull(query.findModelOptional(modelRef("m-to-delete-1")))

        assertNotNull(query.findModelOptional(modelRef("m-to-keep-1")))
        assertNotNull(query.findModelOptional(modelRef("m-to-delete-2")))
        assertNotNull(query.findModelOptional(modelRef("m-to-keep-2")))

        env.dispatch(ModelAction.Model_Delete(modelRefKey("m-to-delete-2")))
        assertNull(query.findModelOptional(modelRef("m-to-delete-2")))
        assertNotNull(query.findModelOptional(modelRef("m-to-keep-1")))
        assertNotNull(query.findModelOptional(modelRef("m-to-keep-2")))

        env.dispatch(ModelAction.Model_Delete(modelRef("m-to-keep-1")))
        assertNull(query.findModelOptional(modelRef("m-to-keep-1")))
        assertNotNull(query.findModelOptional(modelRef("m-to-keep-2")))

        env.dispatch(ModelAction.Model_Delete(modelRefKey("m-to-keep-2")))
        assertNull(query.findModelOptional(modelRef("m-to-keep-2")))

    }

    // ------------------------------------------------------------------------
    // Types
    // ------------------------------------------------------------------------

    class TestEnvTypes {
        val runtime = createEnv()
        val query: ModelQueries = runtime.queries
        private val modelKey = ModelKey("m1")
        val modelRef = modelRefKey(ModelKey("m1"))
        val dispatch = runtime::dispatch

        init {
            runtime.dispatch(
                ModelAction.Model_Create(
                    modelKey = modelKey,
                    name = LocalizedTextNotLocalized("Model name"),
                    description = null,
                    version = ModelVersion("2.0.0")
                )
            )
        }

        val model: Model
            get() {
                return query.findModelByKey(modelKey)
            }
    }

    @Test
    fun `create type`() {
        val env = TestEnvTypes()
        env.runtime.dispatch(
            ModelAction.Type_Create(
                modelRef = env.modelRef,
                typeKey = TypeKey("String"),
                name = LocalizedTextNotLocalized("Simple string"),
                description = LocalizedMarkdownNotLocalized("Simple string description")
            )
        )
        assertEquals(1, env.model.types.size)
        val type = env.model.findTypeOptional(TypeKey("String"))
        assertNotNull(type)
        assertEquals(LocalizedTextNotLocalized("Simple string"), type.name)
        assertEquals(LocalizedMarkdownNotLocalized("Simple string description"), type.description)
    }

    @Test
    fun `create type without name and description`() {
        val env = TestEnvTypes()
        env.runtime.dispatch(
            ModelAction.Type_Create(
                modelRef = env.modelRef,
                typeKey = TypeKey("String"),
                name = null,
                description = null
            )
        )
        assertEquals(1, env.model.types.size)
        val type = env.model.findTypeOptional(TypeKey("String"))
        assertNotNull(type)
        assertNull(type.name)
        assertNull(type.description)
    }

    @Test
    fun `create type on unknown model throw ModelNotFoundException`() {
        val env = TestEnvTypes()
        assertThrows<ModelNotFoundException> {
            env.dispatch(
                ModelAction.Type_Create(
                    modelRef = modelRefKey("unknown"),
                    typeKey = TypeKey("String"),
                    name = null,
                    description = null
                )
            )
        }
    }

    @Test
    fun `create type with duplicate name throws DuplicateTypeException`() {
        val env = TestEnvTypes()
        env.dispatch(ModelAction.Type_Create(env.modelRef, TypeKey("String"), null, null))
        assertThrows<TypeCreateDuplicateException> {
            env.dispatch(
                ModelAction.Type_Create(
                    modelRef = env.modelRef,
                    typeKey = TypeKey("String"),
                    name = null,
                    description = null
                )
            )
        }
    }

    @Test
    fun `update type name `() {
        val env = TestEnvTypes()
        val typeKey = TypeKey("String")
        val typeRef = TypeRef.ByKey(typeKey)
        env.dispatch(ModelAction.Type_Create(env.modelRef, typeKey, null, null))
        env.dispatch(
            ModelAction.Type_UpdateName(
                modelRef = env.modelRef,
                typeRef = typeRef,
                value = LocalizedTextNotLocalized("This is a string")
            )
        )
        val t = env.model.findTypeOptional(typeRef)
        assertNotNull(t)
        assertEquals(LocalizedTextNotLocalized("This is a string"), t.name)
    }

    @Test
    fun `update type name with null`() {
        val env = TestEnvTypes()
        val typeKey = TypeKey("String")
        val typeRef = TypeRef.ByKey(typeKey)
        env.dispatch(ModelAction.Type_Create(env.modelRef, typeKey, null, null))
        env.dispatch(ModelAction.Type_UpdateName(env.modelRef, typeRef, null))
        val t = env.model.findTypeOptional(typeRef)
        assertNotNull(t)
        assertNull(t.name)
    }

    @Test
    fun `update type description`() {
        val env = TestEnvTypes()
        val typeKey = TypeKey("String")
        val typeRef = TypeRef.ByKey(typeKey)
        env.dispatch(ModelAction.Type_Create(env.modelRef, typeKey, null, null))
        env.dispatch(
            ModelAction.Type_UpdateDescription(
                modelRef = env.modelRef,
                typeRef = typeRef,
                value = LocalizedMarkdownNotLocalized("This is a string")
            )
        )
        val t = env.model.findTypeOptional(typeRef)
        assertNotNull(t)
        assertEquals(LocalizedMarkdownNotLocalized("This is a string"), t.description)
    }

    @Test
    fun `update type description with null`() {
        val env = TestEnvTypes()
        val typeKey = TypeKey("String")
        val typeRef = TypeRef.ByKey(typeKey)
        env.dispatch(ModelAction.Type_Create(env.modelRef, typeKey, null, null))
        env.dispatch(ModelAction.Type_UpdateDescription(env.modelRef, typeRef, null))
        val t = env.model.findTypeOptional(typeRef)
        assertNotNull(t)
        assertNull(t.description)
    }

    @Test
    fun `update type with model not found`() {
        val env = TestEnvTypes()
        val typeKey = TypeKey("String")
        val typeRef = TypeRef.ByKey(typeKey)
        env.dispatch(ModelAction.Type_Create(env.modelRef, typeKey, null, null))
        assertThrows<ModelNotFoundException> {
            env.dispatch(
                ModelAction.Type_UpdateDescription(
                    modelRef = modelRefKey("unknown"),
                    typeRef = typeRef,
                    value = null
                )
            )
        }
    }

    @Test
    fun `update type with type not found`() {
        val env = TestEnvTypes()
        val typeKey = TypeKey("String")
        env.dispatch(ModelAction.Type_Create(env.modelRef, typeKey, null, null))
        assertThrows<TypeNotFoundException> {
            env.dispatch(
                ModelAction.Type_UpdateDescription(
                    modelRef = env.modelRef,
                    typeRef = TypeRef.ByKey(TypeKey("String2")),
                    value = null
                )
            )
        }
    }

    @Test
    fun `delete type model not found`() {
        val env = TestEnvTypes()
        val typeKey = TypeKey("String")
        val typeRef = TypeRef.ByKey(typeKey)
        env.dispatch(ModelAction.Type_Create(env.modelRef, typeKey, null, null))
        assertThrows<ModelNotFoundException> {
            env.dispatch(ModelAction.Type_Delete(modelRefKey("unknown"), typeRef))
        }
    }

    @Test
    fun `delete type type not found`() {
        val env = TestEnvTypes()
        val typeKey = TypeKey("String")
        val typeKeyWrong = TypeKey("String2")
        val typeRefWrong = TypeRef.ByKey(typeKeyWrong)
        env.dispatch(ModelAction.Type_Create(env.modelRef, typeKey, null, null))
        assertThrows<TypeNotFoundException> {
            env.dispatch(ModelAction.Type_Delete(env.modelRef, typeRefWrong))
        }
    }

    @Test
    fun `delete type used in attributes then error`() {
        val env = TestEnvTypes()
        env.dispatch(ModelAction.Type_Create(env.modelRef, TypeKey("String"), null, null))
        env.dispatch(ModelAction.Type_Create(env.modelRef, TypeKey("Markdown"), null, null))
        env.dispatch(ModelAction.Type_Create(env.modelRef, TypeKey("Int"), null, null))
        val entityKey = EntityKey("contact")
        val entityRef = EntityRef.ByKey(entityKey)
        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = env.modelRef,
                entityKey = entityKey,
                name = null,
                description = null,
                identityAttributeKey = AttributeKey("name"),
                identityAttributeType = typeRef("String"),
                identityAttributeName = null,
                documentationHome = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = env.modelRef,
                entityRef = entityRef,
                attributeKey = AttributeKey("infos"),
                type = TypeRef.ByKey(TypeKey("Markdown")),
                optional = false,
                name = null,
                description = null
            )
        )
        assertThrows<ModelTypeDeleteUsedException> {
            env.dispatch(ModelAction.Type_Delete(env.modelRef, typeRef("String")))
        }
        assertThrows<ModelTypeDeleteUsedException> {
            env.dispatch(ModelAction.Type_Delete(env.modelRef, TypeRef.ByKey(TypeKey("Markdown"))))
        }
        env.dispatch(ModelAction.Type_Delete(env.modelRef, TypeRef.ByKey(TypeKey("Int"))))

    }

    @Test
    fun `delete type success`() {
        val env = TestEnvTypes()
        env.dispatch(ModelAction.Type_Create(env.modelRef, TypeKey("String"), null, null))
        env.dispatch(ModelAction.Type_Create(env.modelRef, TypeKey("Markdown"), null, null))
        env.dispatch(ModelAction.Type_Create(env.modelRef, TypeKey("Int"), null, null))
        env.dispatch(ModelAction.Type_Delete(env.modelRef, TypeRef.ByKey(TypeKey("Int"))))
        assertNull(env.model.findTypeOptional(TypeRef.ByKey(TypeKey("Int"))))
        assertNotNull(env.model.findTypeOptional(typeRef("String")))
        assertNotNull(env.model.findTypeOptional(TypeRef.ByKey(TypeKey("Markdown"))))

    }

    // ------------------------------------------------------------------------
    // Entities
    // ------------------------------------------------------------------------

    @Test
    fun `create entity then id and name shall persist`() {
        val env = TestEnvOneModel()
        val entityId = EntityKey("entity")
        val entityRef = EntityRef.ByKey(entityId)
        val name = LocalizedTextNotLocalized("Order")
        val description = LocalizedMarkdownNotLocalized("Order description")
        val docHome= "http://test.dev/local=123"
        env.dispatch(
            ModelAction.Entity_Create(
                env.modelRef,
                entityKey = entityId,
                name = name,
                description = description,
                identityAttributeKey = AttributeKey("id"),
                identityAttributeName = LocalizedTextNotLocalized("Identifier"),
                documentationHome = docHome,
                identityAttributeType = typeRef("String")
            )
        )

        val reloaded = env.query.findEntity(env.modelRef, entityRef)
        assertEquals(entityId, reloaded.key)
        assertEquals(name, reloaded.name)
        assertEquals(description, reloaded.description)
        assertEquals(URI.create(docHome).toURL(), reloaded.documentationHome)
        assertEquals(1, reloaded.attributes.size)
        val attrId = reloaded.attributes[0]
        assertEquals(AttributeKey("id"), attrId.key)
        assertEquals("Identifier", attrId.name?.name)
        assertNull( attrId.description?.name)
    }

    @Test
    fun `create entity with null name then name shall be null`() {
        val env = TestEnvOneModel()
        val entityKey = EntityKey("entity-null-name")
        val entityRef = EntityRef.ByKey(entityKey)
        val description = LocalizedMarkdownNotLocalized("Entity without name")

        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = env.modelRef,
                entityKey = entityKey,
                name = null,
                description = description,
                identityAttributeKey = AttributeKey("id"),
                identityAttributeType = typeRef("String"),
                identityAttributeName = null,
                documentationHome = null
            )
        )

        val reloaded = env.query.findEntity(env.modelRef, entityRef)
        assertEquals(entityKey, reloaded.key)
        assertNull(reloaded.name)
        assertEquals(description, reloaded.description)
        assertEquals(1, reloaded.attributes.size)
    }

    @Test
    fun `create entity with null description then description shall be null`() {
        val env = TestEnvOneModel()
        val entityKey = EntityKey("entity-null-description")
        val entityRef = EntityRef.ByKey(entityKey)
        val name = LocalizedTextNotLocalized("Entity without description")

        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = env.modelRef,
                entityKey = entityKey,
                name = name,
                description = null,
                identityAttributeKey = AttributeKey("String"),
                identityAttributeType = typeRef("String"),
                identityAttributeName = null,
                documentationHome = null
            )
        )

        val reloaded = env.query.findEntity(env.modelRef, entityRef)
        assertEquals(entityKey, reloaded.key)
        assertEquals(name, reloaded.name)
        assertNull(reloaded.description)
    }

    @Test
    fun `create entity with null attribute name and description then name and desc shall be null`() {
        val env = TestEnvOneModel()
        val entityKey = EntityKey("entity-null-attr-name")
        val entityRef = EntityRef.ByKey(entityKey)
        val description = LocalizedMarkdownNotLocalized("Entity without name")

        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = env.modelRef,
                entityKey = entityKey,
                name = null,
                description = description,
                identityAttributeKey = AttributeKey("id"),
                identityAttributeType = typeRef("String"),
                identityAttributeName = null,
                documentationHome = null
            )
        )

        val reloaded = env.query.findEntity(env.modelRef, entityRef)
        assertNull(reloaded.attributes[0].name)
        assertNull(reloaded.attributes[0].description)
    }

    @Test
    fun `create entity with documentation home null`() {
        val env = TestEnvOneModel()
        val entityKey = EntityKey("entity-null-attr-name")
        val entityRef = EntityRef.ByKey(entityKey)
        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = env.modelRef,
                entityKey = entityKey,
                name = null,
                description = null,
                identityAttributeKey = AttributeKey("id"),
                identityAttributeType = typeRef("String"),
                identityAttributeName = null,
                documentationHome = null
            )
        )
        assertNull(env.query.findEntity(env.modelRef, entityRef).documentationHome)
    }

    @Test
    fun `create entity with documentation home not null`() {
        val env = TestEnvOneModel()
        val entityKey = EntityKey("entity-null-attr-name")
        val entityRef = EntityRef.ByKey(entityKey)
        val url = URI("http://localhost:8080").toURL()
        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = env.modelRef,
                entityKey = entityKey,
                name = null,
                description = null,
                identityAttributeKey = AttributeKey("id"),
                identityAttributeType = typeRef("String"),
                identityAttributeName = null,
                documentationHome = url.toString()
            )
        )
        assertEquals(url, env.query.findEntity(env.modelRef, entityRef).documentationHome)
    }

    class TestEnvEntityUpdate {
        val runtime = createEnv()
        val dispatch = runtime::dispatch
        val query: ModelQueries = runtime.queries
        private val modelKey = ModelKey("model-entity-update")
        val modelRef = modelRefKey(ModelKey("model-entity-update"))
        val primaryEntityKey = EntityKey("entity-primary")
        val primaryEntityRef = EntityRef.ByKey(primaryEntityKey)
        val secondaryEntityKey = EntityKey("entity-secondary")
        val secondaryEntityRef = EntityRef.ByKey(secondaryEntityKey)

        init {
            runtime.dispatch(
                ModelAction.Model_Create(
                    modelKey = modelKey,
                    name = LocalizedTextNotLocalized("Model entity update"),
                    description = null,
                    version = ModelVersion("1.0.0")
                )
            )
            runtime.dispatch(ModelAction.Type_Create(modelRef, TypeKey("String"), null, null))
            runtime.dispatch(
                ModelAction.Entity_Create(
                    modelRef = modelRef,
                    entityKey = primaryEntityKey,
                    name = LocalizedTextNotLocalized("Entity primary"),
                    description = LocalizedMarkdownNotLocalized("Entity primary description"),
                    identityAttributeKey = AttributeKey("id"),
                    identityAttributeType = typeRef("String"),
                    identityAttributeName = null,
                    documentationHome = null
                )
            )
            runtime.dispatch(
                ModelAction.Entity_Create(
                    modelRef = modelRef,
                    entityKey = secondaryEntityKey,
                    name = LocalizedTextNotLocalized("Entity secondary"),
                    description = LocalizedMarkdownNotLocalized("Entity secondary description"),
                    identityAttributeKey = AttributeKey("id"),
                    identityAttributeType = typeRef("String"),
                    identityAttributeName = null,
                    documentationHome = null
                )
            )
        }
    }

    @Test
    fun `update entity with wrong model id throws ModelNotFoundException`() {
        val env = TestEnvEntityUpdate()
        val wrongModelKey = modelRefKey("unknown-model")

        assertFailsWith<ModelNotFoundException> {
            env.runtime.dispatch(
                ModelAction.Entity_UpdateName(
                    wrongModelKey,
                    env.primaryEntityRef,
                    LocalizedTextNotLocalized("Updated name")
                )
            )
        }
    }

    @Test
    fun `update entity with wrong entity id throws EntityNotFoundException`() {
        val env = TestEnvEntityUpdate()
        val wrongEntityId = EntityKey("unknown-entity")
        val wrongEntityRef = EntityRef.ByKey(wrongEntityId)

        assertFailsWith<EntityNotFoundException> {
            env.dispatch(
                ModelAction.Entity_UpdateName(
                    env.modelRef,
                    wrongEntityRef,
                    LocalizedTextNotLocalized("Updated name")
                )
            )
        }
    }

    @Test
    fun `update entity id with duplicate id throws exception`() {
        val env = TestEnvEntityUpdate()
        val duplicateId = env.secondaryEntityKey

        assertFailsWith<EntityUpdateIdDuplicateIdException> {
            env.dispatch(
                ModelAction.Entity_UpdateKey(
                    env.modelRef,
                    env.primaryEntityRef,
                    duplicateId
                )
            )
        }
    }

    @Test
    fun `update entity id with correct id ok`() {
        val env = TestEnvEntityUpdate()
        val newId = EntityKey("entity-renamed")

        env.dispatch(ModelAction.Entity_UpdateKey(env.modelRef, env.primaryEntityRef, newId))

        val reloaded = env.query.findModel(env.modelRef)
        assertNull(reloaded.findEntityOptional(env.primaryEntityKey))
        assertNotNull(reloaded.findEntityOptional(newId))
    }

    @Test
    fun `update entity name not null persisted`() {
        val env = TestEnvEntityUpdate()
        val newName = LocalizedTextNotLocalized("Entity primary updated")

        env.dispatch(ModelAction.Entity_UpdateName(env.modelRef, env.primaryEntityRef, newName))

        val reloaded = env.query.findEntity(env.modelRef, env.primaryEntityRef)
        assertEquals(newName, reloaded.name)
    }

    @Test
    fun `update entity name null then name is null`() {
        val env = TestEnvEntityUpdate()

        env.dispatch(ModelAction.Entity_UpdateName(env.modelRef, env.primaryEntityRef, null))

        val reloaded = env.query.findEntity(env.modelRef, env.primaryEntityRef)
        assertNull(reloaded.name)
    }

    @Test
    fun `update entity description not null persisted`() {
        val env = TestEnvEntityUpdate()
        val newDescription = LocalizedMarkdownNotLocalized("Primary entity updated description")

        env.dispatch(ModelAction.Entity_UpdateDescription(env.modelRef, env.primaryEntityRef, newDescription))

        val reloaded = env.query.findEntity(env.modelRef, env.primaryEntityRef)
        assertEquals(newDescription, reloaded.description)
    }

    @Test
    fun `update entity description with null then description is null`() {
        val env = TestEnvEntityUpdate()

        env.dispatch(ModelAction.Entity_UpdateDescription(env.modelRef, env.primaryEntityRef, null))

        val reloaded = env.query.findEntity(env.modelRef, env.primaryEntityRef)
        assertNull(reloaded.description)
    }

    @Test
    fun `update entity documentation home not null`() {
        val env = TestEnvEntityUpdate()
        val url = URI("http://localhost").toURL()
        env.dispatch(ModelAction.Entity_UpdateDocumentationHome(env.modelRef, env.primaryEntityRef, url.toString()))
        val reloaded = env.query.findEntity(env.modelRef, env.primaryEntityRef)
        assertEquals(url, reloaded.documentationHome)
    }

    @Test
    fun `update entity documentation home to null`() {
        val env = TestEnvEntityUpdate()
        val url = URI("http://localhost").toURL()
        env.dispatch(ModelAction.Entity_UpdateDocumentationHome(env.modelRef, env.primaryEntityRef, url.toString()))
        env.dispatch(ModelAction.Entity_UpdateDocumentationHome(env.modelRef, env.primaryEntityRef, null))
        val reloaded = env.query.findEntity(env.modelRef, env.primaryEntityRef)
        assertNull(reloaded.documentationHome)
    }

    @Test
    fun `add and delete tag on entity persists tag ids`() {
        val env = TestEnvEntityUpdate()
        val managedTag = createManagedTag(env.runtime, "g-entity", "t-entity")

        env.dispatch(ModelAction.Entity_AddTag(env.modelRef, env.primaryEntityRef, managedTag.ref))
        assertEquals(listOf(managedTag.id), env.query.findEntity(env.modelRef, env.primaryEntityRef).tags)

        env.dispatch(ModelAction.Entity_DeleteTag(env.modelRef, env.primaryEntityRef, managedTag.ref))
        assertTrue(env.query.findEntity(env.modelRef, env.primaryEntityRef).tags.isEmpty())
    }


    @Test
    fun `delete entity in model then entity removed`() {
        val env = TestEnvOneModel()
        val entityId = EntityKey("entity-to-delete")
        val entityRef = EntityRef.ByKey(entityId)

        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = env.modelRef,
                entityKey = entityId,
                name = LocalizedTextNotLocalized("To delete"),
                description = null,
                identityAttributeKey = AttributeKey("id"),
                identityAttributeType = typeRef("String"),
                identityAttributeName = null,
                documentationHome = null
            )
        )
        env.dispatch(ModelAction.Entity_Delete(env.modelRef, entityRef))

        val reloaded = env.query.findModel(env.modelRef)
        assertNull(reloaded.findEntityOptional(entityId))
    }

    @Test
    fun `delete entity with same id in two models then only entity in the specified model is removed`() {
        val runtime = createEnv()
        val query: ModelQueries = runtime.queries

        val modelKey1 = ModelKey("model-1")
        val modelRef1 = modelRefKey(modelKey1)
        val modelKey2 = ModelKey("model-2")
        val modelRef2 = modelRefKey(modelKey2)
        val entityKey = EntityKey("shared-entity")
        val entityRef = EntityRef.ByKey(entityKey)

        runtime.dispatch(ModelAction.Model_Create(modelKey1, LocalizedTextNotLocalized("Model 1"), null, ModelVersion("1.0.0")))
        runtime.dispatch(ModelAction.Type_Create(modelRef1, TypeKey("String"), null, null))
        runtime.dispatch(ModelAction.Model_Create(modelKey2, LocalizedTextNotLocalized("Model 2"), null, ModelVersion("1.0.0")))
        runtime.dispatch(ModelAction.Type_Create(modelRef2, TypeKey("String"), null, null))
        runtime.dispatch(
            ModelAction.Entity_Create(
                modelRef = modelRef1,
                entityKey = entityKey,
                name = LocalizedTextNotLocalized("Entity"),
                description = null,
                identityAttributeKey = AttributeKey("id"),
                identityAttributeType = typeRef("String"),
                identityAttributeName = null,
                documentationHome = null
            )
        )
        runtime.dispatch(
            ModelAction.Entity_Create(
                modelRef = modelRef2,
                entityKey = entityKey,
                name = LocalizedTextNotLocalized("Entity"),
                description = null,
                identityAttributeKey = AttributeKey("id"),
                identityAttributeType = typeRef("String"),
                identityAttributeName = null,
                documentationHome = null
            )
        )

        runtime.dispatch(ModelAction.Entity_Delete(modelRef1, entityRef))

        val reloadedModel1 = query.findModel(modelRef1)
        val reloadedModel2 = query.findModel(modelRef2)

        assertNull(reloadedModel1.findEntityOptional(entityKey))
        assertNotNull(reloadedModel2.findEntityOptional(entityKey))
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Attributes
    // -----------------------------------------------------------------------------------------------------------------

    class TestEnvAttribute {
        val runtime = createEnv()
        val dispatch = runtime::dispatch
        val query: ModelQueries = runtime.queries
        private val sampleModelKey = ModelKey("model-1")
        val sampleModelRef = modelRefKey(sampleModelKey)
        val sampleEntityKey = EntityKey("Entity1")
        val sampleEntityRef = EntityRef.ByKey(sampleEntityKey)

        init {
            runtime.dispatch(
                ModelAction.Model_Create(
                    sampleModelKey,
                    LocalizedTextNotLocalized("Model 1"),
                    null,
                    ModelVersion("1.0.0")
                )
            )
            runtime.dispatch(ModelAction.Type_Create(sampleModelRef, TypeKey("String"), null, null))
        }

        fun addSampleEntity() {
            runtime.dispatch(
                ModelAction.Entity_Create(
                    modelRef = sampleModelRef,
                    entityKey = sampleEntityKey,
                    name = null,
                    description = null,
                    identityAttributeKey = AttributeKey("id"),
                    identityAttributeType = typeRef("String"),
                    identityAttributeName = null,
                    documentationHome = null
                )
            )
        }

        fun createAttribute(
            attributeKey: AttributeKey = AttributeKey("myattribute"),
            type: TypeRef = typeRef("String"),
            optional: Boolean = false,
            name: LocalizedText? = null,
            description: LocalizedMarkdown? = null
        ): Attribute {

            runtime.dispatch(
                ModelAction.EntityAttribute_Create(
                    modelRef = sampleModelRef,
                    entityRef = sampleEntityRef,
                    attributeKey = attributeKey,
                    type = type,
                    optional = optional,
                    name = name,
                    description = description
                )
            )
            val model = query.findModel(sampleModelRef)
            val attributeRef = EntityAttributeRef.ByKey(attributeKey)
            val reloaded = model.findEntityAttributeOptional(sampleEntityRef, attributeRef)
                ?: throw EntityAttributeNotFoundException(sampleModelRef, sampleEntityRef, attributeRef)
            return reloaded
        }

        fun updateAttribute(
            attributeRef: EntityAttributeRef = EntityAttributeRef.ByKey(AttributeKey("myattribute")),
            command: AttributeUpdateCmd,
            reloadId: EntityAttributeRef? = null
        ): Attribute {

            // TODO supprimer ca et réintégrer dans chaque test

            when (command) {
                is AttributeUpdateCmd.Key -> runtime.dispatch(
                    ModelAction.EntityAttribute_UpdateId(sampleModelRef, sampleEntityRef, attributeRef, command.value)
                )

                is AttributeUpdateCmd.Name -> runtime.dispatch(
                    ModelAction.EntityAttribute_UpdateName(sampleModelRef, sampleEntityRef, attributeRef, command.value)
                )

                is AttributeUpdateCmd.Description -> runtime.dispatch(
                    ModelAction.EntityAttribute_UpdateDescription(
                        sampleModelRef,
                        sampleEntityRef,
                        attributeRef,
                        command.value
                    )
                )

                is AttributeUpdateCmd.Type -> runtime.dispatch(
                    ModelAction.EntityAttribute_UpdateType(sampleModelRef, sampleEntityRef, attributeRef, command.value)
                )

                is AttributeUpdateCmd.Optional -> runtime.dispatch(
                    ModelAction.EntityAttribute_UpdateOptional(sampleModelRef, sampleEntityRef, attributeRef, command.value)
                )
            }
            val reloaded = query.findEntityAttribute(sampleModelRef, sampleEntityRef, reloadId ?: attributeRef)
            return reloaded
        }

    }

    @Test
    fun `create attribute then id name and description shall persist`() {
        val env = TestEnvAttribute()
        env.addSampleEntity()
        val reloaded = env.createAttribute(
            attributeKey = AttributeKey("businesskey"),
            type = typeRef("String"),
            optional = false,
            name = LocalizedTextNotLocalized("Business Key"),
            description = LocalizedMarkdownNotLocalized("Unique business key"),
        )
        val type = env.query.findType(env.sampleModelRef, typeRef("String"))
        assertEquals(AttributeKey("businesskey"), reloaded.key)
        assertEquals(LocalizedTextNotLocalized("Business Key"), reloaded.name)
        assertEquals(LocalizedMarkdownNotLocalized("Unique business key"), reloaded.description)
        assertEquals(type.id, reloaded.typeId)
        assertEquals(false, reloaded.optional)
    }

    @Test
    fun `create attribute with null name then name shall be null`() {
        val env = TestEnvAttribute()
        env.addSampleEntity()
        val reloaded = env.createAttribute(name = null)
        assertNull(reloaded.name)
    }

    @Test
    fun `create attribute with null description then description shall be null`() {
        val env = TestEnvAttribute()
        env.addSampleEntity()
        val reloaded = env.createAttribute(description = null)
        assertNull(reloaded.description)
    }

    @Test
    fun `create attribute with optional true description then optional is true`() {
        val env = TestEnvAttribute()
        env.addSampleEntity()
        val reloaded = env.createAttribute(optional = true)
        assertTrue(reloaded.optional)
    }

    @Test
    fun `create attribute with type boolean then type found`() {
        val env = TestEnvAttribute()
        env.addSampleEntity()
        val typeKey = TypeKey("Boolean")
        env.dispatch(ModelAction.Type_Create(env.sampleModelRef, typeKey, null, null))
        val type = env.query.findType(env.sampleModelRef, TypeRef.ByKey(typeKey))
        val reloaded = env.createAttribute(type = typeRef("Boolean"))
        assertEquals(type.id, reloaded.typeId)
    }


    @Test
    fun `create attribute with duplicate id then error`() {
        val env = TestEnvAttribute()
        env.addSampleEntity()
        env.createAttribute(attributeKey = AttributeKey("lastname"))
        assertFailsWith<CreateAttributeDuplicateIdException> {
            env.createAttribute(attributeKey = AttributeKey("lastname"))
        }
    }

    @Test
    fun `create attribute unknown type then error`() {
        val env = TestEnvAttribute()
        env.addSampleEntity()
        assertFailsWith<TypeNotFoundException> {
            env.createAttribute(attributeKey = AttributeKey("lastname"), type = typeRef("UnknownType"))
        }
    }

    @Test
    fun `update attribute with wrong model id throws ModelNotFoundException`() {
        val env = TestEnvAttribute()
        env.addSampleEntity()
        env.createAttribute()
        assertFailsWith<ModelNotFoundException> {
            env.dispatch(
                ModelAction.EntityAttribute_UpdateName(
                    modelRef = modelRefKey(ModelKey("unknown")),
                    entityRef = EntityRef.ByKey(EntityKey("unknownEntity")),
                    attributeRef = entityAttributeRef("unknownAttribute"),
                    value = null
                )
            )
        }

    }

    @Test
    fun `update attribute with wrong entity id throws ModelEntityNotFoundException`() {
        val env = TestEnvAttribute()
        env.addSampleEntity()
        env.createAttribute()
        assertFailsWith<EntityNotFoundException> {
            env.dispatch(
                ModelAction.EntityAttribute_UpdateName(
                    modelRef = env.sampleModelRef,
                    entityRef = EntityRef.ByKey(EntityKey("unknownEntity")),
                    attributeRef = entityAttributeRef("unknownAttribute"),
                    value = null
                )
            )
        }
    }


    @Test
    fun `update attribute with wrong attribute id throws ModelEntityNotFoundException`() {
        val env = TestEnvAttribute()
        env.addSampleEntity()
        env.createAttribute()
        assertFailsWith<EntityAttributeNotFoundException> {
            env.dispatch(
                ModelAction.EntityAttribute_UpdateName(
                    modelRef = env.sampleModelRef,
                    entityRef = env.sampleEntityRef,
                    attributeRef = entityAttributeRef("unknownAttribute"),
                    value = null
                )
            )
        }
    }


    @Test
    fun `update attribute id with duplicate id throws exception`() {
        val env = TestEnvAttribute()
        env.addSampleEntity()
        env.createAttribute(attributeKey = AttributeKey("lastname"))
        env.createAttribute(attributeKey = AttributeKey("firstname"))
        assertFailsWith<UpdateAttributeDuplicateKeyException> {
            // Rename firstname to lastname causes exception because lastname already exists
            env.updateAttribute(
                attributeRef = entityAttributeRef("firstname"),
                AttributeUpdateCmd.Key(AttributeKey("lastname"))
            )
        }
    }

    @Test
    fun `update attribute id with correct id works`() {
        val env = TestEnvAttribute()
        env.addSampleEntity()
        env.createAttribute(AttributeKey("lastname"))
        env.createAttribute(AttributeKey("firstname"))
        val reloaded = env.updateAttribute(
            entityAttributeRef("firstname"),
            AttributeUpdateCmd.Key(AttributeKey("nextname")),
            entityAttributeRef("nextname"),
        )
        assertEquals(AttributeKey("nextname"), reloaded.key)
    }

    @Test
    fun `update attribute key does not loose entity identifier attribute`() {
        val env = TestEnvAttribute()
        env.addSampleEntity()
        val e = env.query.findEntity(env.sampleModelRef, env.sampleEntityRef)
        val attrId = e.identifierAttributeId
        val newKey = AttributeKey("id_next")
        // Be careful to specify "reloadId" because the attribute's id changed
        env.updateAttribute(
            attributeRef = entityAttributeRef(attrId),
            command = AttributeUpdateCmd.Key(newKey),
            reloadId = entityAttributeRef(newKey)
        )
        val reloadedEntity = env.query.findEntity(env.sampleModelRef, env.sampleEntityRef)
        assertEquals(attrId, reloadedEntity.identifierAttributeId)


    }

    @Test
    fun `update attribute name is persisted`() {
        val env = TestEnvAttribute()
        env.addSampleEntity()
        val attr = env.createAttribute(name = null)
        val nextValue = LocalizedTextNotLocalized("New name")
        val reloaded = env.updateAttribute(entityAttributeRef(attr.key), AttributeUpdateCmd.Name(nextValue))
        assertEquals(nextValue, reloaded.name)
    }

    @Test
    fun `update attribute name to null stays null`() {
        val env = TestEnvAttribute()
        env.addSampleEntity()
        val attr = env.createAttribute(name = LocalizedTextNotLocalized("Name"))
        val reloaded = env.updateAttribute(entityAttributeRef(attr.key), AttributeUpdateCmd.Name(null))
        assertNull(reloaded.name)
    }


    @Test
    fun `update attribute description is persisted`() {
        val env = TestEnvAttribute()
        env.addSampleEntity()
        val attr = env.createAttribute(description = null)
        val nextValue = LocalizedMarkdownNotLocalized("New description")
        val reloaded =
            env.updateAttribute(entityAttributeRef(attr.key), AttributeUpdateCmd.Description(nextValue))
        assertEquals(nextValue, reloaded.description)
    }

    @Test
    fun `update attribute description to null stays null`() {
        val env = TestEnvAttribute()
        env.addSampleEntity()
        val attr = env.createAttribute(description = LocalizedMarkdownNotLocalized("New description"))
        val reloaded = env.updateAttribute(entityAttributeRef(attr.key), AttributeUpdateCmd.Description(null))
        assertNull(reloaded.description)
    }

    @Test
    fun `update attribute type is persisted`() {
        val env = TestEnvAttribute()
        env.addSampleEntity()
        val typeMarkdownKey = TypeKey("Markdown")
        env.dispatch(ModelAction.Type_Create(env.sampleModelRef, typeMarkdownKey, null, null))
        val typeMarkdownId =env.query.findType(env.sampleModelRef, typeRef(typeMarkdownKey)).id
        val type = env.query.findType(env.sampleModelRef, typeRef("String"))
        val attr = env.createAttribute(type = typeRef(type.id))


        val reloaded = env.updateAttribute(entityAttributeRef(attr.key), AttributeUpdateCmd.Type(typeRef(typeMarkdownKey)))
        assertEquals(typeMarkdownId, reloaded.typeId)
    }

    @Test
    fun `update attribute unknown type then error`() {
        val env = TestEnvAttribute()
        env.addSampleEntity()
        val type = env.query.findType(env.sampleModelRef, typeRef("String"))
        val attr = env.createAttribute(type = typeRef(type.id))
        assertThrows<TypeNotFoundException> {
            env.dispatch(
                ModelAction.EntityAttribute_UpdateType(
                    env.sampleModelRef,
                    env.sampleEntityRef,
                    entityAttributeRef(attr.key),
                    typeRef("String2")
                )
            )
        }
    }

    @Test
    fun `update attribute optional true to false is persisted`() {
        val env = TestEnvAttribute()
        env.addSampleEntity()
        val attr = env.createAttribute(optional = true)
        val nextValue = false
        val reloaded = env.updateAttribute(entityAttributeRef(attr.key), AttributeUpdateCmd.Optional(nextValue))
        assertEquals(nextValue, reloaded.optional)
    }

    @Test
    fun `update attribute optional false to true is persisted`() {
        val env = TestEnvAttribute()
        env.addSampleEntity()
        val attr = env.createAttribute(optional = false)
        val nextValue = true
        val reloaded = env.updateAttribute(entityAttributeRef(attr.key), AttributeUpdateCmd.Optional(nextValue))
        assertEquals(nextValue, reloaded.optional)
    }

    @Test
    fun `delete entity attribute in model then attribute removed`() {
        val env = TestEnvAttribute()
        env.addSampleEntity()
        env.createAttribute(attributeKey = AttributeKey("bk"))
        env.createAttribute(attributeKey = AttributeKey("firstname"))
        env.createAttribute(attributeKey = AttributeKey("lastname"))
        env.dispatch(
            ModelAction.EntityAttribute_Delete(
                env.sampleModelRef,
                env.sampleEntityRef,
                EntityAttributeRef.ByKey(AttributeKey("firstname"))
            )
        )

        assertNotNull(
            env.query.findEntityAttributeOptional(
                env.sampleModelRef,
                env.sampleEntityRef,
                entityAttributeRef("bk")
            )
        )
        assertNull(
            env.query.findEntityAttributeOptional(
                env.sampleModelRef,
                env.sampleEntityRef,
                entityAttributeRef("firstname")
            )
        )
        assertNotNull(
            env.query.findEntityAttributeOptional(
                env.sampleModelRef,
                env.sampleEntityRef,
                entityAttributeRef("lastname")
            )
        )


    }

    @Test
    fun `delete entity attribute used as identifier throws error`() {
        val env = TestEnvAttribute()
        env.addSampleEntity()
        env.createAttribute(attributeKey = AttributeKey("bk"))
        env.createAttribute(attributeKey = AttributeKey("firstname"))
        env.createAttribute(attributeKey = AttributeKey("lastname"))

        val reloaded = env.query.findEntity(env.sampleModelRef, env.sampleEntityRef)
        assertThrows<DeleteAttributeIdentifierException> {
            env.dispatch(
                ModelAction.EntityAttribute_Delete(
                    env.sampleModelRef,
                    env.sampleEntityRef,
                    EntityAttributeRef.ById(reloaded.identifierAttributeId)
                )
            )
        }
    }

    @Test
    fun `add and delete tag on entity attribute persists tag ids`() {
        val env = TestEnvAttribute()
        env.addSampleEntity()
        val attribute = env.createAttribute(attributeKey = AttributeKey("tagged"))
        val managedTag = createManagedTag(env.runtime, "g-ea", "t-ea")

        env.dispatch(
            ModelAction.EntityAttribute_AddTag(
                env.sampleModelRef,
                env.sampleEntityRef,
                EntityAttributeRef.ById(attribute.id),
                managedTag.ref
            )
        )
        val added = env.query.findEntityAttribute(env.sampleModelRef, env.sampleEntityRef, EntityAttributeRef.ById(attribute.id))
        assertEquals(listOf(managedTag.id), added.tags)

        env.dispatch(
            ModelAction.EntityAttribute_DeleteTag(
                env.sampleModelRef,
                env.sampleEntityRef,
                EntityAttributeRef.ById(attribute.id),
                managedTag.ref
            )
        )
        val deleted = env.query.findEntityAttribute(env.sampleModelRef, env.sampleEntityRef, EntityAttributeRef.ById(attribute.id))
        assertTrue(deleted.tags.isEmpty())
    }

    // ------------------------------------------------------------------------
    // Relationships
    // ------------------------------------------------------------------------

    @Test
    fun `add and delete tag on relationship persists tag ids`() {
        val env = TestEnvEntityUpdate()
        val relationshipKey = RelationshipKey("works-with")
        val relationshipRef = RelationshipRef.ByKey(relationshipKey)
        val managedTag = createManagedTag(env.runtime, "g-rel", "t-rel")

        env.dispatch(
            ModelAction.Relationship_Create(
                modelRef = env.modelRef,
                relationshipKey = relationshipKey,
                name = null,
                description = null,
                roleAKey = RelationshipRoleKey("a"),
                roleAEntityRef = env.primaryEntityRef,
                roleAName = null,
                roleACardinality = RelationshipCardinality.One,
                roleBKey = RelationshipRoleKey("b"),
                roleBEntityRef = env.secondaryEntityRef,
                roleBName = null,
                roleBCardinality = RelationshipCardinality.Many
            )
        )

        env.dispatch(ModelAction.Relationship_AddTag(env.modelRef, relationshipRef, managedTag.ref))
        assertEquals(listOf(managedTag.id), env.query.findModel(env.modelRef).findRelationship(relationshipRef).tags)

        env.dispatch(ModelAction.Relationship_DeleteTag(env.modelRef, relationshipRef, managedTag.ref))
        assertTrue(env.query.findModel(env.modelRef).findRelationship(relationshipRef).tags.isEmpty())
    }

    @Test
    fun `add and delete tag on relationship attribute persists tag ids`() {
        val env = TestEnvEntityUpdate()
        val relationshipKey = RelationshipKey("employs")
        val relationshipRef = RelationshipRef.ByKey(relationshipKey)
        val attributeRef = RelationshipAttributeRef.ByKey(AttributeKey("startDate"))
        val managedTag = createManagedTag(env.runtime, "g-ra", "t-ra")

        env.dispatch(
            ModelAction.Relationship_Create(
                modelRef = env.modelRef,
                relationshipKey = relationshipKey,
                name = null,
                description = null,
                roleAKey = RelationshipRoleKey("employer"),
                roleAEntityRef = env.primaryEntityRef,
                roleAName = null,
                roleACardinality = RelationshipCardinality.One,
                roleBKey = RelationshipRoleKey("employee"),
                roleBEntityRef = env.secondaryEntityRef,
                roleBName = null,
                roleBCardinality = RelationshipCardinality.Many
            )
        )
        env.dispatch(
            ModelAction.RelationshipAttribute_Create(
                modelRef = env.modelRef,
                relationshipRef = relationshipRef,
                attributeKey = AttributeKey("startDate"),
                type = typeRef("String"),
                optional = false,
                name = null,
                description = null
            )
        )

        env.dispatch(ModelAction.RelationshipAttribute_AddTag(env.modelRef, relationshipRef, attributeRef, managedTag.ref))
        val added = env.query.findModel(env.modelRef).findRelationshipAttributeOptional(relationshipRef, attributeRef)
        assertNotNull(added)
        assertEquals(listOf(managedTag.id), added.tags)

        env.dispatch(ModelAction.RelationshipAttribute_DeleteTag(env.modelRef, relationshipRef, attributeRef, managedTag.ref))
        val deleted = env.query.findModel(env.modelRef).findRelationshipAttributeOptional(relationshipRef, attributeRef)
        assertNotNull(deleted)
        assertTrue(deleted.tags.isEmpty())
    }

    // ------------------------------------------------------------------------
    // Validation process
    // ------------------------------------------------------------------------

    class TestEnvInvalidModel {
        val runtime = createEnv()
        val dispatch = runtime::dispatch
        val query = runtime.queries
        private val modelKey = ModelKey("test")
        val modelRef = modelRefKey(modelKey)

        val typeStringId = TypeId.generate()

        fun createInvalidModel() {
            val invalidModel = ModelInMemory.builder(
                key = modelKey,
                version = ModelVersion("0.0.1"),
            ) {
                name = null
                description = null
                types = mutableListOf(
                    ModelTypeInMemory(
                        id = typeStringId,
                        key = TypeKey("String"),
                        name = null,
                        description = null
                    )
                )
                addEntity(
                    key = EntityKey("Contact"),
                    // Error is here
                    identifierAttributeId = AttributeId.generate(),
                ) {
                    addAttribute(
                        AttributeInMemory(
                            id = AttributeId.generate(),
                            key = AttributeKey("id"),
                            typeId = typeStringId,
                            name = null,
                            description = null,
                            optional = false,
                            tags = emptyList()
                        )
                    )
                }

            }
            runtime.repositories.first().push(invalidModel)
        }
    }

    @Test
    fun `can not load model with errors`() {

        // This test only checks loading and basic behaviour of model operations

        // Each method that needs checking shall be checked independently
        // as some methods can effectively work on invalid models (for example to be able
        // to correct them)

        val env = TestEnvInvalidModel()
        env.createInvalidModel()

        // Getting a model that has error shall fail with invalid exception
        assertThrows<ModelInvalidException> { env.query.findModel(env.modelRef) }

        // Find all model ids shall not validate models, just give their ids
        assertDoesNotThrow { env.query.findAllModelIds() }

        // Creating or trying to modify something in invalid model shall throw error
        assertThrows<ModelInvalidException> {
            env.dispatch(ModelAction.Type_Create(env.modelRef, TypeKey("Markdown"), null, null))
        }

    }

}
