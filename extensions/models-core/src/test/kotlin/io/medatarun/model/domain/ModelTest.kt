package io.medatarun.model.domain

import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.ModelTestRuntime.Companion.createRuntime
import io.medatarun.model.infra.*
import io.medatarun.model.internal.ModelValidationImpl
import io.medatarun.model.ports.exposed.*
import io.medatarun.model.ports.needs.RepositoryRef
import io.medatarun.model.ports.needs.RepositoryRef.Companion.ref
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.net.URI
import kotlin.test.*

class ModelTest {

    @Test
    fun `can not instantiate storages without repositories`() {
        assertFailsWith(ModelStoragesCompositeNoRepositoryException::class) {
            ModelStoragesComposite(emptyList(), ModelValidationImpl())
        }
    }

    // ------------------------------------------------------------------------
    // Create models
    // ------------------------------------------------------------------------

    @Test
    fun `create model fail with ambiguous storage`() {
        val repo1 = ModelRepositoryInMemory("repo1")
        val repo2 = ModelRepositoryInMemory("repo2")
        val cmd = createRuntime(repositories = listOf(repo1, repo2)).cmd
        assertFailsWith(ModelStoragesAmbiguousRepositoryException::class) {
            cmd.dispatch(
                ModelCmd.CreateModel(
                    ModelKey("m1"),
                    LocalizedTextNotLocalized("M1"),
                    null,
                    ModelVersion("1.0.0")
                )
            )
        }
    }

    @Test
    fun `create model ok with one storage mode auto`() {
        val repo1 = ModelRepositoryInMemory("repo1")
        val cmd = createRuntime(repositories = listOf(repo1)).cmd
        val modelKey = ModelKey("m1")
        assertDoesNotThrow {
            cmd.dispatch(
                ModelCmd.CreateModel(
                    modelKey,
                    LocalizedTextNotLocalized("M1"),
                    null,
                    ModelVersion("1.0.0")
                )
            )
        }
        assertNotNull(repo1.findModelByKeyOptional(modelKey))
    }

    @Test
    fun `create model ok with multiple storages and specified storage`() {
        val repo1 = ModelRepositoryInMemory("repo1")
        val repo2 = ModelRepositoryInMemory("repo2")
        val runtime = createRuntime(repositories = listOf(repo1, repo2))
        val cmd = runtime.cmd
        val query = runtime.queries

        val modelKey = ModelKey("m1")
        cmd.dispatch(
            ModelCmd.CreateModel(
                modelKey,
                LocalizedTextNotLocalized("M1"),
                null,
                ModelVersion("1.0.0"),
                RepositoryRef.Id(repo2.repositoryId)
            )
        )
        assertDoesNotThrow { query.findModelByKey(modelKey) }

        assertNull(repo1.findModelByKeyOptional(modelKey))
        assertNotNull(repo2.findModelByKeyOptional(modelKey))

    }

    @Test
    fun `create model with name description and version when present`() {
        val runtime = createRuntime()
        val cmd: ModelCmds = runtime.cmd
        val query: ModelQueries = runtime.queries

        val modelKey = ModelKey("m-")
        val name = LocalizedTextNotLocalized("Model name")
        val description = LocalizedMarkdownNotLocalized("Model description")
        val version = ModelVersion("2.0.0")

        cmd.dispatch(ModelCmd.CreateModel(modelKey, name, description, version))

        val reloaded = query.findModelByKey(modelKey)
        assertEquals(name, reloaded.name)
        assertEquals(description, reloaded.description)
        assertEquals(version, reloaded.version)
    }

    @Test
    fun `create model keeps values without optional description`() {

        val runtime = createRuntime()
        val cmd: ModelCmds = runtime.cmd
        val query: ModelQueries = runtime.queries

        val modelKey = ModelKey("m")
        val name = LocalizedTextNotLocalized("Model without description")
        val version = ModelVersion("3.0.0")

        cmd.dispatch(ModelCmd.CreateModel(modelKey, name, null, version))

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
        val runtime = createRuntime()
        val cmd: ModelCmds = runtime.cmd
        val query: ModelQueries = runtime.queries

        val modelKey = ModelKey("m1")
        cmd.dispatch(
            ModelCmd.CreateModel(
                modelKey,
                LocalizedTextNotLocalized("Model name"),
                null,
                ModelVersion("2.0.0")
            )
        )
        val modelKeyWrong = modelRefKey("m2")
        assertFailsWith(ModelNotFoundException::class) {
            cmd.dispatch(
                ModelCmd.UpdateModelName(
                    modelKeyWrong,
                    LocalizedTextNotLocalized("other")
                )
            )
        }
        assertFailsWith(ModelNotFoundException::class) {
            cmd.dispatch(
                ModelCmd.UpdateModelDescription(
                    modelKeyWrong,
                    LocalizedMarkdownNotLocalized("other description")
                )
            )
        }
        assertFailsWith(ModelNotFoundException::class) {
            cmd.dispatch(
                ModelCmd.UpdateModelVersion(
                    modelKeyWrong,
                    ModelVersion("3.0.0")
                )
            )
        }
        cmd.dispatch(ModelCmd.UpdateModelName(modelRefKey(modelKey), LocalizedTextNotLocalized("Model name 2")))
        assertEquals(LocalizedTextNotLocalized("Model name 2"), query.findModelByKey(modelKey).name)
    }

    class TestEnvOneModel {
        val runtime = createRuntime()
        val cmd: ModelCmds = runtime.cmd
        val query: ModelQueries = runtime.queries
        private val modelKey = ModelKey("m1")
        val modelRef = modelRef(modelKey)

        init {
            cmd.dispatch(
                ModelCmd.CreateModel(
                    modelKey,
                    LocalizedTextNotLocalized("Model name"),
                    null,
                    ModelVersion("2.0.0")
                )
            )
            cmd.dispatch(
                ModelCmd.CreateType(
                    modelRef = modelRef,
                    initializer = ModelTypeInitializer(TypeKey("String"), null, null)
                )
            )
        }
    }


