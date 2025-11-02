package io.medatarun.model.model

import io.medatarun.model.infra.*
import io.medatarun.model.internal.ModelValidationImpl
import io.medatarun.model.model.ModelTestRuntime.Companion.createRuntime
import io.medatarun.model.ports.RepositoryRef
import io.medatarun.model.ports.RepositoryRef.Companion.ref
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
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
            cmd.createModel(
                ModelId("m1"),
                LocalizedTextNotLocalized("M1"),
                null,
                ModelVersion("1.0.0")
            )
        }
    }

    @Test
    fun `create model ok with one storage mode auto`() {
        val repo1 = ModelRepositoryInMemory("repo1")
        val cmd = createRuntime(repositories = listOf(repo1)).cmd
        val modelId = ModelId("m1")
        assertDoesNotThrow {
            cmd.createModel(
                modelId,
                LocalizedTextNotLocalized("M1"),
                null,
                ModelVersion("1.0.0")
            )
        }
        assertNotNull(repo1.findModelByIdOptional(modelId))
    }

    @Test
    fun `create model ok with multiple storages and specified storage`() {
        val repo1 = ModelRepositoryInMemory("repo1")
        val repo2 = ModelRepositoryInMemory("repo2")
        val runtime = createRuntime(repositories = listOf(repo1, repo2))
        val cmd = runtime.cmd
        val query = runtime.queries

        val modelId = ModelId("m1")
        cmd.createModel(
            modelId,
            LocalizedTextNotLocalized("M1"),
            null,
            ModelVersion("1.0.0"),
            RepositoryRef.Id(repo2.repositoryId)
        )
        assertDoesNotThrow { query.findModelById(modelId) }

        assertNull(repo1.findModelByIdOptional(modelId))
        assertNotNull(repo2.findModelByIdOptional(modelId))

    }

    @Test
    fun `create model with name description and version when present`() {
        val runtime = createRuntime()
        val cmd: ModelCmds = runtime.cmd
        val query: ModelQueries = runtime.queries

        val modelId = ModelId("m-")
        val name = LocalizedTextNotLocalized("Model name")
        val description = LocalizedMarkdownNotLocalized("Model description")
        val version = ModelVersion("2.0.0")

        cmd.createModel(modelId, name, description, version)

        val reloaded = query.findModelById(modelId)
        assertEquals(name, reloaded.name)
        assertEquals(description, reloaded.description)
        assertEquals(version, reloaded.version)
    }

    @Test
    fun `create model keeps values without optional description`() {

        val runtime = createRuntime()
        val cmd: ModelCmds = runtime.cmd
        val query: ModelQueries = runtime.queries

        val modelId = ModelId("m")
        val name = LocalizedTextNotLocalized("Model without description")
        val version = ModelVersion("3.0.0")

        cmd.createModel(modelId, name, null, version)

        val reloaded = query.findModelById(modelId)
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

        val modelId = ModelId("m1")
        cmd.createModel(modelId, LocalizedTextNotLocalized("Model name"), null, ModelVersion("2.0.0"))
        val modelIdWrong = ModelId("m2")
        assertFailsWith(ModelNotFoundException::class) {
            cmd.updateModelName(
                modelIdWrong,
                LocalizedTextNotLocalized("other")
            )
        }
        assertFailsWith(ModelNotFoundException::class) {
            cmd.updateModelDescription(
                modelIdWrong,
                LocalizedTextNotLocalized("other description")
            )
        }
        assertFailsWith(ModelNotFoundException::class) { cmd.updateModelVersion(modelIdWrong, ModelVersion("3.0.0")) }
        cmd.updateModelName(modelId, LocalizedTextNotLocalized("Model name 2"))
        assertEquals(LocalizedTextNotLocalized("Model name 2"), query.findModelById(modelId).name)
    }

    class TestEnvOneModel() {
        val runtime = createRuntime()
        val cmd: ModelCmds = runtime.cmd
        val query: ModelQueries = runtime.queries
        val modelId = ModelId("m1")

        init {
            cmd.createModel(modelId, LocalizedTextNotLocalized("Model name"), null, ModelVersion("2.0.0"))
            cmd.dispatch(
                ModelCmd.CreateType(
                    modelId = modelId,
                    initializer = ModelTypeInitializer(ModelTypeId("String"), null, null)
                )
            )
        }
    }


    @Test
    fun `updates on model name persists the name`() {
        val env = TestEnvOneModel()
        env.cmd.updateModelName(env.modelId, LocalizedTextNotLocalized("Model name 2"))
        assertEquals(LocalizedTextNotLocalized("Model name 2"), env.query.findModelById(env.modelId).name)
    }

    @Test
    fun `updates on model description persists the description`() {
        val env = TestEnvOneModel()
        env.cmd.updateModelDescription(env.modelId, LocalizedTextNotLocalized("Model description 2"))
        assertEquals(LocalizedTextNotLocalized("Model description 2"), env.query.findModelById(env.modelId).description)
    }

    @Test
    fun `updates on model description to null persists the description`() {
        val env = TestEnvOneModel()
        env.cmd.updateModelDescription(env.modelId, LocalizedTextNotLocalized("Model description 2"))
        env.cmd.updateModelDescription(env.modelId, null)
        assertNull(env.query.findModelById(env.modelId).description)
    }

    @Test
    fun `updates on model version persists the version`() {
        val env = TestEnvOneModel()
        env.cmd.updateModelVersion(env.modelId, ModelVersion("4.5.6"))
        assertEquals(ModelVersion("4.5.6"), env.query.findModelById(env.modelId).version)
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
        cmd.createModel(
            id = ModelId("m-to-delete-repo-1"),
            name = LocalizedTextNotLocalized("Model to delete"),
            description = null,
            version = ModelVersion("0.0.1"),
            repositoryRef = repo2.repositoryId.ref()
        )
        cmd.createModel(
            id = ModelId("m-to-delete-repo-2"),
            name = LocalizedTextNotLocalized("Model to delete 2 on repo 2"),
            description = null,
            version = ModelVersion("0.0.1"),
            repositoryRef = repo2.repositoryId.ref()
        )
        assertThrows<ModelNotFoundException> {
            cmd.deleteModel(ModelId("m-to-delete-repo-3"))
        }
    }

    @Test
    fun `delete model removes it from storage`() {
        val repo1 = ModelRepositoryInMemory("repo1")
        val repo2 = ModelRepositoryInMemory("repo2")
        val runtime = createRuntime(listOf(repo1, repo2))
        val cmd: ModelCmds = runtime.cmd
        val query: ModelQueries = runtime.queries

        cmd.createModel(
            id = ModelId("m-to-delete-repo-1"),
            name = LocalizedTextNotLocalized("Model to delete"),
            description = null,
            version = ModelVersion("0.0.1"),
            repositoryRef = repo1.repositoryId.ref()
        )
        cmd.createModel(
            id = ModelId("m-to-preserve-repo-1"),
            name = LocalizedTextNotLocalized("Model to preserve"),
            description = null,
            version = ModelVersion("0.1.0"),
            repositoryRef = repo1.repositoryId.ref()
        )
        cmd.createModel(
            id = ModelId("m-to-delete-repo-2"),
            name = LocalizedTextNotLocalized("Model to delete 2 on repo 2"),
            description = null,
            version = ModelVersion("0.0.1"),
            repositoryRef = repo2.repositoryId.ref()
        )
        cmd.createModel(
            id = ModelId("m-to-preserve-repo-2"),
            name = LocalizedTextNotLocalized("Model to preserve on repo 2"),
            description = null,
            version = ModelVersion("0.1.0"),
            repositoryRef = repo2.repositoryId.ref()
        )

        cmd.deleteModel(ModelId("m-to-delete-repo-1"))
        assertNull(repo1.findModelByIdOptional(ModelId("m-to-delete-repo-1")))
        assertFailsWith<ModelNotFoundException> {
            query.findModelById(ModelId("m-to-delete-repo-1"))
        }
        assertNotNull(query.findModelById(ModelId("m-to-preserve-repo-1")))
        assertNotNull(query.findModelById(ModelId("m-to-delete-repo-2")))
        assertNotNull(query.findModelById(ModelId("m-to-preserve-repo-2")))

        cmd.deleteModel(ModelId("m-to-delete-repo-2"))
        assertNull(repo1.findModelByIdOptional(ModelId("m-to-delete-repo-2")))
        assertNotNull(query.findModelById(ModelId("m-to-preserve-repo-1")))
        assertNotNull(query.findModelById(ModelId("m-to-preserve-repo-2")))

        cmd.deleteModel(ModelId("m-to-preserve-repo-1"))
        assertNull(repo2.findModelByIdOptional(ModelId("m-to-delete-repo-2")))
        assertNotNull(query.findModelById(ModelId("m-to-preserve-repo-2")))

        cmd.deleteModel(ModelId("m-to-preserve-repo-2"))
        assertNull(repo2.findModelByIdOptional(ModelId("m-to-delete-repo-2")))

    }

    // ------------------------------------------------------------------------
    // Types
    // ------------------------------------------------------------------------

    class TestEnvTypes {
        val runtime = createRuntime()
        val cmd: ModelCmds = runtime.cmd
        val query: ModelQueries = runtime.queries
        val modelId = ModelId("m1")

        init {
            cmd.createModel(modelId, LocalizedTextNotLocalized("Model name"), null, ModelVersion("2.0.0"))
        }

        val model: Model
            get() {
                return query.findModelById(modelId)
            }
    }

    @Test
    fun `create type`() {
        val env = TestEnvTypes()
        env.cmd.dispatch(
            ModelCmd.CreateType(
                env.modelId,
                ModelTypeInitializer(
                    ModelTypeId("String"),
                    LocalizedTextNotLocalized("Simple string"),
                    LocalizedTextNotLocalized("Simple string description")
                )
            )
        )
        assertEquals(1, env.model.types.size)
        val type = env.model.findTypeOptional(ModelTypeId("String"))
        assertNotNull(type)
        assertEquals(LocalizedTextNotLocalized("Simple string"), type.name)
        assertEquals(LocalizedTextNotLocalized("Simple string description"), type.description)
    }

    @Test
    fun `create type without name and description`() {
        val env = TestEnvTypes()
        env.cmd.dispatch(ModelCmd.CreateType(env.modelId, ModelTypeInitializer(ModelTypeId("String"), null, null)))
        assertEquals(1, env.model.types.size)
        val type = env.model.findTypeOptional(ModelTypeId("String"))
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
                    ModelId("unknown"),
                    ModelTypeInitializer(ModelTypeId("String"), null, null)
                )
            )
        }
    }

    @Test
    fun `create type with duplicate name throws DuplicateTypeException`() {
        val env = TestEnvTypes()
        env.cmd.dispatch(ModelCmd.CreateType(env.modelId, ModelTypeInitializer(ModelTypeId("String"), null, null)))
        assertThrows<TypeCreateDuplicateException> {
            env.cmd.dispatch(
                ModelCmd.CreateType(
                    env.modelId,
                    ModelTypeInitializer(ModelTypeId("String"), null, null)
                )
            )
        }
    }

    @Test
    fun `update type name `() {
        val env = TestEnvTypes()
        env.cmd.dispatch(ModelCmd.CreateType(env.modelId, ModelTypeInitializer(ModelTypeId("String"), null, null)))
        env.cmd.dispatch(
            ModelCmd.UpdateType(
                env.modelId,
                ModelTypeId("String"),
                ModelTypeUpdateCmd.Name(LocalizedTextNotLocalized("This is a string"))
            )
        )
        val t = env.model.findTypeOptional(ModelTypeId("String"))
        assertNotNull(t)
        assertEquals(LocalizedTextNotLocalized("This is a string"), t.name)
    }

    @Test
    fun `update type name with null`() {
        val env = TestEnvTypes()
        env.cmd.dispatch(ModelCmd.CreateType(env.modelId, ModelTypeInitializer(ModelTypeId("String"), null, null)))
        env.cmd.dispatch(ModelCmd.UpdateType(env.modelId, ModelTypeId("String"), ModelTypeUpdateCmd.Name(null)))
        val t = env.model.findTypeOptional(ModelTypeId("String"))
        assertNotNull(t)
        assertNull(t.name)
    }

    @Test
    fun `update type description`() {
        val env = TestEnvTypes()
        env.cmd.dispatch(ModelCmd.CreateType(env.modelId, ModelTypeInitializer(ModelTypeId("String"), null, null)))
        env.cmd.dispatch(
            ModelCmd.UpdateType(
                env.modelId,
                ModelTypeId("String"),
                ModelTypeUpdateCmd.Description(LocalizedTextNotLocalized("This is a string"))
            )
        )
        val t = env.model.findTypeOptional(ModelTypeId("String"))
        assertNotNull(t)
        assertEquals(LocalizedTextNotLocalized("This is a string"), t.description)
    }

    @Test
    fun `update type description with null`() {
        val env = TestEnvTypes()
        env.cmd.dispatch(ModelCmd.CreateType(env.modelId, ModelTypeInitializer(ModelTypeId("String"), null, null)))
        env.cmd.dispatch(ModelCmd.UpdateType(env.modelId, ModelTypeId("String"), ModelTypeUpdateCmd.Description(null)))
        val t = env.model.findTypeOptional(ModelTypeId("String"))
        assertNotNull(t)
        assertNull(t.description)
    }

    @Test
    fun `update type with model not found`() {
        val env = TestEnvTypes()
        env.cmd.dispatch(ModelCmd.CreateType(env.modelId, ModelTypeInitializer(ModelTypeId("String"), null, null)))
        assertThrows<ModelNotFoundException> {
            env.cmd.dispatch(
                ModelCmd.UpdateType(
                    ModelId("unknown"),
                    ModelTypeId("String"),
                    ModelTypeUpdateCmd.Description(null)
                )
            )
        }
    }

    @Test
    fun `update type with type not found`() {
        val env = TestEnvTypes()
        env.cmd.dispatch(ModelCmd.CreateType(env.modelId, ModelTypeInitializer(ModelTypeId("String"), null, null)))
        assertThrows<TypeNotFoundException> {
            env.cmd.dispatch(
                ModelCmd.UpdateType(
                    env.modelId,
                    ModelTypeId("String2"),
                    ModelTypeUpdateCmd.Description(null)
                )
            )
        }
    }

    @Test
    fun `delete type model not found`() {
        val env = TestEnvTypes()
        env.cmd.dispatch(ModelCmd.CreateType(env.modelId, ModelTypeInitializer(ModelTypeId("String"), null, null)))
        assertThrows<ModelNotFoundException> {
            env.cmd.dispatch(ModelCmd.DeleteType(ModelId("unknown"), ModelTypeId("String")))
        }
    }

    @Test
    fun `delete type type not found`() {
        val env = TestEnvTypes()
        env.cmd.dispatch(ModelCmd.CreateType(env.modelId, ModelTypeInitializer(ModelTypeId("String"), null, null)))
        assertThrows<TypeNotFoundException> {
            env.cmd.dispatch(ModelCmd.DeleteType(env.modelId, ModelTypeId("String2")))
        }
    }

    @Test
    fun `delete type used in attributes then error`() {
        val env = TestEnvTypes()
        env.cmd.dispatch(ModelCmd.CreateType(env.modelId, ModelTypeInitializer(ModelTypeId("String"), null, null)))
        env.cmd.dispatch(ModelCmd.CreateType(env.modelId, ModelTypeInitializer(ModelTypeId("Markdown"), null, null)))
        env.cmd.dispatch(ModelCmd.CreateType(env.modelId, ModelTypeInitializer(ModelTypeId("Int"), null, null)))
        env.cmd.dispatch(
            ModelCmd.CreateEntityDef(
                env.modelId, EntityDefInitializer(
                    entityDefId = EntityDefId("contact"), name = null, description = null,
                    identityAttribute = AttributeDefIdentityInitializer(
                        attributeDefId = AttributeDefId("name"),
                        type = ModelTypeId("String"),
                        name = null,
                        description = null
                    )
                )
            )
        )
        env.cmd.dispatch(
            ModelCmd.CreateEntityDefAttributeDef(
                modelId = env.modelId,
                entityDefId = EntityDefId("contact"),
                attributeDefInitializer = AttributeDefInitializer(
                    attributeDefId = AttributeDefId("infos"),
                    type = ModelTypeId("Markdown"),
                    optional = false, name = null, description = null
                )
            )
        )
        assertThrows<ModelTypeDeleteUsedException> {
            env.cmd.dispatch(ModelCmd.DeleteType(env.modelId, ModelTypeId("String")))
        }
        assertThrows<ModelTypeDeleteUsedException> {
            env.cmd.dispatch(ModelCmd.DeleteType(env.modelId, ModelTypeId("Markdown")))
        }
        env.cmd.dispatch(ModelCmd.DeleteType(env.modelId, ModelTypeId("Int")))

    }

    @Test
    fun `delete type success`() {
        val env = TestEnvTypes()
        env.cmd.dispatch(ModelCmd.CreateType(env.modelId, ModelTypeInitializer(ModelTypeId("String"), null, null)))
        env.cmd.dispatch(ModelCmd.CreateType(env.modelId, ModelTypeInitializer(ModelTypeId("Markdown"), null, null)))
        env.cmd.dispatch(ModelCmd.CreateType(env.modelId, ModelTypeInitializer(ModelTypeId("Int"), null, null)))
        env.cmd.dispatch(ModelCmd.DeleteType(env.modelId, ModelTypeId("Int")))
        assertNull(env.model.findTypeOptional(ModelTypeId("Int")))
        assertNotNull(env.model.findTypeOptional(ModelTypeId("String")))
        assertNotNull(env.model.findTypeOptional(ModelTypeId("Markdown")))

    }

    // ------------------------------------------------------------------------
    // Entities
    // ------------------------------------------------------------------------

    @Test
    fun `create entity then id name and description shall persist`() {
        val env = TestEnvOneModel()
        val entityId = EntityDefId("entity")
        val name = LocalizedTextNotLocalized("Order")
        val description = LocalizedMarkdownNotLocalized("Order description")

        env.cmd.dispatch(
            ModelCmd.CreateEntityDef(
                env.modelId, EntityDefInitializer(
                    entityDefId = entityId,
                    name = name,
                    description = description,
                    identityAttribute = AttributeDefIdentityInitializer(
                        attributeDefId = AttributeDefId("id"),
                        type = ModelTypeId("String"),
                        name = LocalizedTextNotLocalized("Identifier"),
                        description = LocalizedTextNotLocalized("Identifier description")
                    )
                )
            )
        )

        val reloaded = env.query.findModelById(env.modelId).findEntityDef(entityId)
        assertEquals(entityId, reloaded.id)
        assertEquals(name, reloaded.name)
        assertEquals(description, reloaded.description)
        assertEquals(1, reloaded.attributes.size)
        val attrId = reloaded.attributes[0]
        assertEquals(AttributeDefId("id"), attrId.id)
        assertEquals("Identifier", attrId.name?.name)
        assertEquals("Identifier description", attrId.description?.name)
    }

    @Test
    fun `create entity with null name then name shall be null`() {
        val env = TestEnvOneModel()
        val entityId = EntityDefId("entity-null-name")
        val description = LocalizedMarkdownNotLocalized("Entity without name")

        env.cmd.dispatch(
            ModelCmd.CreateEntityDef(
                env.modelId, EntityDefInitializer(
                    entityId, null, description,
                    AttributeDefIdentityInitializer(
                        attributeDefId = AttributeDefId("id"),
                        type = ModelTypeId("String"),
                        name = null,
                        description = null
                    )
                )
            )
        )

        val reloaded = env.query.findModelById(env.modelId).findEntityDef(entityId)
        assertEquals(entityId, reloaded.id)
        assertNull(reloaded.name)
        assertEquals(description, reloaded.description)
        assertEquals(1, reloaded.attributes.size)
    }

    @Test
    fun `create entity with null description then description shall be null`() {
        val env = TestEnvOneModel()
        val entityId = EntityDefId("entity-null-description")
        val name = LocalizedTextNotLocalized("Entity without description")

        env.cmd.dispatch(
            ModelCmd.CreateEntityDef(
                env.modelId,
                EntityDefInitializer(
                    entityDefId = entityId,
                    name = name,
                    description = null,
                    identityAttribute = AttributeDefIdentityInitializer(
                        attributeDefId = AttributeDefId("String"),
                        type = ModelTypeId("String"),
                        name = null,
                        description = null
                    )
                )
            )
        )

        val reloaded = env.query.findModelById(env.modelId).findEntityDef(entityId)
        assertEquals(entityId, reloaded.id)
        assertEquals(name, reloaded.name)
        assertNull(reloaded.description)
    }

    @Test
    fun `create entity with null attribute name and description then name and desc shall be null`() {
        val env = TestEnvOneModel()
        val entityId = EntityDefId("entity-null-attr-name")
        val description = LocalizedMarkdownNotLocalized("Entity without name")

        env.cmd.dispatch(
            ModelCmd.CreateEntityDef(
                env.modelId, EntityDefInitializer(
                    entityId, null, description,
                    AttributeDefIdentityInitializer(
                        attributeDefId = AttributeDefId("id"),
                        type = ModelTypeId("String"),
                        name = null,
                        description = null
                    )
                )
            )
        )

        val reloaded = env.query.findModelById(env.modelId).findEntityDef(entityId)
        assertNull(reloaded.attributes[0].name)
        assertNull(reloaded.attributes[0].description)
    }

    class TestEnvEntityUpdate {
        val runtime = createRuntime()
        val cmd: ModelCmds = runtime.cmd
        val query: ModelQueries = runtime.queries
        val modelId = ModelId("model-entity-update")
        val primaryEntityId = EntityDefId("entity-primary")
        val secondaryEntityId = EntityDefId("entity-secondary")

        init {
            cmd.createModel(
                modelId,
                LocalizedTextNotLocalized("Model entity update"),
                null,
                ModelVersion("1.0.0")
            )
            cmd.dispatch(ModelCmd.CreateType(modelId, ModelTypeInitializer(ModelTypeId("String"), null, null)))
            cmd.dispatch(
                ModelCmd.CreateEntityDef(
                    modelId,
                    EntityDefInitializer(
                        entityDefId = primaryEntityId,
                        name = LocalizedTextNotLocalized("Entity primary"),
                        description = LocalizedMarkdownNotLocalized("Entity primary description"),
                        identityAttribute = AttributeDefIdentityInitializer(
                            attributeDefId = AttributeDefId("id"),
                            type = ModelTypeId("String"),
                            name = null,
                            description = null
                        )
                    )
                )
            )
            cmd.dispatch(
                ModelCmd.CreateEntityDef(
                    modelId,
                    EntityDefInitializer(
                        secondaryEntityId,
                        LocalizedTextNotLocalized("Entity secondary"),
                        LocalizedMarkdownNotLocalized("Entity secondary description"),
                        identityAttribute = AttributeDefIdentityInitializer(
                            attributeDefId = AttributeDefId("id"),
                            type = ModelTypeId("String"),
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
        val wrongModelId = ModelId("unknown-model")

        assertFailsWith<ModelNotFoundException> {
            env.cmd.dispatch(
                ModelCmd.UpdateEntityDef(
                    wrongModelId,
                    env.primaryEntityId,
                    EntityDefUpdateCmd.Name(LocalizedTextNotLocalized("Updated name"))
                )
            )
        }
    }

    @Test
    fun `update entity with wrong entity id throws EntityDefNotFoundException`() {
        val env = TestEnvEntityUpdate()
        val wrongEntityId = EntityDefId("unknown-entity")

        assertFailsWith<EntityDefNotFoundException> {
            env.cmd.dispatch(
                ModelCmd.UpdateEntityDef(
                    env.modelId,
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
                    env.modelId,
                    env.primaryEntityId,
                    EntityDefUpdateCmd.Id(duplicateId)
                )
            )
        }
    }

    @Test
    fun `update entity id with correct id ok`() {
        val env = TestEnvEntityUpdate()
        val newId = EntityDefId("entity-renamed")

        env.cmd.dispatch(
            ModelCmd.UpdateEntityDef(
                env.modelId,
                env.primaryEntityId,
                EntityDefUpdateCmd.Id(newId)
            )
        )

        val reloaded = env.query.findModelById(env.modelId)
        assertNull(reloaded.findEntityDefOptional(env.primaryEntityId))
        assertNotNull(reloaded.findEntityDefOptional(newId))
    }

    @Test
    fun `update entity name not null persisted`() {
        val env = TestEnvEntityUpdate()
        val newName = LocalizedTextNotLocalized("Entity primary updated")

        env.cmd.dispatch(
            ModelCmd.UpdateEntityDef(
                env.modelId,
                env.primaryEntityId,
                EntityDefUpdateCmd.Name(newName)
            )
        )

        val reloaded = env.query.findModelById(env.modelId).findEntityDef(env.primaryEntityId)
        assertEquals(newName, reloaded.name)
    }

    @Test
    fun `update entity name null then name is null`() {
        val env = TestEnvEntityUpdate()

        env.cmd.dispatch(
            ModelCmd.UpdateEntityDef(
                env.modelId,
                env.primaryEntityId,
                EntityDefUpdateCmd.Name(null)
            )
        )

        val reloaded = env.query.findModelById(env.modelId).findEntityDef(env.primaryEntityId)
        assertNull(reloaded.name)
    }

    @Test
    fun `update entity description not null persisted`() {
        val env = TestEnvEntityUpdate()
        val newDescription = LocalizedMarkdownNotLocalized("Primary entity updated description")

        env.cmd.dispatch(
            ModelCmd.UpdateEntityDef(
                env.modelId,
                env.primaryEntityId,
                EntityDefUpdateCmd.Description(newDescription)
            )
        )

        val reloaded = env.query.findModelById(env.modelId).findEntityDef(env.primaryEntityId)
        assertEquals(newDescription, reloaded.description)
    }

    @Test
    fun `update entity description with null then description is null`() {
        val env = TestEnvEntityUpdate()

        env.cmd.dispatch(
            ModelCmd.UpdateEntityDef(
                env.modelId,
                env.primaryEntityId,
                EntityDefUpdateCmd.Description(null)
            )
        )

        val reloaded = env.query.findModelById(env.modelId).findEntityDef(env.primaryEntityId)
        assertNull(reloaded.description)
    }


    @Test
    fun `delete entity in model then entity removed`() {
        val env = TestEnvOneModel()
        val entityId = EntityDefId("entity-to-delete")

        env.cmd.dispatch(
            ModelCmd.CreateEntityDef(
                env.modelId, EntityDefInitializer(
                    entityDefId = entityId,
                    name = LocalizedTextNotLocalized("To delete"),
                    description = null,
                    identityAttribute = AttributeDefIdentityInitializer(
                        attributeDefId = AttributeDefId("id"),
                        type = ModelTypeId("String"),
                        name = null,
                        description = null
                    )
                )
            )
        )
        env.cmd.dispatch(ModelCmd.DeleteEntityDef(env.modelId, entityId))

        val reloaded = env.query.findModelById(env.modelId)
        assertNull(reloaded.findEntityDefOptional(entityId))
    }

    @Test
    fun `delete entity with same id in two models then only entity in the specified model is removed`() {
        val runtime = createRuntime()
        val cmd: ModelCmds = runtime.cmd
        val query: ModelQueries = runtime.queries

        val modelId1 = ModelId("model-1")
        val modelId2 = ModelId("model-2")
        val entityId = EntityDefId("shared-entity")

        cmd.createModel(
            modelId1,
            LocalizedTextNotLocalized("Model 1"),
            null,
            ModelVersion("1.0.0")
        )
        cmd.dispatch(ModelCmd.CreateType(modelId1, ModelTypeInitializer(ModelTypeId("String"), null, null)))
        cmd.createModel(
            modelId2,
            LocalizedTextNotLocalized("Model 2"),
            null,
            ModelVersion("1.0.0")
        )
        cmd.dispatch(ModelCmd.CreateType(modelId2, ModelTypeInitializer(ModelTypeId("String"), null, null)))
        cmd.dispatch(
            ModelCmd.CreateEntityDef(
                modelId1, EntityDefInitializer(
                    entityDefId = entityId, name = LocalizedTextNotLocalized("Entity"), description = null,
                    identityAttribute = AttributeDefIdentityInitializer(
                        attributeDefId = AttributeDefId("id"),
                        type = ModelTypeId("String"),
                        name = null,
                        description = null
                    )
                )
            )
        )
        cmd.dispatch(
            ModelCmd.CreateEntityDef(
                modelId2, EntityDefInitializer(
                    entityDefId = entityId, name = LocalizedTextNotLocalized("Entity"), description = null,
                    identityAttribute = AttributeDefIdentityInitializer(
                        attributeDefId = AttributeDefId("id"),
                        type = ModelTypeId("String"),
                        name = null,
                        description = null
                    )
                )
            )
        )

        cmd.dispatch(ModelCmd.DeleteEntityDef(modelId1, entityId))

        val reloadedModel1 = query.findModelById(modelId1)
        val reloadedModel2 = query.findModelById(modelId2)

        assertNull(reloadedModel1.findEntityDefOptional(entityId))
        assertNotNull(reloadedModel2.findEntityDefOptional(entityId))
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Attributes
    // -----------------------------------------------------------------------------------------------------------------

    class TestEnvAttribute() {
        val runtime = createRuntime()
        val cmd: ModelCmds = runtime.cmd
        val query: ModelQueries = runtime.queries
        val sampleModelId = ModelId("model-1")
        val sampleEntityDefId = EntityDefId("Entity1")

        init {
            cmd.createModel(
                sampleModelId,
                LocalizedTextNotLocalized("Model 1"),
                null,
                ModelVersion("1.0.0"),
            )
            cmd.dispatch(ModelCmd.CreateType(sampleModelId, ModelTypeInitializer(ModelTypeId("String"), null, null)))
        }

        fun addSampleEntityDef() {
            cmd.dispatch(
                ModelCmd.CreateEntityDef(
                    sampleModelId,
                    EntityDefInitializer(
                        entityDefId = sampleEntityDefId, name = null, description = null,
                        identityAttribute = AttributeDefIdentityInitializer(
                            attributeDefId = AttributeDefId("id"),
                            type = ModelTypeId("String"),
                            name = null,
                            description = null
                        )
                    )
                )
            )
        }

        fun createAttributeDef(
            attributeDefId: AttributeDefId = AttributeDefId("myattribute"),
            type: ModelTypeId = ModelTypeId("String"),
            optional: Boolean = false,
            name: LocalizedText? = null,
            description: LocalizedMarkdown? = null
        ): AttributeDef {

            cmd.dispatch(
                ModelCmd.CreateEntityDefAttributeDef(
                    modelId = sampleModelId,
                    entityDefId = sampleEntityDefId,
                    attributeDefInitializer = AttributeDefInitializer(
                        attributeDefId = attributeDefId,
                        type = type,
                        optional = optional,
                        name = name,
                        description = description,
                    )
                )
            )
            val reloaded =
                query.findModelById(sampleModelId).findEntityDef(sampleEntityDefId).getAttributeDef(attributeDefId)
            return reloaded
        }

        fun updateAttributeDef(
            attributeDefId: AttributeDefId = AttributeDefId("myattribute"),
            command: AttributeDefUpdateCmd,
            reloadId: AttributeDefId? = null
        ): AttributeDef {
            cmd.dispatch(
                ModelCmd.UpdateEntityDefAttributeDef(
                    sampleModelId,
                    sampleEntityDefId,
                    attributeDefId,
                    command
                )
            )
            val reloaded = query.findModelById(sampleModelId).findEntityDef(sampleEntityDefId)
                .getAttributeDef(reloadId ?: attributeDefId)
            return reloaded
        }

    }

    @Test
    fun `create attribute then id name and description shall persist`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        val reloaded = env.createAttributeDef(
            attributeDefId = AttributeDefId("businesskey"),
            type = ModelTypeId("String"),
            optional = false,
            name = LocalizedTextNotLocalized("Business Key"),
            description = LocalizedTextNotLocalized("Unique business key"),
        )
        assertEquals(AttributeDefId("businesskey"), reloaded.id)
        assertEquals(LocalizedTextNotLocalized("Business Key"), reloaded.name)
        assertEquals(LocalizedTextNotLocalized("Unique business key"), reloaded.description)
        assertEquals(ModelTypeId("String"), reloaded.type)
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
                env.sampleModelId,
                ModelTypeInitializer(ModelTypeId("Boolean"), null, null)
            )
        )

        val reloaded = env.createAttributeDef(type = ModelTypeId("Boolean"))
        assertEquals(ModelTypeId("Boolean"), reloaded.type)
    }


    @Test
    fun `create attribute with duplicate id then error`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        env.createAttributeDef(attributeDefId = AttributeDefId("lastname"))
        assertFailsWith<CreateAttributeDefDuplicateIdException> {
            env.createAttributeDef(attributeDefId = AttributeDefId("lastname"))
        }
    }

    @Test
    fun `create attribute unknown type then error`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        assertFailsWith<TypeNotFoundException> {
            env.createAttributeDef(attributeDefId = AttributeDefId("lastname"), type = ModelTypeId("UnknownType"))
        }
    }

    @Test
    fun `update attribute with wrong model id throws ModelNotFoundException`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        env.createAttributeDef()
        assertFailsWith<ModelNotFoundException> {
            env.cmd.dispatch(
                ModelCmd.UpdateEntityDefAttributeDef(
                    modelId = ModelId("unknown"),
                    entityDefId = EntityDefId("unknownEntity"),
                    attributeDefId = AttributeDefId("unknownAttribute"),
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
                    modelId = env.sampleModelId,
                    entityDefId = EntityDefId("unknownEntity"),
                    attributeDefId = AttributeDefId("unknownAttribute"),
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
                    modelId = env.sampleModelId,
                    entityDefId = env.sampleEntityDefId,
                    attributeDefId = AttributeDefId("unknownAttribute"),
                    cmd = AttributeDefUpdateCmd.Name(null)
                )
            )
        }
    }


    @Test
    fun `update attribute id with duplicate id throws exception`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        env.createAttributeDef(attributeDefId = AttributeDefId("lastname"))
        env.createAttributeDef(attributeDefId = AttributeDefId("firstname"))
        assertFailsWith<UpdateAttributeDefDuplicateIdException> {
            // Rename firstname to lastname causes exception because lastname already exists
            env.updateAttributeDef(
                attributeDefId = AttributeDefId("firstname"),
                AttributeDefUpdateCmd.Id(AttributeDefId("lastname"))
            )
        }
    }

    @Test
    fun `update attribute id with correct id works`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        env.createAttributeDef(AttributeDefId("lastname"))
        env.createAttributeDef(AttributeDefId("firstname"))
        val reloaded = env.updateAttributeDef(
            AttributeDefId("firstname"),
            AttributeDefUpdateCmd.Id(AttributeDefId("nextname")),
            AttributeDefId("nextname"),
        )
        assertEquals(AttributeDefId("nextname"), reloaded.id)
    }

    @Test
    fun `update attribute id of the entity identifier also changes the identifier id`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        val e = env.query.findModelById(env.sampleModelId).findEntityDef(env.sampleEntityDefId)
        val attrId = e.identifierAttributeDefId
        val attrIdNext = AttributeDefId("id_next")
        // Be careful to specify "reloadId" because the attribute's id changed
        env.updateAttributeDef(attrId, command = AttributeDefUpdateCmd.Id(attrIdNext), reloadId = attrIdNext)
        val reloadedEntity = env.query.findModelById(env.sampleModelId).findEntityDef(env.sampleEntityDefId)
        assertEquals(attrIdNext, reloadedEntity.identifierAttributeDefId)


    }

    @Test
    fun `update attribute name is persisted`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        val attr = env.createAttributeDef(name = null)
        val nextValue = LocalizedTextNotLocalized("New name")
        val reloaded = env.updateAttributeDef(attr.id, AttributeDefUpdateCmd.Name(nextValue))
        assertEquals(nextValue, reloaded.name)
    }

    @Test
    fun `update attribute name to null stays null`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        val attr = env.createAttributeDef(name = LocalizedTextNotLocalized("Name"))
        val reloaded = env.updateAttributeDef(attr.id, AttributeDefUpdateCmd.Name(null))
        assertNull(reloaded.name)
    }


    @Test
    fun `update attribute description is persisted`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        val attr = env.createAttributeDef(description = null)
        val nextValue = LocalizedTextNotLocalized("New description")
        val reloaded = env.updateAttributeDef(attr.id, AttributeDefUpdateCmd.Description(nextValue))
        assertEquals(nextValue, reloaded.description)
    }

    @Test
    fun `update attribute description to null stays null`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        val attr = env.createAttributeDef(description = LocalizedTextNotLocalized("New description"))
        val reloaded = env.updateAttributeDef(attr.id, AttributeDefUpdateCmd.Description(null))
        assertNull(reloaded.description)
    }

    @Test
    fun `update attribute type is persisted`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        val typeMarkdownId = ModelTypeId("Markdown")
        env.cmd.dispatch(
            ModelCmd.CreateType(
                env.sampleModelId,
                ModelTypeInitializer(id = typeMarkdownId, name = null, description = null)
            )
        )

        val attr = env.createAttributeDef(type = ModelTypeId("String"))

        val reloaded = env.updateAttributeDef(attr.id, AttributeDefUpdateCmd.Type(typeMarkdownId))
        assertEquals(typeMarkdownId, reloaded.type)
    }

    @Test
    fun `update attribute unknown type then error`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        val attr = env.createAttributeDef(type = ModelTypeId("String"))
        assertThrows<TypeNotFoundException> {
            env.cmd.dispatch(
                ModelCmd.UpdateEntityDefAttributeDef(
                    env.sampleModelId,
                    env.sampleEntityDefId,
                    attr.id,
                    AttributeDefUpdateCmd.Type(ModelTypeId("String2"))
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
        val reloaded = env.updateAttributeDef(attr.id, AttributeDefUpdateCmd.Optional(nextValue))
        assertEquals(nextValue, reloaded.optional)
    }

    @Test
    fun `update attribute optional false to true is persisted`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        val attr = env.createAttributeDef(optional = false)
        val nextValue = true
        val reloaded = env.updateAttributeDef(attr.id, AttributeDefUpdateCmd.Optional(nextValue))
        assertEquals(nextValue, reloaded.optional)
    }

    @Test
    fun `delete entity attribute in model then attribute removed`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        env.createAttributeDef(attributeDefId = AttributeDefId("bk"))
        env.createAttributeDef(attributeDefId = AttributeDefId("firstname"))
        env.createAttributeDef(attributeDefId = AttributeDefId("lastname"))
        env.cmd.dispatch(
            ModelCmd.DeleteEntityDefAttributeDef(
                env.sampleModelId,
                env.sampleEntityDefId,
                AttributeDefId("firstname")
            )
        )
        val reloaded = env.query.findModelById(env.sampleModelId).findEntityDef(env.sampleEntityDefId)

        assertTrue(reloaded.hasAttributeDef(AttributeDefId("bk")))
        assertFalse(reloaded.hasAttributeDef(AttributeDefId("firstname")))
        assertTrue(reloaded.hasAttributeDef(AttributeDefId("lastname")))

    }

    @Test
    fun `delete entity attribute used as identifier throws error`() {
        val env = TestEnvAttribute()
        env.addSampleEntityDef()
        env.createAttributeDef(attributeDefId = AttributeDefId("bk"))
        env.createAttributeDef(attributeDefId = AttributeDefId("firstname"))
        env.createAttributeDef(attributeDefId = AttributeDefId("lastname"))

        val reloaded = env.query.findModelById(env.sampleModelId).findEntityDef(env.sampleEntityDefId)
        assertThrows<DeleteAttributeIdentifierException> {
            env.cmd.dispatch(
                ModelCmd.DeleteEntityDefAttributeDef(
                    env.sampleModelId,
                    env.sampleEntityDefId,
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
        val modelId = ModelId("test")
        val invalidModel = ModelInMemory(
            id = modelId,
            name = null,
            description = null,
            version = ModelVersion("0.0.1"),
            types = listOf(ModelTypeInMemory(id = ModelTypeId("String"), name = null, description = null)),
            entityDefs = listOf(
                EntityDefInMemory(
                    id = EntityDefId("Contact"),
                    name = null,
                    description = null,
                    // Error is here
                    identifierAttributeDefId = AttributeDefId("unknown"),
                    attributes = listOf(
                        AttributeDefInMemory(
                            id = AttributeDefId("id"),
                            type = ModelTypeId("String"),
                            name = null,
                            description = null,
                            optional = false
                        )
                    )
                )
            ),
            relationshipDefs = emptyList()
        )

        init {
            runtime.repositories.first().push(invalidModel)
        }
    }

    @Test
    fun `can not load model with errors`() {

        // This test only checks loading and basic behaviour of model operations

        // Each method that need checking shall be checked independently
        // as some methods can effectively work on invalid models (for example to be able
        // to correct them)

        val env = TestEnvInvalidModel()

        // Getting a model that has error shall fail with invalid exception
        assertThrows<ModelInvalidException> { env.query.findModelById(env.modelId) }

        // Find all model ids shall not validate models, just give their ids
        assertDoesNotThrow { env.query.findAllModelIds() }

        // Creating or trying to modify something in invalid model shall throw error
        assertThrows<ModelInvalidException> {
            env.cmd.dispatch(
                ModelCmd.CreateType(
                    env.modelId,
                    ModelTypeInitializer(ModelTypeId("Markdown"), null, null)
                )
            )
        }


    }

}
