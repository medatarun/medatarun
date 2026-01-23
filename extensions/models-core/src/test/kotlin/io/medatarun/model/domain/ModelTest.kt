package io.medatarun.model.domain

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
        val modelKeyWrong = ModelKey("m2")
        assertFailsWith(ModelNotFoundByKeyException::class) {
            cmd.dispatch(
                ModelCmd.UpdateModelName(
                    modelKeyWrong,
                    LocalizedTextNotLocalized("other")
                )
            )
        }
        assertFailsWith(ModelNotFoundByKeyException::class) {
            cmd.dispatch(
                ModelCmd.UpdateModelDescription(
                    modelKeyWrong,
                    LocalizedMarkdownNotLocalized("other description")
                )
            )
        }
        assertFailsWith(ModelNotFoundByKeyException::class) {
            cmd.dispatch(
                ModelCmd.UpdateModelVersion(
                    modelKeyWrong,
                    ModelVersion("3.0.0")
                )
            )
        }
        cmd.dispatch(ModelCmd.UpdateModelName(modelKey, LocalizedTextNotLocalized("Model name 2")))
        assertEquals(LocalizedTextNotLocalized("Model name 2"), query.findModelByKey(modelKey).name)
    }

    class TestEnvOneModel {
        val runtime = createRuntime()
        val cmd: ModelCmds = runtime.cmd
        val query: ModelQueries = runtime.queries
        val modelKey = ModelKey("m1")

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
                    modelKey = modelKey,
                    initializer = ModelTypeInitializer(TypeKey("String"), null, null)
                )
            )
        }
    }


    @Test
    fun `updates on model name persists the name`() {
        val env = TestEnvOneModel()
        env.cmd.dispatch(ModelCmd.UpdateModelName(env.modelKey, LocalizedTextNotLocalized("Model name 2")))
        assertEquals(LocalizedTextNotLocalized("Model name 2"), env.query.findModelByKey(env.modelKey).name)
    }

    @Test
    fun `updates on model description persists the description`() {
        val env = TestEnvOneModel()
        env.cmd.dispatch(ModelCmd.UpdateModelDescription(env.modelKey, LocalizedMarkdownNotLocalized("Model description 2")))
        assertEquals(LocalizedMarkdownNotLocalized("Model description 2"), env.query.findModelByKey(env.modelKey).description)
    }

    @Test
    fun `updates on model description to null persists the description`() {
        val env = TestEnvOneModel()
        env.cmd.dispatch(ModelCmd.UpdateModelDescription(env.modelKey, LocalizedMarkdownNotLocalized("Model description 2")))
        env.cmd.dispatch(ModelCmd.UpdateModelDescription(env.modelKey, null))
        assertNull(env.query.findModelByKey(env.modelKey).description)
    }

    @Test
    fun `updates on model version persists the version`() {
        val env = TestEnvOneModel()
        env.cmd.dispatch(ModelCmd.UpdateModelVersion(env.modelKey, ModelVersion("4.5.6")))
        assertEquals(ModelVersion("4.5.6"), env.query.findModelByKey(env.modelKey).version)
    }

    @Test
    fun `update documentation home with value then updated`() {
        val env = TestEnvOneModel()
        val url = URI("https://some.url/index.html").toURL()
        env.cmd.dispatch(ModelCmd.UpdateModelDocumentationHome(env.modelKey, url))
        assertEquals(url, env.query.findModelByKey(env.modelKey).documentationHome)
    }

    @Test
    fun `update documentation home with null then updated to null`() {
        val env = TestEnvOneModel()
        val url = URI("https://some.url/index.html").toURL()
        env.cmd.dispatch(ModelCmd.UpdateModelDocumentationHome(env.modelKey, url))
        env.cmd.dispatch(ModelCmd.UpdateModelDocumentationHome(env.modelKey, null))
        assertNull(env.query.findModelByKey(env.modelKey).documentationHome)
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
        assertThrows<ModelNotFoundByKeyException> {
            cmd.dispatch(ModelCmd.DeleteModel(ModelKey("m-to-delete-repo-3")))
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

        cmd.dispatch(ModelCmd.DeleteModel(ModelKey("m-to-delete-repo-1")))
        assertNull(repo1.findModelByKeyOptional(ModelKey("m-to-delete-repo-1")))
        assertFailsWith<ModelNotFoundByKeyException> {
            query.findModelByKey(ModelKey("m-to-delete-repo-1"))
        }
        assertNotNull(query.findModelByKey(ModelKey("m-to-preserve-repo-1")))
        assertNotNull(query.findModelByKey(ModelKey("m-to-delete-repo-2")))
        assertNotNull(query.findModelByKey(ModelKey("m-to-preserve-repo-2")))

        cmd.dispatch(ModelCmd.DeleteModel(ModelKey("m-to-delete-repo-2")))
        assertNull(repo1.findModelByKeyOptional(ModelKey("m-to-delete-repo-2")))
        assertNotNull(query.findModelByKey(ModelKey("m-to-preserve-repo-1")))
        assertNotNull(query.findModelByKey(ModelKey("m-to-preserve-repo-2")))

        cmd.dispatch(ModelCmd.DeleteModel(ModelKey("m-to-preserve-repo-1")))
        assertNull(repo2.findModelByKeyOptional(ModelKey("m-to-delete-repo-2")))
        assertNotNull(query.findModelByKey(ModelKey("m-to-preserve-repo-2")))

        cmd.dispatch(ModelCmd.DeleteModel(ModelKey("m-to-preserve-repo-2")))
        assertNull(repo2.findModelByKeyOptional(ModelKey("m-to-delete-repo-2")))

    }

    // ------------------------------------------------------------------------
    // Types
    // ------------------------------------------------------------------------

    class TestEnvTypes {
        val runtime = createRuntime()
        val cmd: ModelCmds = runtime.cmd
        val query: ModelQueries = runtime.queries
        val modelKey = ModelKey("m1")

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
                env.modelKey,
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
        env.cmd.dispatch(ModelCmd.CreateType(env.modelKey, ModelTypeInitializer(TypeKey("String"), null, null)))
        assertEquals(1, env.model.types.size)
        val type = env.model.findTypeOptional(TypeKey("String"))
        assertNotNull(type)
        assertNull(type.name)
        assertNull(type.description)
    }

    @Test
    fun `create type on unknown model throw ModelNotFoundException`() {
        val env = TestEnvTypes()
        assertThrows<ModelNotFoundByKeyException> {
            env.cmd.dispatch(
                ModelCmd.CreateType(
                    ModelKey("unknown"),
                    ModelTypeInitializer(TypeKey("String"), null, null)
                )
            )
        }
    }

    @Test
    fun `create type with duplicate name throws DuplicateTypeException`() {
        val env = TestEnvTypes()
        env.cmd.dispatch(ModelCmd.CreateType(env.modelKey, ModelTypeInitializer(TypeKey("String"), null, null)))
        assertThrows<TypeCreateDuplicateException> {
            env.cmd.dispatch(
                ModelCmd.CreateType(
                    env.modelKey,
                    ModelTypeInitializer(TypeKey("String"), null, null)
                )
            )
        }
    }

    @Test
    fun `update type name `() {
        val env = TestEnvTypes()
        env.cmd.dispatch(ModelCmd.CreateType(env.modelKey, ModelTypeInitializer(TypeKey("String"), null, null)))
        env.cmd.dispatch(
            ModelCmd.UpdateType(
                env.modelKey,
                TypeKey("String"),
                ModelTypeUpdateCmd.Name(LocalizedTextNotLocalized("This is a string"))
            )
        )
        val t = env.model.findTypeOptional(TypeKey("String"))
        assertNotNull(t)
        assertEquals(LocalizedTextNotLocalized("This is a string"), t.name)
    }

    @Test
    fun `update type name with null`() {
        val env = TestEnvTypes()
        env.cmd.dispatch(ModelCmd.CreateType(env.modelKey, ModelTypeInitializer(TypeKey("String"), null, null)))
        env.cmd.dispatch(ModelCmd.UpdateType(env.modelKey, TypeKey("String"), ModelTypeUpdateCmd.Name(null)))
        val t = env.model.findTypeOptional(TypeKey("String"))
        assertNotNull(t)
        assertNull(t.name)
    }

    @Test
    fun `update type description`() {
        val env = TestEnvTypes()
        env.cmd.dispatch(ModelCmd.CreateType(env.modelKey, ModelTypeInitializer(TypeKey("String"), null, null)))
        env.cmd.dispatch(
            ModelCmd.UpdateType(
                env.modelKey,
                TypeKey("String"),
                ModelTypeUpdateCmd.Description(LocalizedMarkdownNotLocalized("This is a string"))
            )
        )
        val t = env.model.findTypeOptional(TypeKey("String"))
        assertNotNull(t)
        assertEquals(LocalizedMarkdownNotLocalized("This is a string"), t.description)
    }

    @Test
    fun `update type description with null`() {
        val env = TestEnvTypes()
        env.cmd.dispatch(ModelCmd.CreateType(env.modelKey, ModelTypeInitializer(TypeKey("String"), null, null)))
        env.cmd.dispatch(ModelCmd.UpdateType(env.modelKey, TypeKey("String"), ModelTypeUpdateCmd.Description(null)))
        val t = env.model.findTypeOptional(TypeKey("String"))
        assertNotNull(t)
        assertNull(t.description)
    }

    @Test
    fun `update type with model not found`() {
        val env = TestEnvTypes()
        env.cmd.dispatch(ModelCmd.CreateType(env.modelKey, ModelTypeInitializer(TypeKey("String"), null, null)))
        assertThrows<ModelNotFoundByKeyException> {
            env.cmd.dispatch(
                ModelCmd.UpdateType(
                    ModelKey("unknown"),
                    TypeKey("String"),
                    ModelTypeUpdateCmd.Description(null)
                )
            )
        }
    }

    @Test
    fun `update type with type not found`() {
        val env = TestEnvTypes()
        env.cmd.dispatch(ModelCmd.CreateType(env.modelKey, ModelTypeInitializer(TypeKey("String"), null, null)))
        assertThrows<TypeNotFoundException> {
            env.cmd.dispatch(
                ModelCmd.UpdateType(
                    env.modelKey,
                    TypeKey("String2"),
                    ModelTypeUpdateCmd.Description(null)
                )
            )
        }
    }

    @Test
    fun `delete type model not found`() {
        val env = TestEnvTypes()
        env.cmd.dispatch(ModelCmd.CreateType(env.modelKey, ModelTypeInitializer(TypeKey("String"), null, null)))
        assertThrows<ModelNotFoundByKeyException> {
            env.cmd.dispatch(ModelCmd.DeleteType(ModelKey("unknown"), TypeKey("String")))
        }
    }

    @Test
    fun `delete type type not found`() {
        val env = TestEnvTypes()
        env.cmd.dispatch(ModelCmd.CreateType(env.modelKey, ModelTypeInitializer(TypeKey("String"), null, null)))
        assertThrows<TypeNotFoundException> {
            env.cmd.dispatch(ModelCmd.DeleteType(env.modelKey, TypeKey("String2")))
        }
    }

    @Test
    fun `delete type used in attributes then error`() {
        val env = TestEnvTypes()
        env.cmd.dispatch(ModelCmd.CreateType(env.modelKey, ModelTypeInitializer(TypeKey("String"), null, null)))
        env.cmd.dispatch(ModelCmd.CreateType(env.modelKey, ModelTypeInitializer(TypeKey("Markdown"), null, null)))
        env.cmd.dispatch(ModelCmd.CreateType(env.modelKey, ModelTypeInitializer(TypeKey("Int"), null, null)))
        env.cmd.dispatch(
            ModelCmd.CreateEntityDef(
                env.modelKey, EntityDefInitializer(
                    entityKey = EntityKey("contact"), name = null, description = null,
                    documentationHome = null,
                    identityAttribute = AttributeDefIdentityInitializer(
                        attributeKey = AttributeKey("name"),
                        type = TypeKey("String"),
                        name = null,
                        description = null
                    )
                )
            )
        )
        env.cmd.dispatch(
            ModelCmd.CreateEntityDefAttributeDef(
                modelKey = env.modelKey,
                entityKey = EntityKey("contact"),
                attributeDefInitializer = AttributeDefInitializer(
                    attributeKey = AttributeKey("infos"),
                    type = TypeKey("Markdown"),
                    optional = false, name = null, description = null
                )
            )
        )
        assertThrows<ModelTypeDeleteUsedException> {
            env.cmd.dispatch(ModelCmd.DeleteType(env.modelKey, TypeKey("String")))
        }
        assertThrows<ModelTypeDeleteUsedException> {
            env.cmd.dispatch(ModelCmd.DeleteType(env.modelKey, TypeKey("Markdown")))
        }
        env.cmd.dispatch(ModelCmd.DeleteType(env.modelKey, TypeKey("Int")))

    }

    @Test
    fun `delete type success`() {
        val env = TestEnvTypes()
        env.cmd.dispatch(ModelCmd.CreateType(env.modelKey, ModelTypeInitializer(TypeKey("String"), null, null)))
        env.cmd.dispatch(ModelCmd.CreateType(env.modelKey, ModelTypeInitializer(TypeKey("Markdown"), null, null)))
        env.cmd.dispatch(ModelCmd.CreateType(env.modelKey, ModelTypeInitializer(TypeKey("Int"), null, null)))
        env.cmd.dispatch(ModelCmd.DeleteType(env.modelKey, TypeKey("Int")))
        assertNull(env.model.findTypeOptional(TypeKey("Int")))
        assertNotNull(env.model.findTypeOptional(TypeKey("String")))
        assertNotNull(env.model.findTypeOptional(TypeKey("Markdown")))

    }

    // ------------------------------------------------------------------------
    // Entities
    // ------------------------------------------------------------------------

    @Test
    fun `create entity then id name and description shall persist`() {
        val env = TestEnvOneModel()
        val entityId = EntityKey("entity")
        val name = LocalizedTextNotLocalized("Order")
        val description = LocalizedMarkdownNotLocalized("Order description")

        env.cmd.dispatch(
            ModelCmd.CreateEntityDef(
                env.modelKey, EntityDefInitializer.build(
                    entityKey = entityId,
                    identityAttribute = AttributeDefIdentityInitializer(
                        attributeKey = AttributeKey("id"),
                        type = TypeKey("String"),
                        name = LocalizedTextNotLocalized("Identifier"),
                        description = LocalizedMarkdownNotLocalized("Identifier description")
                    )
                ) {
                    this.name = name
                    this.description = description

                }
            )
        )

        val reloaded = env.query.findModelByKey(env.modelKey).findEntityDef(entityId)
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
        val entityId = EntityKey("entity-null-name")
        val description = LocalizedMarkdownNotLocalized("Entity without name")

        env.cmd.dispatch(
            ModelCmd.CreateEntityDef(
                env.modelKey, EntityDefInitializer.build(
                    entityKey = entityId,
                    identityAttribute = AttributeDefIdentityInitializer.build(
                        attributeKey = AttributeKey("id"),
                        type = TypeKey("String")
                    )
                ) {
                    this.description = description
                }
            )
        )

        val reloaded = env.query.findModelByKey(env.modelKey).findEntityDef(entityId)
        assertEquals(entityId, reloaded.key)
        assertNull(reloaded.name)
        assertEquals(description, reloaded.description)
        assertEquals(1, reloaded.attributes.size)
    }

    @Test
    fun `create entity with null description then description shall be null`() {
        val env = TestEnvOneModel()
        val entityId = EntityKey("entity-null-description")
        val name = LocalizedTextNotLocalized("Entity without description")

        env.cmd.dispatch(
            ModelCmd.CreateEntityDef(
                env.modelKey, EntityDefInitializer.build(
                    entityKey = entityId,
                    identityAttribute = AttributeDefIdentityInitializer.build(
                        attributeKey = AttributeKey("String"),
                        type = TypeKey("String")
                    )
                ) {
                    this.name = name
                }
            )
        )

        val reloaded = env.query.findModelByKey(env.modelKey).findEntityDef(entityId)
        assertEquals(entityId, reloaded.key)
        assertEquals(name, reloaded.name)
        assertNull(reloaded.description)
    }

    @Test
    fun `create entity with null attribute name and description then name and desc shall be null`() {
        val env = TestEnvOneModel()
        val entityId = EntityKey("entity-null-attr-name")
        val description = LocalizedMarkdownNotLocalized("Entity without name")

        env.cmd.dispatch(
            ModelCmd.CreateEntityDef(
                env.modelKey, EntityDefInitializer.build(
                    entityKey = entityId,
                    identityAttribute = AttributeDefIdentityInitializer.build(
                        attributeKey = AttributeKey("id"),
                        type = TypeKey("String"),
                    ),
                ) {
                    this.description = description
                }
            )
        )

        val reloaded = env.query.findModelByKey(env.modelKey).findEntityDef(entityId)
        assertNull(reloaded.attributes[0].name)
        assertNull(reloaded.attributes[0].description)
    }

    @Test
    fun `create entity with documentation home null`() {
        val env = TestEnvOneModel()
        val entityId = EntityKey("entity-null-attr-name")
        env.cmd.dispatch(
            ModelCmd.CreateEntityDef(
                env.modelKey, EntityDefInitializer.build(
                    entityId, AttributeDefIdentityInitializer.build(
                        AttributeKey("id"),
                        TypeKey("String")
                    )
                )
            )
        )
        assertNull(env.query.findModelByKey(env.modelKey).findEntityDef(entityId).documentationHome)
    }

    @Test
    fun `create entity with documentation home not null`() {
        val env = TestEnvOneModel()
        val entityId = EntityKey("entity-null-attr-name")
        val url = URI("http://localhost:8080").toURL()
        env.cmd.dispatch(
            ModelCmd.CreateEntityDef(
                env.modelKey, EntityDefInitializer.build(
                    entityId, AttributeDefIdentityInitializer.build(
                        AttributeKey("id"),
                        TypeKey("String")
                    )
                ){
                    documentationHome = url
                }
            )
        )
        assertEquals(url, env.query.findModelByKey(env.modelKey).findEntityDef(entityId).documentationHome)
    }

    class TestEnvEntityUpdate {
        val runtime = createRuntime()
        val cmd: ModelCmds = runtime.cmd
        val query: ModelQueries = runtime.queries
        val modelKey = ModelKey("model-entity-update")
        val primaryEntityId = EntityKey("entity-primary")
        val secondaryEntityId = EntityKey("entity-secondary")

        init {
            cmd.dispatch(
                ModelCmd.CreateModel(
                    modelKey,
                    LocalizedTextNotLocalized("Model entity update"),
                    null,
                    ModelVersion("1.0.0")
                )
            )
            cmd.dispatch(ModelCmd.CreateType(modelKey, ModelTypeInitializer(TypeKey("String"), null, null)))
            cmd.dispatch(
                ModelCmd.CreateEntityDef(
                    modelKey,
                    EntityDefInitializer(
                        entityKey = primaryEntityId,
                        name = LocalizedTextNotLocalized("Entity primary"),
                        description = LocalizedMarkdownNotLocalized("Entity primary description"),
                        documentationHome = null,
                        identityAttribute = AttributeDefIdentityInitializer(
                            attributeKey = AttributeKey("id"),
                            type = TypeKey("String"),
                            name = null,
                            description = null
                        )
                    )
                )
            )
            cmd.dispatch(
                ModelCmd.CreateEntityDef(
                    modelKey,
                    EntityDefInitializer(
                        secondaryEntityId,
                        LocalizedTextNotLocalized("Entity secondary"),
                        LocalizedMarkdownNotLocalized("Entity secondary description"),
                        documentationHome = null,
                        identityAttribute = AttributeDefIdentityInitializer(
                            attributeKey = AttributeKey("id"),
                            type = TypeKey("String"),
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
        val wrongModelKey = ModelKey("unknown-model")

        assertFailsWith<ModelNotFoundByKeyException> {
            env.cmd.dispatch(
                ModelCmd.UpdateEntityDef(
                    wrongModelKey,
                    env.primaryEntityId,
                    EntityDefUpdateCmd.Name(LocalizedTextNotLocalized("Updated name"))
                )
            )
        }
    }

    @Test
    fun `update entity with wrong entity id throws EntityDefNotFoundException`() {
        val env = TestEnvEntityUpdate()
        val wrongEntityId = EntityKey("unknown-entity")

        assertFailsWith<EntityDefNotFoundException> {
            env.cmd.dispatch(
                ModelCmd.UpdateEntityDef(
                    env.modelKey,
                    wrongEntityId,
                    EntityDefUpdateCmd.Name(LocalizedTextNotLocalized("Updated name"))
                )
            )
        }
    }

    @Test
    fun `update entity id with duplicate id throws exception`() {
        val env = TestEnvEntityUpdate()
        val duplicateId = env.secondaryEntityId

        assertFailsWith<UpdateEntityDefIdDuplicateIdException> {
            env.cmd.dispatch(
                ModelCmd.UpdateEntityDef(
                    env.modelKey,
                    env.primaryEntityId,
                    EntityDefUpdateCmd.Id(duplicateId)
                )
            )
        }
    }

    @Test
    fun `update entity id with correct id ok`() {
        val env = TestEnvEntityUpdate()
        val newId = EntityKey("entity-renamed")

        env.cmd.dispatch(
            ModelCmd.UpdateEntityDef(
                env.modelKey,
                env.primaryEntityId,
                EntityDefUpdateCmd.Id(newId)
            )
        )

        val reloaded = env.query.findModelByKey(env.modelKey)
        assertNull(reloaded.findEntityDefOptional(env.primaryEntityId))
        assertNotNull(reloaded.findEntityDefOptional(newId))
    }

    @Test
    fun `update entity name not null persisted`() {
        val env = TestEnvEntityUpdate()
        val newName = LocalizedTextNotLocalized("Entity primary updated")

        env.cmd.dispatch(
            ModelCmd.UpdateEntityDef(
                env.modelKey,
                env.primaryEntityId,
                EntityDefUpdateCmd.Name(newName)
            )
        )

        val reloaded = env.query.findModelByKey(env.modelKey).findEntityDef(env.primaryEntityId)
        assertEquals(newName, reloaded.name)
    }

    @Test
    fun `update entity name null then name is null`() {
        val env = TestEnvEntityUpdate()

        env.cmd.dispatch(
            ModelCmd.UpdateEntityDef(
                env.modelKey,
                env.primaryEntityId,
                EntityDefUpdateCmd.Name(null)
            )
        )

        val reloaded = env.query.findModelByKey(env.modelKey).findEntityDef(env.primaryEntityId)
        assertNull(reloaded.name)
    }

    @Test
    fun `update entity description not null persisted`() {
        val env = TestEnvEntityUpdate()
        val newDescription = LocalizedMarkdownNotLocalized("Primary entity updated description")

        env.cmd.dispatch(
            ModelCmd.UpdateEntityDef(
                env.modelKey,
                env.primaryEntityId,
                EntityDefUpdateCmd.Description(newDescription)
            )
        )

        val reloaded = env.query.findModelByKey(env.modelKey).findEntityDef(env.primaryEntityId)
        assertEquals(newDescription, reloaded.description)
    }

    @Test
    fun `update entity description with null then description is null`() {
        val env = TestEnvEntityUpdate()

        env.cmd.dispatch(
            ModelCmd.UpdateEntityDef(
                env.modelKey,
                env.primaryEntityId,
                EntityDefUpdateCmd.Description(null)
            )
        )

        val reloaded = env.query.findModelByKey(env.modelKey).findEntityDef(env.primaryEntityId)
        assertNull(reloaded.description)
    }

    @Test
    fun `update entity documentation home not null`() {
        val env = TestEnvEntityUpdate()
        val url = URI("http://localhost").toURL()
        env.cmd.dispatch(
            ModelCmd.UpdateEntityDef(env.modelKey, env.primaryEntityId, EntityDefUpdateCmd.DocumentationHome(url))
        )
        val reloaded = env.query.findModelByKey(env.modelKey).findEntityDef(env.primaryEntityId)
        assertEquals(url, reloaded.documentationHome)
    }

    @Test
    fun `update entity documentation home to null`() {
        val env = TestEnvEntityUpdate()
        val url = URI("http://localhost").toURL()
        env.cmd.dispatch(
            ModelCmd.UpdateEntityDef(env.modelKey, env.primaryEntityId, EntityDefUpdateCmd.DocumentationHome(url))
        )
        env.cmd.dispatch(
            ModelCmd.UpdateEntityDef(env.modelKey, env.primaryEntityId, EntityDefUpdateCmd.DocumentationHome(null))
        )
        val reloaded = env.query.findModelByKey(env.modelKey).findEntityDef(env.primaryEntityId)
        assertNull(reloaded.documentationHome)
    }


    @Test
    fun `delete entity in model then entity removed`() {
        val env = TestEnvOneModel()
        val entityId = EntityKey("entity-to-delete")

        env.cmd.dispatch(
            ModelCmd.CreateEntityDef(
                env.modelKey, EntityDefInitializer(
                    entityKey = entityId,
                    name = LocalizedTextNotLocalized("To delete"),
                    description = null,
                    documentationHome = null,
                    identityAttribute = AttributeDefIdentityInitializer(
                        attributeKey = AttributeKey("id"),
                        type = TypeKey("String"),
                        name = null,
                        description = null
                    )
                )
            )
        )
        env.cmd.dispatch(ModelCmd.DeleteEntityDef(env.modelKey, entityId))

        val reloaded = env.query.findModelByKey(env.modelKey)
        assertNull(reloaded.findEntityDefOptional(entityId))
    }

    @Test
    fun `delete entity with same id in two models then only entity in the specified model is removed`() {
        val runtime = createRuntime()
        val cmd: ModelCmds = runtime.cmd
        val query: ModelQueries = runtime.queries

        val modelKey1 = ModelKey("model-1")
        val modelKey2 = ModelKey("model-2")
        val entityId = EntityKey("shared-entity")

        cmd.dispatch(
            ModelCmd.CreateModel(
                modelKey1,
                LocalizedTextNotLocalized("Model 1"),
                null,
                ModelVersion("1.0.0")
            )
        )
        cmd.dispatch(ModelCmd.CreateType(modelKey1, ModelTypeInitializer(TypeKey("String"), null, null)))
        cmd.dispatch(
            ModelCmd.CreateModel(
                modelKey2,
                LocalizedTextNotLocalized("Model 2"),
                null,
                ModelVersion("1.0.0")
            )
        )
        cmd.dispatch(ModelCmd.CreateType(modelKey2, ModelTypeInitializer(TypeKey("String"), null, null)))
        cmd.dispatch(
            ModelCmd.CreateEntityDef(
                modelKey1, EntityDefInitializer(
                    entityKey = entityId, name = LocalizedTextNotLocalized("Entity"), description = null,
                    documentationHome = null,
                    identityAttribute = AttributeDefIdentityInitializer(
                        attributeKey = AttributeKey("id"),
                        type = TypeKey("String"),
                        name = null,
                        description = null
                    )
                )
            )
        )
        cmd.dispatch(
            ModelCmd.CreateEntityDef(
                modelKey2, EntityDefInitializer(
                    entityKey = entityId, name = LocalizedTextNotLocalized("Entity"), description = null,
                    documentationHome = null,
                    identityAttribute = AttributeDefIdentityInitializer(
                        attributeKey = AttributeKey("id"),
                        type = TypeKey("String"),
                        name = null,
                        description = null
                    )
                )
            )
        )

        cmd.dispatch(ModelCmd.DeleteEntityDef(modelKey1, entityId))

        val reloadedModel1 = query.findModelByKey(modelKey1)
        val reloadedModel2 = query.findModelByKey(modelKey2)

        assertNull(reloadedModel1.findEntityDefOptional(entityId))
        assertNotNull(reloadedModel2.findEntityDefOptional(entityId))
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Attributes
    // -----------------------------------------------------------------------------------------------------------------

    class TestEnvAttribute {
        val runtime = createRuntime()
        val cmd: ModelCmds = runtime.cmd
        val query: ModelQueries = runtime.queries
        val sampleModelKey = ModelKey("model-1")
        val sampleEntityKey = EntityKey("Entity1")

        init {
            cmd.dispatch(
                ModelCmd.CreateModel(
                    sampleModelKey,
                    LocalizedTextNotLocalized("Model 1"),
                    null,
                    ModelVersion("1.0.0"),
                )
            )
            cmd.dispatch(ModelCmd.CreateType(sampleModelKey, ModelTypeInitializer(TypeKey("String"), null, null)))
        }

        fun addSampleEntityDef() {
            cmd.dispatch(
                ModelCmd.CreateEntityDef(
                    sampleModelKey,
                    EntityDefInitializer(
                        entityKey = sampleEntityKey, name = null, description = null,
                        documentationHome = null,
                        identityAttribute = AttributeDefIdentityInitializer(
                            attributeKey = AttributeKey("id"),
                            type = TypeKey("String"),
                            name = null,
                            description = null
                        )
                    )
                )
            )
        }

        fun createAttributeDef(
            attributeKey: AttributeKey = AttributeKey("myattribute"),
            type: TypeKey = TypeKey("String"),
            optional: Boolean = false,
            name: LocalizedText? = null,
            description: LocalizedMarkdown? = null
        ): AttributeDef {

            cmd.dispatch(
                ModelCmd.CreateEntityDefAttributeDef(
                    modelKey = sampleModelKey,
                    entityKey = sampleEntityKey,
                    attributeDefInitializer = AttributeDefInitializer(
                        attributeKey = attributeKey,
                        type = type,
                        optional = optional,
                        name = name,
                        description = description,
                    )
                )
            )
            val reloaded =
                query.findModelByKey(sampleModelKey).findEntityDef(sampleEntityKey).getAttributeDef(attributeKey)
            return reloaded
        }

        fun updateAttributeDef(
            attributeKey: AttributeKey = AttributeKey("myattribute"),
            command: AttributeDefUpdateCmd,
            reloadId: AttributeKey? = null
        ): AttributeDef {
            cmd.dispatch(
                ModelCmd.UpdateEntityDefAttributeDef(
                    sampleModelKey,
                    sampleEntityKey,
                    attributeKey,
                    command
                )
            )
            val reloaded = query.findModelByKey(sampleModelKey).findEntityDef(sampleEntityKey)
                .getAttributeDef(reloadId ?: attributeKey)
            return reloaded
        }

    }

    @Test
    fun `create attribute then id name and description shall persist`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        val reloaded = env.createAttributeDef(
            attributeKey = AttributeKey("businesskey"),
            type = TypeKey("String"),
            optional = false,
            name = LocalizedTextNotLocalized("Business Key"),
            description = LocalizedMarkdownNotLocalized("Unique business key"),
        )
        assertEquals(AttributeKey("businesskey"), reloaded.key)
        assertEquals(LocalizedTextNotLocalized("Business Key"), reloaded.name)
        assertEquals(LocalizedMarkdownNotLocalized("Unique business key"), reloaded.description)
        assertEquals(TypeKey("String"), reloaded.type)
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
        env.cmd.dispatch(
            ModelCmd.CreateType(
                env.sampleModelKey,
                ModelTypeInitializer(TypeKey("Boolean"), null, null)
            )
        )

        val reloaded = env.createAttributeDef(type = TypeKey("Boolean"))
        assertEquals(TypeKey("Boolean"), reloaded.type)
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
            env.createAttributeDef(attributeKey = AttributeKey("lastname"), type = TypeKey("UnknownType"))
        }
    }

    @Test
    fun `update attribute with wrong model id throws ModelNotFoundException`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        env.createAttributeDef()
        assertFailsWith<ModelNotFoundByKeyException> {
            env.cmd.dispatch(
                ModelCmd.UpdateEntityDefAttributeDef(
                    modelKey = ModelKey("unknown"),
                    entityKey = EntityKey("unknownEntity"),
                    attributeKey = AttributeKey("unknownAttribute"),
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
        assertFailsWith<EntityDefNotFoundException> {
            env.cmd.dispatch(
                ModelCmd.UpdateEntityDefAttributeDef(
                    modelKey = env.sampleModelKey,
                    entityKey = EntityKey("unknownEntity"),
                    attributeKey = AttributeKey("unknownAttribute"),
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
        assertFailsWith<EntityAttributeDefNotFoundException> {
            env.cmd.dispatch(
                ModelCmd.UpdateEntityDefAttributeDef(
                    modelKey = env.sampleModelKey,
                    entityKey = env.sampleEntityKey,
                    attributeKey = AttributeKey("unknownAttribute"),
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
        assertFailsWith<UpdateAttributeDefDuplicateIdException> {
            // Rename firstname to lastname causes exception because lastname already exists
            env.updateAttributeDef(
                attributeKey = AttributeKey("firstname"),
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
            AttributeKey("firstname"),
            AttributeDefUpdateCmd.Key(AttributeKey("nextname")),
            AttributeKey("nextname"),
        )
        assertEquals(AttributeKey("nextname"), reloaded.key)
    }

    @Test
    fun `update attribute id of the entity identifier also changes the identifier id`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        val e = env.query.findModelByKey(env.sampleModelKey).findEntityDef(env.sampleEntityKey)
        val attrId = e.identifierAttributeKey
        val attrIdNext = AttributeKey("id_next")
        // Be careful to specify "reloadId" because the attribute's id changed
        env.updateAttributeDef(attrId, command = AttributeDefUpdateCmd.Key(attrIdNext), reloadId = attrIdNext)
        val reloadedEntity = env.query.findModelByKey(env.sampleModelKey).findEntityDef(env.sampleEntityKey)
        assertEquals(attrIdNext, reloadedEntity.identifierAttributeKey)


    }

    @Test
    fun `update attribute name is persisted`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        val attr = env.createAttributeDef(name = null)
        val nextValue = LocalizedTextNotLocalized("New name")
        val reloaded = env.updateAttributeDef(attr.key, AttributeDefUpdateCmd.Name(nextValue))
        assertEquals(nextValue, reloaded.name)
    }

    @Test
    fun `update attribute name to null stays null`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        val attr = env.createAttributeDef(name = LocalizedTextNotLocalized("Name"))
        val reloaded = env.updateAttributeDef(attr.key, AttributeDefUpdateCmd.Name(null))
        assertNull(reloaded.name)
    }


    @Test
    fun `update attribute description is persisted`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        val attr = env.createAttributeDef(description = null)
        val nextValue = LocalizedMarkdownNotLocalized("New description")
        val reloaded = env.updateAttributeDef(attr.key, AttributeDefUpdateCmd.Description(nextValue))
        assertEquals(nextValue, reloaded.description)
    }

    @Test
    fun `update attribute description to null stays null`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        val attr = env.createAttributeDef(description = LocalizedMarkdownNotLocalized("New description"))
        val reloaded = env.updateAttributeDef(attr.key, AttributeDefUpdateCmd.Description(null))
        assertNull(reloaded.description)
    }

    @Test
    fun `update attribute type is persisted`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        val typeMarkdownId = TypeKey("Markdown")
        env.cmd.dispatch(
            ModelCmd.CreateType(
                env.sampleModelKey,
                ModelTypeInitializer(id = typeMarkdownId, name = null, description = null)
            )
        )

        val attr = env.createAttributeDef(type = TypeKey("String"))

        val reloaded = env.updateAttributeDef(attr.key, AttributeDefUpdateCmd.Type(typeMarkdownId))
        assertEquals(typeMarkdownId, reloaded.type)
    }

    @Test
    fun `update attribute unknown type then error`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        val attr = env.createAttributeDef(type = TypeKey("String"))
        assertThrows<TypeNotFoundException> {
            env.cmd.dispatch(
                ModelCmd.UpdateEntityDefAttributeDef(
                    env.sampleModelKey,
                    env.sampleEntityKey,
                    attr.key,
                    AttributeDefUpdateCmd.Type(TypeKey("String2"))
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
        val reloaded = env.updateAttributeDef(attr.key, AttributeDefUpdateCmd.Optional(nextValue))
        assertEquals(nextValue, reloaded.optional)
    }

    @Test
    fun `update attribute optional false to true is persisted`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        val attr = env.createAttributeDef(optional = false)
        val nextValue = true
        val reloaded = env.updateAttributeDef(attr.key, AttributeDefUpdateCmd.Optional(nextValue))
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
            ModelCmd.DeleteEntityDefAttributeDef(
                env.sampleModelKey,
                env.sampleEntityKey,
                AttributeKey("firstname")
            )
        )
        val reloaded = env.query.findModelByKey(env.sampleModelKey).findEntityDef(env.sampleEntityKey)

        assertTrue(reloaded.hasAttributeDef(AttributeKey("bk")))
        assertFalse(reloaded.hasAttributeDef(AttributeKey("firstname")))
        assertTrue(reloaded.hasAttributeDef(AttributeKey("lastname")))

    }

    @Test
    fun `delete entity attribute used as identifier throws error`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        env.createAttributeDef(attributeKey = AttributeKey("bk"))
        env.createAttributeDef(attributeKey = AttributeKey("firstname"))
        env.createAttributeDef(attributeKey = AttributeKey("lastname"))

        val reloaded = env.query.findModelByKey(env.sampleModelKey).findEntityDef(env.sampleEntityKey)
        assertThrows<DeleteAttributeIdentifierException> {
            env.cmd.dispatch(
                ModelCmd.DeleteEntityDefAttributeDef(
                    env.sampleModelKey,
                    env.sampleEntityKey,
                    reloaded.entityIdAttributeDefId()
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
        val modelKey = ModelKey("test")
        val invalidModel = ModelInMemory.builder(
            key = modelKey,
            version = ModelVersion("0.0.1"),
        ) {
            name = null
            description = null
            types = mutableListOf(ModelTypeInMemory(id = TypeId.generate(), key = TypeKey("String"), name = null, description = null))
            addEntityDef(
                key = EntityKey("Contact"),
                // Error is here
                identifierAttributeKey = AttributeKey("unknown"),
            ) {
                addAttribute(
                    AttributeDefInMemory(
                        id = AttributeId.generate(),
                        key = AttributeKey("id"),
                        type = TypeKey("String"),
                        name = null,
                        description = null,
                        optional = false,
                        hashtags = emptyList()
                    )
                )
            }

        }

        init {
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

        // Getting a model that has error shall fail with invalid exception
        assertThrows<ModelInvalidException> { env.query.findModelByKey(env.modelKey) }

        // Find all model ids shall not validate models, just give their ids
        assertDoesNotThrow { env.query.findAllModelIds() }

        // Creating or trying to modify something in invalid model shall throw error
        assertThrows<ModelInvalidException> {
            env.cmd.dispatch(
                ModelCmd.CreateType(
                    env.modelKey,
                    ModelTypeInitializer(TypeKey("Markdown"), null, null)
                )
            )
        }


    }

}