    @Test
    fun `updates on model name persists the name`() {
        val env = TestEnvOneModel()
        env.cmd.dispatch(ModelCmd.UpdateModelName(env.modelRef, LocalizedTextNotLocalized("Model name 2")))
        assertEquals(LocalizedTextNotLocalized("Model name 2"), env.query.findModel(env.modelRef).name)
    }

    @Test
    fun `updates on model description persists the description`() {
        val env = TestEnvOneModel()
        env.cmd.dispatch(
            ModelCmd.UpdateModelDescription(
                env.modelRef,
                LocalizedMarkdownNotLocalized("Model description 2")
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
        env.cmd.dispatch(
            ModelCmd.UpdateModelDescription(
                env.modelRef,
                LocalizedMarkdownNotLocalized("Model description 2")
            )
        )
        env.cmd.dispatch(ModelCmd.UpdateModelDescription(env.modelRef, null))
        assertNull(env.query.findModel(env.modelRef).description)
    }

    @Test
    fun `updates on model version persists the version`() {
        val env = TestEnvOneModel()
        env.cmd.dispatch(ModelCmd.UpdateModelVersion(env.modelRef, ModelVersion("4.5.6")))
        assertEquals(ModelVersion("4.5.6"), env.query.findModel(env.modelRef).version)
    }

    @Test
    fun `update documentation home with value then updated`() {
        val env = TestEnvOneModel()
        val url = URI("https://some.url/index.html").toURL()
        env.cmd.dispatch(ModelCmd.UpdateModelDocumentationHome(env.modelRef, url))
        assertEquals(url, env.query.findModel(env.modelRef).documentationHome)
    }

    @Test
    fun `update documentation home with null then updated to null`() {
        val env = TestEnvOneModel()
        val url = URI("https://some.url/index.html").toURL()
        env.cmd.dispatch(ModelCmd.UpdateModelDocumentationHome(env.modelRef, url))
        env.cmd.dispatch(ModelCmd.UpdateModelDocumentationHome(env.modelRef, null))
        assertNull(env.query.findModel(env.modelRef).documentationHome)
    }

    // ------------------------------------------------------------------------
    // Delete models
    // ------------------------------------------------------------------------

    @Test
    fun `delete model fails if model Id not found in any storage`() {
        val repo1 = ModelRepositoryInMemory("repo1")
        val repo2 = ModelRepositoryInMemory("repo2")
        val runtime = createRuntime(listOf(repo1, repo2))
        val cmd: ModelCmds = runtime.cmd
        cmd.dispatch(
            ModelCmd.CreateModel(
                modelKey = ModelKey("m-to-delete-repo-1"),
                name = LocalizedTextNotLocalized("Model to delete"),
                description = null,
                version = ModelVersion("0.0.1"),
                repositoryRef = repo2.repositoryId.ref()
            )
        )
        cmd.dispatch(
            ModelCmd.CreateModel(
                modelKey = ModelKey("m-to-delete-repo-2"),
                name = LocalizedTextNotLocalized("Model to delete 2 on repo 2"),
                description = null,
                version = ModelVersion("0.0.1"),
                repositoryRef = repo2.repositoryId.ref()
            )
        )
        assertThrows<ModelNotFoundException> {
            cmd.dispatch(ModelCmd.DeleteModel(modelRefKey("m-to-delete-repo-3")))
        }
    }

    @Test
    fun `delete model removes it from storage`() {
        val repo1 = ModelRepositoryInMemory("repo1")
        val repo2 = ModelRepositoryInMemory("repo2")
        val runtime = createRuntime(listOf(repo1, repo2))
        val cmd: ModelCmds = runtime.cmd
        val query: ModelQueries = runtime.queries

        cmd.dispatch(
            ModelCmd.CreateModel(
                modelKey = ModelKey("m-to-delete-repo-1"),
                name = LocalizedTextNotLocalized("Model to delete"),
                description = null,
                version = ModelVersion("0.0.1"),
                repositoryRef = repo1.repositoryId.ref()
            )
        )
        cmd.dispatch(
            ModelCmd.CreateModel(
                modelKey = ModelKey("m-to-preserve-repo-1"),
                name = LocalizedTextNotLocalized("Model to preserve"),
                description = null,
                version = ModelVersion("0.1.0"),
                repositoryRef = repo1.repositoryId.ref()
            )
        )
        cmd.dispatch(
            ModelCmd.CreateModel(
                modelKey = ModelKey("m-to-delete-repo-2"),
                name = LocalizedTextNotLocalized("Model to delete 2 on repo 2"),
                description = null,
                version = ModelVersion("0.0.1"),
                repositoryRef = repo2.repositoryId.ref()
            )
        )
        cmd.dispatch(
            ModelCmd.CreateModel(
                modelKey = ModelKey("m-to-preserve-repo-2"),
                name = LocalizedTextNotLocalized("Model to preserve on repo 2"),
                description = null,
                version = ModelVersion("0.1.0"),
                repositoryRef = repo2.repositoryId.ref()
            )
        )

        cmd.dispatch(ModelCmd.DeleteModel(modelRef("m-to-delete-repo-1")))
        assertNull(repo1.findModelOptional(modelRef("m-to-delete-repo-1")))
        assertNull(query.findModelOptional(modelRef("m-to-delete-repo-1")))

        assertNotNull(query.findModelOptional(modelRef("m-to-preserve-repo-1")))
        assertNotNull(query.findModelOptional(modelRef("m-to-delete-repo-2")))
        assertNotNull(query.findModelOptional(modelRef("m-to-preserve-repo-2")))

        cmd.dispatch(ModelCmd.DeleteModel(modelRefKey("m-to-delete-repo-2")))
        assertNull(repo1.findModelOptional(modelRef("m-to-delete-repo-2")))
        assertNotNull(query.findModelOptional(modelRef("m-to-preserve-repo-1")))
        assertNotNull(query.findModelOptional(modelRef("m-to-preserve-repo-2")))

        cmd.dispatch(ModelCmd.DeleteModel(modelRef("m-to-preserve-repo-1")))
        assertNull(repo2.findModelOptional(modelRef("m-to-delete-repo-2")))
        assertNotNull(query.findModelOptional(modelRef("m-to-preserve-repo-2")))

        cmd.dispatch(ModelCmd.DeleteModel(modelRefKey("m-to-preserve-repo-2")))
        assertNull(repo2.findModelOptional(modelRef("m-to-delete-repo-2")))

    }

    // ------------------------------------------------------------------------
    // Types
    // ------------------------------------------------------------------------

    class TestEnvTypes {
        val runtime = createRuntime()
        val cmd: ModelCmds = runtime.cmd
        val query: ModelQueries = runtime.queries
        private val modelKey = ModelKey("m1")
        val modelRef = modelRefKey(ModelKey("m1"))

        init {
            cmd.dispatch(
                ModelCmd.CreateModel(
                    modelKey,
                    LocalizedTextNotLocalized("Model name"),
                    null,
                    ModelVersion("2.0.0")
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
        env.cmd.dispatch(
            ModelCmd.CreateType(
                env.modelRef,
                ModelTypeInitializer(
                    TypeKey("String"),
                    LocalizedTextNotLocalized("Simple string"),
                    LocalizedMarkdownNotLocalized("Simple string description")
                )
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
        env.cmd.dispatch(ModelCmd.CreateType(env.modelRef, ModelTypeInitializer(TypeKey("String"), null, null)))
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
            env.cmd.dispatch(
                ModelCmd.CreateType(
                    modelRefKey("unknown"),
                    ModelTypeInitializer(TypeKey("String"), null, null)
                )
            )
        }
    }

    @Test
    fun `create type with duplicate name throws DuplicateTypeException`() {
        val env = TestEnvTypes()
        env.cmd.dispatch(ModelCmd.CreateType(env.modelRef, ModelTypeInitializer(TypeKey("String"), null, null)))
        assertThrows<TypeCreateDuplicateException> {
            env.cmd.dispatch(
                ModelCmd.CreateType(
                    env.modelRef,
                    ModelTypeInitializer(TypeKey("String"), null, null)
                )
            )
        }
    }

    @Test
    fun `update type name `() {
        val env = TestEnvTypes()
        val typeKey = TypeKey("String")
        val typeRef = TypeRef.ByKey(typeKey)
        env.cmd.dispatch(ModelCmd.CreateType(env.modelRef, ModelTypeInitializer(typeKey, null, null)))
        env.cmd.dispatch(
            ModelCmd.UpdateType(
                env.modelRef,
                typeRef,
                ModelTypeUpdateCmd.Name(LocalizedTextNotLocalized("This is a string"))
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
        env.cmd.dispatch(ModelCmd.CreateType(env.modelRef, ModelTypeInitializer(typeKey, null, null)))
        env.cmd.dispatch(ModelCmd.UpdateType(env.modelRef, typeRef, ModelTypeUpdateCmd.Name(null)))
        val t = env.model.findTypeOptional(typeRef)
        assertNotNull(t)
        assertNull(t.name)
    }

    @Test
    fun `update type description`() {
        val env = TestEnvTypes()
        val typeKey = TypeKey("String")
        val typeRef = TypeRef.ByKey(typeKey)
        env.cmd.dispatch(ModelCmd.CreateType(env.modelRef, ModelTypeInitializer(typeKey, null, null)))
        env.cmd.dispatch(
            ModelCmd.UpdateType(
                env.modelRef,
                typeRef,
                ModelTypeUpdateCmd.Description(LocalizedMarkdownNotLocalized("This is a string"))
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
        env.cmd.dispatch(ModelCmd.CreateType(env.modelRef, ModelTypeInitializer(typeKey, null, null)))
        env.cmd.dispatch(ModelCmd.UpdateType(env.modelRef, typeRef, ModelTypeUpdateCmd.Description(null)))
        val t = env.model.findTypeOptional(typeRef)
        assertNotNull(t)
        assertNull(t.description)
    }

    @Test
    fun `update type with model not found`() {
        val env = TestEnvTypes()
        val typeKey = TypeKey("String")
        val typeRef = TypeRef.ByKey(typeKey)
        env.cmd.dispatch(ModelCmd.CreateType(env.modelRef, ModelTypeInitializer(typeKey, null, null)))
        assertThrows<ModelNotFoundException> {
            env.cmd.dispatch(
                ModelCmd.UpdateType(
                    modelRefKey("unknown"),
                    typeRef,
                    ModelTypeUpdateCmd.Description(null)
                )
            )
        }
    }

    @Test
    fun `update type with type not found`() {
        val env = TestEnvTypes()
        val typeKey = TypeKey("String")
        env.cmd.dispatch(ModelCmd.CreateType(env.modelRef, ModelTypeInitializer(typeKey, null, null)))
        assertThrows<TypeNotFoundException> {
            env.cmd.dispatch(
                ModelCmd.UpdateType(
                    env.modelRef,
                    TypeRef.ByKey(TypeKey("String2")),
                    ModelTypeUpdateCmd.Description(null)
                )
            )
        }
    }

    @Test
    fun `delete type model not found`() {
        val env = TestEnvTypes()
        val typeKey = TypeKey("String")
        val typeRef = TypeRef.ByKey(typeKey)
        env.cmd.dispatch(ModelCmd.CreateType(env.modelRef, ModelTypeInitializer(typeKey, null, null)))
        assertThrows<ModelNotFoundException> {
            env.cmd.dispatch(ModelCmd.DeleteType(modelRefKey("unknown"), typeRef))
        }
    }

    @Test
    fun `delete type type not found`() {
        val env = TestEnvTypes()
        val typeKey = TypeKey("String")
        val typeKeyWrong = TypeKey("String2")
        val typeRefWrong = TypeRef.ByKey(typeKeyWrong)
        env.cmd.dispatch(ModelCmd.CreateType(env.modelRef, ModelTypeInitializer(typeKey, null, null)))
        assertThrows<TypeNotFoundException> {
            env.cmd.dispatch(ModelCmd.DeleteType(env.modelRef, typeRefWrong))
        }
    }

    @Test
    fun `delete type used in attributes then error`() {
        val env = TestEnvTypes()
        env.cmd.dispatch(ModelCmd.CreateType(env.modelRef, ModelTypeInitializer(TypeKey("String"), null, null)))
        env.cmd.dispatch(ModelCmd.CreateType(env.modelRef, ModelTypeInitializer(TypeKey("Markdown"), null, null)))
        env.cmd.dispatch(ModelCmd.CreateType(env.modelRef, ModelTypeInitializer(TypeKey("Int"), null, null)))
        val entityKey = EntityKey("contact")
        val entityRef = EntityRef.ByKey(entityKey)
        env.cmd.dispatch(
            ModelCmd.CreateEntity(
                env.modelRef, EntityDefInitializer(
                    entityKey = entityKey, name = null, description = null,
                    documentationHome = null,
                    identityAttribute = AttributeDefIdentityInitializer(
                        attributeKey = AttributeKey("name"),
                        type = typeRef("String"),
                        name = null,
                        description = null
                    )
                )
            )
        )
        env.cmd.dispatch(
            ModelCmd.CreateEntityAttribute(
                modelRef = env.modelRef,
                entityRef = entityRef,
                attributeInitializer = AttributeDefInitializer(
                    attributeKey = AttributeKey("infos"),
                    type = TypeRef.ByKey(TypeKey("Markdown")),
                    optional = false, name = null, description = null
                )
            )
        )
        assertThrows<ModelTypeDeleteUsedException> {
            env.cmd.dispatch(ModelCmd.DeleteType(env.modelRef, typeRef("String")))
        }
        assertThrows<ModelTypeDeleteUsedException> {
            env.cmd.dispatch(ModelCmd.DeleteType(env.modelRef, TypeRef.ByKey(TypeKey("Markdown"))))
        }
        env.cmd.dispatch(ModelCmd.DeleteType(env.modelRef, TypeRef.ByKey(TypeKey("Int"))))

    }

    @Test
    fun `delete type success`() {
        val env = TestEnvTypes()
        env.cmd.dispatch(ModelCmd.CreateType(env.modelRef, ModelTypeInitializer(TypeKey("String"), null, null)))
        env.cmd.dispatch(ModelCmd.CreateType(env.modelRef, ModelTypeInitializer(TypeKey("Markdown"), null, null)))
        env.cmd.dispatch(ModelCmd.CreateType(env.modelRef, ModelTypeInitializer(TypeKey("Int"), null, null)))
        env.cmd.dispatch(ModelCmd.DeleteType(env.modelRef, TypeRef.ByKey(TypeKey("Int"))))
        assertNull(env.model.findTypeOptional(TypeRef.ByKey(TypeKey("Int"))))
        assertNotNull(env.model.findTypeOptional(typeRef("String")))
        assertNotNull(env.model.findTypeOptional(TypeRef.ByKey(TypeKey("Markdown"))))

    }

    // ------------------------------------------------------------------------
    // Entities
    // ------------------------------------------------------------------------

    @Test
    fun `create entity then id name and description shall persist`() {
        val env = TestEnvOneModel()
        val entityId = EntityKey("entity")
        val entityRef = EntityRef.ByKey(entityId)
        val name = LocalizedTextNotLocalized("Order")
        val description = LocalizedMarkdownNotLocalized("Order description")

        env.cmd.dispatch(
            ModelCmd.CreateEntity(
                env.modelRef, EntityDefInitializer.build(
                    entityKey = entityId,
                    identityAttribute = AttributeDefIdentityInitializer(
                        attributeKey = AttributeKey("id"),
                        type = typeRef("String"),
                        name = LocalizedTextNotLocalized("Identifier"),
                        description = LocalizedMarkdownNotLocalized("Identifier description")
                    )
                ) {
                    this.name = name
                    this.description = description

                }
            )
        )

        val reloaded = env.query.findEntity(env.modelRef, entityRef)
        assertEquals(entityId, reloaded.key)
        assertEquals(name, reloaded.name)
        assertEquals(description, reloaded.description)
        assertEquals(1, reloaded.attributes.size)
        val attrId = reloaded.attributes[0]
        assertEquals(AttributeKey("id"), attrId.key)
        assertEquals("Identifier", attrId.name?.name)
        assertEquals("Identifier description", attrId.description?.name)
    }

    @Test
    fun `create entity with null name then name shall be null`() {
        val env = TestEnvOneModel()
        val entityKey = EntityKey("entity-null-name")
        val entityRef = EntityRef.ByKey(entityKey)
        val description = LocalizedMarkdownNotLocalized("Entity without name")

        env.cmd.dispatch(
            ModelCmd.CreateEntity(
                env.modelRef, EntityDefInitializer.build(
                    entityKey = entityKey,
                    identityAttribute = AttributeDefIdentityInitializer.build(
                        attributeKey = AttributeKey("id"),
                        type = typeRef("String")
                    )
                ) {
                    this.description = description
                }
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

        env.cmd.dispatch(
            ModelCmd.CreateEntity(
                env.modelRef, EntityDefInitializer.build(
                    entityKey = entityKey,
                    identityAttribute = AttributeDefIdentityInitializer.build(
                        attributeKey = AttributeKey("String"),
                        type = typeRef("String")
                    )
                ) {
                    this.name = name
                }
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

        env.cmd.dispatch(
            ModelCmd.CreateEntity(
                env.modelRef, EntityDefInitializer.build(
                    entityKey = entityKey,
                    identityAttribute = AttributeDefIdentityInitializer.build(
                        attributeKey = AttributeKey("id"),
                        type = typeRef("String"),
                    ),
                ) {
                    this.description = description
                }
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
        env.cmd.dispatch(
            ModelCmd.CreateEntity(
                env.modelRef, EntityDefInitializer.build(
                    entityKey, AttributeDefIdentityInitializer.build(
                        AttributeKey("id"),
                        typeRef("String")
                    )
                )
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
        env.cmd.dispatch(
            ModelCmd.CreateEntity(
                env.modelRef, EntityDefInitializer.build(
                    entityKey, AttributeDefIdentityInitializer.build(
                        AttributeKey("id"),
                        typeRef("String")
                    )
                ) {
                    documentationHome = url
                }
            )
        )
        assertEquals(url, env.query.findEntity(env.modelRef, entityRef).documentationHome)
    }

    class TestEnvEntityUpdate {
        val runtime = createRuntime()
        val cmd: ModelCmds = runtime.cmd
        val query: ModelQueries = runtime.queries
        private val modelKey = ModelKey("model-entity-update")
        val modelRef = modelRefKey(ModelKey("model-entity-update"))
        val primaryEntityKey = EntityKey("entity-primary")
        val primaryEntityRef = EntityRef.ByKey(primaryEntityKey)
        val secondaryEntityKey = EntityKey("entity-secondary")
        val secondaryEntityRef = EntityRef.ByKey(secondaryEntityKey)

        init {
            cmd.dispatch(
                ModelCmd.CreateModel(
                    modelKey,
                    LocalizedTextNotLocalized("Model entity update"),
                    null,
                    ModelVersion("1.0.0")
                )
            )
            cmd.dispatch(ModelCmd.CreateType(modelRef, ModelTypeInitializer(TypeKey("String"), null, null)))
            cmd.dispatch(
                ModelCmd.CreateEntity(
                    modelRef,
                    EntityDefInitializer(
                        entityKey = primaryEntityKey,
                        name = LocalizedTextNotLocalized("Entity primary"),
                        description = LocalizedMarkdownNotLocalized("Entity primary description"),
                        documentationHome = null,
                        identityAttribute = AttributeDefIdentityInitializer(
                            attributeKey = AttributeKey("id"),
                            type = typeRef("String"),
                            name = null,
                            description = null
                        )
                    )
                )
            )
            cmd.dispatch(
                ModelCmd.CreateEntity(
                    modelRef,
                    EntityDefInitializer(
                        secondaryEntityKey,
                        LocalizedTextNotLocalized("Entity secondary"),
                        LocalizedMarkdownNotLocalized("Entity secondary description"),
                        documentationHome = null,
                        identityAttribute = AttributeDefIdentityInitializer(
                            attributeKey = AttributeKey("id"),
                            type = typeRef("String"),
                            name = null,
                            description = null
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `update entity with wrong model id throws ModelNotFoundException`() {
        val env = TestEnvEntityUpdate()
        val wrongModelKey = modelRefKey("unknown-model")

        assertFailsWith<ModelNotFoundException> {
            env.cmd.dispatch(
                ModelCmd.UpdateEntity(
                    wrongModelKey,
                    env.primaryEntityRef,
                    EntityDefUpdateCmd.Name(LocalizedTextNotLocalized("Updated name"))
                )
            )
        }
    }

    @Test
    fun `update entity with wrong entity id throws EntityDefNotFoundException`() {
        val env = TestEnvEntityUpdate()
        val wrongEntityId = EntityKey("unknown-entity")
        val wrongEntityRef = EntityRef.ByKey(wrongEntityId)

        assertFailsWith<EntityNotFoundException> {
            env.cmd.dispatch(
                ModelCmd.UpdateEntity(
                    env.modelRef,
                    wrongEntityRef,
                    EntityDefUpdateCmd.Name(LocalizedTextNotLocalized("Updated name"))
                )
            )
        }
    }

    @Test
    fun `update entity id with duplicate id throws exception`() {
        val env = TestEnvEntityUpdate()
        val duplicateId = env.secondaryEntityKey

        assertFailsWith<UpdateEntityDefIdDuplicateIdException> {
            env.cmd.dispatch(
                ModelCmd.UpdateEntity(
                    env.modelRef,
                    env.primaryEntityRef,
                    EntityDefUpdateCmd.Key(duplicateId)
                )
            )
        }
    }

    @Test
    fun `update entity id with correct id ok`() {
        val env = TestEnvEntityUpdate()
        val newId = EntityKey("entity-renamed")

        env.cmd.dispatch(
            ModelCmd.UpdateEntity(
                env.modelRef,
                env.primaryEntityRef,
                EntityDefUpdateCmd.Key(newId)
            )
        )

        val reloaded = env.query.findModel(env.modelRef)
        assertNull(reloaded.findEntityOptional(env.primaryEntityKey))
        assertNotNull(reloaded.findEntityOptional(newId))
    }

    @Test
    fun `update entity name not null persisted`() {
        val env = TestEnvEntityUpdate()
        val newName = LocalizedTextNotLocalized("Entity primary updated")

        env.cmd.dispatch(
            ModelCmd.UpdateEntity(
                env.modelRef,
                env.primaryEntityRef,
                EntityDefUpdateCmd.Name(newName)
            )
        )

        val reloaded = env.query.findEntity(env.modelRef, env.primaryEntityRef)
        assertEquals(newName, reloaded.name)
    }

    @Test
    fun `update entity name null then name is null`() {
        val env = TestEnvEntityUpdate()

        env.cmd.dispatch(
            ModelCmd.UpdateEntity(
                env.modelRef,
                env.primaryEntityRef,
                EntityDefUpdateCmd.Name(null)
            )
        )

        val reloaded = env.query.findEntity(env.modelRef, env.primaryEntityRef)
        assertNull(reloaded.name)
    }

    @Test
    fun `update entity description not null persisted`() {
        val env = TestEnvEntityUpdate()
        val newDescription = LocalizedMarkdownNotLocalized("Primary entity updated description")

        env.cmd.dispatch(
            ModelCmd.UpdateEntity(
                env.modelRef,
                env.primaryEntityRef,
                EntityDefUpdateCmd.Description(newDescription)
            )
        )

        val reloaded = env.query.findEntity(env.modelRef, env.primaryEntityRef)
        assertEquals(newDescription, reloaded.description)
    }

    @Test
    fun `update entity description with null then description is null`() {
        val env = TestEnvEntityUpdate()

        env.cmd.dispatch(
            ModelCmd.UpdateEntity(
                env.modelRef,
                env.primaryEntityRef,
                EntityDefUpdateCmd.Description(null)
            )
        )

        val reloaded = env.query.findEntity(env.modelRef, env.primaryEntityRef)
        assertNull(reloaded.description)
    }

    @Test
    fun `update entity documentation home not null`() {
        val env = TestEnvEntityUpdate()
        val url = URI("http://localhost").toURL()
        env.cmd.dispatch(
            ModelCmd.UpdateEntity(env.modelRef, env.primaryEntityRef, EntityDefUpdateCmd.DocumentationHome(url))
        )
        val reloaded = env.query.findEntity(env.modelRef, env.primaryEntityRef)
        assertEquals(url, reloaded.documentationHome)
    }

    @Test
    fun `update entity documentation home to null`() {
        val env = TestEnvEntityUpdate()
        val url = URI("http://localhost").toURL()
        env.cmd.dispatch(
            ModelCmd.UpdateEntity(env.modelRef, env.primaryEntityRef, EntityDefUpdateCmd.DocumentationHome(url))
        )
        env.cmd.dispatch(
            ModelCmd.UpdateEntity(env.modelRef, env.primaryEntityRef, EntityDefUpdateCmd.DocumentationHome(null))
        )
        val reloaded = env.query.findEntity(env.modelRef, env.primaryEntityRef)
        assertNull(reloaded.documentationHome)
    }


    @Test
    fun `delete entity in model then entity removed`() {
        val env = TestEnvOneModel()
        val entityId = EntityKey("entity-to-delete")
        val entityRef = EntityRef.ByKey(entityId)

        env.cmd.dispatch(
            ModelCmd.CreateEntity(
                env.modelRef, EntityDefInitializer(
                    entityKey = entityId,
                    name = LocalizedTextNotLocalized("To delete"),
                    description = null,
                    documentationHome = null,
                    identityAttribute = AttributeDefIdentityInitializer(
                        attributeKey = AttributeKey("id"),
                        type = typeRef("String"),
                        name = null,
                        description = null
                    )
                )
            )
        )
        env.cmd.dispatch(ModelCmd.DeleteEntity(env.modelRef, entityRef))

        val reloaded = env.query.findModel(env.modelRef)
        assertNull(reloaded.findEntityOptional(entityId))
    }

    @Test
    fun `delete entity with same id in two models then only entity in the specified model is removed`() {
        val runtime = createRuntime()
        val cmd: ModelCmds = runtime.cmd
        val query: ModelQueries = runtime.queries

        val modelKey1 = ModelKey("model-1")
        val modelRef1 = modelRefKey(modelKey1)
        val modelKey2 = ModelKey("model-2")
        val modelRef2 = modelRefKey(modelKey2)
        val entityKey = EntityKey("shared-entity")
        val entityRef = EntityRef.ByKey(entityKey)

        cmd.dispatch(
            ModelCmd.CreateModel(
                modelKey1,
                LocalizedTextNotLocalized("Model 1"),
                null,
                ModelVersion("1.0.0")
            )
        )
        cmd.dispatch(ModelCmd.CreateType(modelRef1, ModelTypeInitializer(TypeKey("String"), null, null)))
        cmd.dispatch(
            ModelCmd.CreateModel(
                modelKey2,
                LocalizedTextNotLocalized("Model 2"),
                null,
                ModelVersion("1.0.0")
            )
        )
        cmd.dispatch(ModelCmd.CreateType(modelRef2, ModelTypeInitializer(TypeKey("String"), null, null)))
        cmd.dispatch(
            ModelCmd.CreateEntity(
                modelRef1, EntityDefInitializer(
                    entityKey = entityKey, name = LocalizedTextNotLocalized("Entity"), description = null,
                    documentationHome = null,
                    identityAttribute = AttributeDefIdentityInitializer(
                        attributeKey = AttributeKey("id"),
                        type = typeRef("String"),
                        name = null,
                        description = null
                    )
                )
            )
        )
        cmd.dispatch(
            ModelCmd.CreateEntity(
                modelRef2, EntityDefInitializer(
                    entityKey = entityKey, name = LocalizedTextNotLocalized("Entity"), description = null,
                    documentationHome = null,
                    identityAttribute = AttributeDefIdentityInitializer(
                        attributeKey = AttributeKey("id"),
                        type = typeRef("String"),
                        name = null,
                        description = null
                    )
                )
            )
        )

        cmd.dispatch(ModelCmd.DeleteEntity(modelRef1, entityRef))

        val reloadedModel1 = query.findModel(modelRef1)
        val reloadedModel2 = query.findModel(modelRef2)

        assertNull(reloadedModel1.findEntityOptional(entityKey))
        assertNotNull(reloadedModel2.findEntityOptional(entityKey))
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Attributes
    // -----------------------------------------------------------------------------------------------------------------

    class TestEnvAttribute {
        val runtime = createRuntime()
        val cmd: ModelCmds = runtime.cmd
        val query: ModelQueries = runtime.queries
        private val sampleModelKey = ModelKey("model-1")
        val sampleModelRef = modelRefKey(sampleModelKey)
        val sampleEntityKey = EntityKey("Entity1")
        val sampleEntityRef = EntityRef.ByKey(sampleEntityKey)

        init {
            cmd.dispatch(
                ModelCmd.CreateModel(
                    sampleModelKey,
                    LocalizedTextNotLocalized("Model 1"),
                    null,
                    ModelVersion("1.0.0"),
                )
            )
            cmd.dispatch(ModelCmd.CreateType(sampleModelRef, ModelTypeInitializer(TypeKey("String"), null, null)))
        }

        fun addSampleEntityDef() {
            cmd.dispatch(
                ModelCmd.CreateEntity(
                    sampleModelRef,
                    EntityDefInitializer(
                        entityKey = sampleEntityKey, name = null, description = null,
                        documentationHome = null,
                        identityAttribute = AttributeDefIdentityInitializer(
                            attributeKey = AttributeKey("id"),
                            type = typeRef("String"),
                            name = null,
                            description = null
                        )
                    )
                )
            )
        }

        fun createAttributeDef(
            attributeKey: AttributeKey = AttributeKey("myattribute"),
            type: TypeRef = typeRef("String"),
            optional: Boolean = false,
            name: LocalizedText? = null,
            description: LocalizedMarkdown? = null
        ): AttributeDef {

            cmd.dispatch(
                ModelCmd.CreateEntityAttribute(
                    modelRef = sampleModelRef,
                    entityRef = sampleEntityRef,
                    attributeInitializer = AttributeDefInitializer(
                        attributeKey = attributeKey,
                        type = type,
                        optional = optional,
                        name = name,
                        description = description,
                    )
                )
            )
            val model = query.findModel(sampleModelRef)
            val attributeRef = EntityAttributeRef.ByKey(attributeKey)
            val reloaded = model.findEntityAttributeOptional(sampleEntityRef, attributeRef)
                ?: throw EntityAttributeNotFoundException(sampleModelRef, sampleEntityRef, attributeRef)
            return reloaded
        }

        fun updateAttributeDef(
            attributeRef: EntityAttributeRef = EntityAttributeRef.ByKey(AttributeKey("myattribute")),
            command: AttributeDefUpdateCmd,
            reloadId: EntityAttributeRef? = null
        ): AttributeDef {
            cmd.dispatch(
                ModelCmd.UpdateEntityAttribute(
                    sampleModelRef,
                    sampleEntityRef,
                    attributeRef,
                    command
                )
            )
            val reloaded = query.findEntityAttribute(sampleModelRef, sampleEntityRef, reloadId ?: attributeRef)
            return reloaded
        }

    }

    @Test
    fun `create attribute then id name and description shall persist`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        val reloaded = env.createAttributeDef(
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
        env.addSampleEntityDef()
        val reloaded = env.createAttributeDef(name = null)
        assertNull(reloaded.name)
    }

    @Test
    fun `create attribute with null description then description shall be null`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        val reloaded = env.createAttributeDef(description = null)
        assertNull(reloaded.description)
    }

    @Test
    fun `create attribute with optional true description then optional is true`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        val reloaded = env.createAttributeDef(optional = true)
        assertTrue(reloaded.optional)
    }

    @Test
    fun `create attribute with type boolean then type found`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        val typeKey = TypeKey("Boolean")
        env.cmd.dispatch(
            ModelCmd.CreateType(
                env.sampleModelRef,
                ModelTypeInitializer(typeKey, null, null)
            )
        )
        val type = env.query.findType(env.sampleModelRef, TypeRef.ByKey(typeKey))
        val reloaded = env.createAttributeDef(type = typeRef("Boolean"))
        assertEquals(type.id, reloaded.typeId)
    }


    @Test
    fun `create attribute with duplicate id then error`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        env.createAttributeDef(attributeKey = AttributeKey("lastname"))
        assertFailsWith<CreateAttributeDefDuplicateIdException> {
            env.createAttributeDef(attributeKey = AttributeKey("lastname"))
        }
    }

    @Test
    fun `create attribute unknown type then error`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        assertFailsWith<TypeNotFoundException> {
            env.createAttributeDef(attributeKey = AttributeKey("lastname"), type = typeRef("UnknownType"))
        }
    }

    @Test
    fun `update attribute with wrong model id throws ModelNotFoundException`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        env.createAttributeDef()
        assertFailsWith<ModelNotFoundException> {
            env.cmd.dispatch(
                ModelCmd.UpdateEntityAttribute(
                    modelRef = modelRefKey(ModelKey("unknown")),
                    entityRef = EntityRef.ByKey(EntityKey("unknownEntity")),
                    attributeRef = entityAttributeRef("unknownAttribute"),
                    cmd = AttributeDefUpdateCmd.Name(null)
                )
            )
        }

    }

    @Test
    fun `update attribute with wrong entity id throws ModelEntityNotFoundException`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        env.createAttributeDef()
        assertFailsWith<EntityNotFoundException> {
            env.cmd.dispatch(
                ModelCmd.UpdateEntityAttribute(
                    modelRef = env.sampleModelRef,
                    entityRef = EntityRef.ByKey(EntityKey("unknownEntity")),
                    attributeRef = entityAttributeRef("unknownAttribute"),
                    cmd = AttributeDefUpdateCmd.Name(null)
                )
            )
        }
    }


    @Test
    fun `update attribute with wrong attribute id throws ModelEntityNotFoundException`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        env.createAttributeDef()
        assertFailsWith<EntityAttributeNotFoundException> {
            env.cmd.dispatch(
                ModelCmd.UpdateEntityAttribute(
                    modelRef = env.sampleModelRef,
                    entityRef = env.sampleEntityRef,
                    attributeRef = entityAttributeRef("unknownAttribute"),
                    cmd = AttributeDefUpdateCmd.Name(null)
                )
            )
        }
    }


    @Test
    fun `update attribute id with duplicate id throws exception`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        env.createAttributeDef(attributeKey = AttributeKey("lastname"))
        env.createAttributeDef(attributeKey = AttributeKey("firstname"))
        assertFailsWith<UpdateAttributeDuplicateKeyException> {
            // Rename firstname to lastname causes exception because lastname already exists
            env.updateAttributeDef(
                attributeRef = entityAttributeRef("firstname"),
                AttributeDefUpdateCmd.Key(AttributeKey("lastname"))
            )
        }
    }

    @Test
    fun `update attribute id with correct id works`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        env.createAttributeDef(AttributeKey("lastname"))
        env.createAttributeDef(AttributeKey("firstname"))
        val reloaded = env.updateAttributeDef(
            entityAttributeRef("firstname"),
            AttributeDefUpdateCmd.Key(AttributeKey("nextname")),
            entityAttributeRef("nextname"),
        )
        assertEquals(AttributeKey("nextname"), reloaded.key)
    }

    @Test
    fun `update attribute key does not loose entity identifier attribute`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        val e = env.query.findEntity(env.sampleModelRef, env.sampleEntityRef)
        val attrId = e.identifierAttributeId
        val newKey = AttributeKey("id_next")
        // Be careful to specify "reloadId" because the attribute's id changed
        env.updateAttributeDef(
            attributeRef = entityAttributeRef(attrId),
            command = AttributeDefUpdateCmd.Key(newKey),
            reloadId = entityAttributeRef(newKey)
        )
        val reloadedEntity = env.query.findEntity(env.sampleModelRef, env.sampleEntityRef)
        assertEquals(attrId, reloadedEntity.identifierAttributeId)


    }

    @Test
    fun `update attribute name is persisted`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        val attr = env.createAttributeDef(name = null)
        val nextValue = LocalizedTextNotLocalized("New name")
        val reloaded = env.updateAttributeDef(entityAttributeRef(attr.key), AttributeDefUpdateCmd.Name(nextValue))
        assertEquals(nextValue, reloaded.name)
    }

    @Test
    fun `update attribute name to null stays null`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        val attr = env.createAttributeDef(name = LocalizedTextNotLocalized("Name"))
        val reloaded = env.updateAttributeDef(entityAttributeRef(attr.key), AttributeDefUpdateCmd.Name(null))
        assertNull(reloaded.name)
    }


    @Test
    fun `update attribute description is persisted`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        val attr = env.createAttributeDef(description = null)
        val nextValue = LocalizedMarkdownNotLocalized("New description")
        val reloaded =
            env.updateAttributeDef(entityAttributeRef(attr.key), AttributeDefUpdateCmd.Description(nextValue))
        assertEquals(nextValue, reloaded.description)
    }

    @Test
    fun `update attribute description to null stays null`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        val attr = env.createAttributeDef(description = LocalizedMarkdownNotLocalized("New description"))
        val reloaded = env.updateAttributeDef(entityAttributeRef(attr.key), AttributeDefUpdateCmd.Description(null))
        assertNull(reloaded.description)
    }

    @Test
    fun `update attribute type is persisted`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        val typeMarkdownKey = TypeKey("Markdown")
        env.cmd.dispatch(
            ModelCmd.CreateType(
                env.sampleModelRef,
                ModelTypeInitializer(id = typeMarkdownKey, name = null, description = null)
            )
        )
        val typeMarkdownId =env.query.findType(env.sampleModelRef, typeRef(typeMarkdownKey)).id
        val type = env.query.findType(env.sampleModelRef, typeRef("String"))
        val attr = env.createAttributeDef(type = typeRef(type.id))


        val reloaded = env.updateAttributeDef(entityAttributeRef(attr.key), AttributeDefUpdateCmd.Type(typeRef(typeMarkdownKey)))
        assertEquals(typeMarkdownId, reloaded.typeId)
    }

    @Test
    fun `update attribute unknown type then error`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        val type = env.query.findType(env.sampleModelRef, typeRef("String"))
        val attr = env.createAttributeDef(type = typeRef(type.id))
        assertThrows<TypeNotFoundException> {
            env.cmd.dispatch(
                ModelCmd.UpdateEntityAttribute(
                    env.sampleModelRef,
                    env.sampleEntityRef,
                    entityAttributeRef(attr.key),
                    AttributeDefUpdateCmd.Type(typeRef("String2"))
                )
            )
        }
    }

    @Test
    fun `update attribute optional true to false is persisted`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        val attr = env.createAttributeDef(optional = true)
        val nextValue = false
        val reloaded = env.updateAttributeDef(entityAttributeRef(attr.key), AttributeDefUpdateCmd.Optional(nextValue))
        assertEquals(nextValue, reloaded.optional)
    }

    @Test
    fun `update attribute optional false to true is persisted`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        val attr = env.createAttributeDef(optional = false)
        val nextValue = true
        val reloaded = env.updateAttributeDef(entityAttributeRef(attr.key), AttributeDefUpdateCmd.Optional(nextValue))
        assertEquals(nextValue, reloaded.optional)
    }

    @Test
    fun `delete entity attribute in model then attribute removed`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        env.createAttributeDef(attributeKey = AttributeKey("bk"))
        env.createAttributeDef(attributeKey = AttributeKey("firstname"))
        env.createAttributeDef(attributeKey = AttributeKey("lastname"))
        env.cmd.dispatch(
            ModelCmd.DeleteEntityAttribute(
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
        env.addSampleEntityDef()
        env.createAttributeDef(attributeKey = AttributeKey("bk"))
        env.createAttributeDef(attributeKey = AttributeKey("firstname"))
        env.createAttributeDef(attributeKey = AttributeKey("lastname"))

        val reloaded = env.query.findEntity(env.sampleModelRef, env.sampleEntityRef)
        assertThrows<DeleteAttributeIdentifierException> {
            env.cmd.dispatch(
                ModelCmd.DeleteEntityAttribute(
                    env.sampleModelRef,
                    env.sampleEntityRef,
                    EntityAttributeRef.ByKey(reloaded.entityIdAttributeDefId())
                )
            )
        }
    }

    // ------------------------------------------------------------------------
    // Validation process
    // ------------------------------------------------------------------------

    class TestEnvInvalidModel {
        val runtime = createRuntime()
        val cmd = runtime.cmd
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
                addEntityDef(
                    key = EntityKey("Contact"),
                    // Error is here
                    identifierAttributeId = AttributeId.generate(),
                ) {
                    addAttribute(
                        AttributeDefInMemory(
                            id = AttributeId.generate(),
                            key = AttributeKey("id"),
                            typeId = typeStringId,
                            name = null,
                            description = null,
                            optional = false,
                            hashtags = emptyList()
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
            env.cmd.dispatch(
                ModelCmd.CreateType(
                    env.modelRef,
                    ModelTypeInitializer(TypeKey("Markdown"), null, null)
                )
            )
        }

    }

}
