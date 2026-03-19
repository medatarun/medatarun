package io.medatarun.model.actions

import io.medatarun.model.domain.*
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.fixtures.ModelTestEnv
import io.medatarun.model.ports.exposed.ModelQueries


fun createEnv(): ModelTestEnv {
    return ModelTestEnv()
}


class TestEnvOneModel(version: ModelVersion = ModelVersion("2.0.0")) {
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
                key = modelKey,
                name = LocalizedTextNotLocalized("Model name"),
                description = null,
                version = version
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


class TestEnvTypes {
    val runtime = createEnv()
    val query: ModelQueries = runtime.queries
    private val modelKey = ModelKey("m1")
    val modelRef = modelRefKey(ModelKey("m1"))
    val dispatch = runtime::dispatch

    init {
        runtime.dispatch(
            ModelAction.Model_Create(
                key = modelKey,
                name = LocalizedTextNotLocalized("Model name"),
                description = null,
                version = ModelVersion("2.0.0")
            )
        )
    }

    val model: ModelAggregate
        get() {
            return query.findModelByKey(modelKey)
        }
}

class TestEnvEntityAttribute {
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

    fun reloadAttribute(
        attributeRef: EntityAttributeRef,
        reloadId: EntityAttributeRef? = null
    ): Attribute {
        val reloaded = query.findEntityAttribute(sampleModelRef, sampleEntityRef, reloadId ?: attributeRef)
        return reloaded
    }

}

class TestEnvRelationshipRole {
    val runtime = createEnv()
    val dispatch = runtime::dispatch
    val query: ModelQueries = runtime.queries

    private val modelKey = ModelKey("model-relationship-role")
    val modelRef = modelRefKey(modelKey)

    val primaryEntityKey = EntityKey("entity-a")
    val primaryEntityRef = EntityRef.ByKey(primaryEntityKey)
    val secondaryEntityKey = EntityKey("entity-b")
    val secondaryEntityRef = EntityRef.ByKey(secondaryEntityKey)

    val relationshipKey = RelationshipKey("works-with")
    val relationshipRef = RelationshipRef.ByKey(relationshipKey)
    val roleAKey = RelationshipRoleKey("buyer")
    val roleARef = RelationshipRoleRef.ByKey(roleAKey)
    val roleBKey = RelationshipRoleKey("purchase")
    val roleBRef = RelationshipRoleRef.ByKey(roleBKey)

    init {
        runtime.dispatch(
            ModelAction.Model_Create(
                key = modelKey,
                name = LocalizedTextNotLocalized("Model relationship role"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )
        runtime.dispatch(ModelAction.Type_Create(modelRef, TypeKey("String"), null, null))
        runtime.dispatch(
            ModelAction.Entity_Create(
                modelRef = modelRef,
                entityKey = primaryEntityKey,
                name = null,
                description = null,
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
                name = null,
                description = null,
                identityAttributeKey = AttributeKey("id"),
                identityAttributeType = typeRef("String"),
                identityAttributeName = null,
                documentationHome = null
            )
        )
        runtime.dispatch(
            ModelAction.Relationship_Create(
                modelRef = modelRef,
                relationshipKey = relationshipKey,
                name = null,
                description = null,
                roleAKey = roleAKey,
                roleAEntityRef = primaryEntityRef,
                roleAName = null,
                roleACardinality = RelationshipCardinality.One,
                roleBKey = roleBKey,
                roleBEntityRef = secondaryEntityRef,
                roleBName = null,
                roleBCardinality = RelationshipCardinality.Many
            )
        )
    }
}
