package io.medatarun.model.actions

import io.medatarun.model.domain.*
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.TypeKey
import io.medatarun.model.domain.fixtures.ModelTestEnv
import io.medatarun.model.ports.exposed.ModelQueries


class TestEnvOneModel(version: ModelVersion = ModelVersion("2.0.0")) {
    private val env = ModelTestEnv()
    val runtime: ModelTestEnv
        get() = env
    val query: ModelQueries = env.queries
    private val modelKey = ModelKey("m1")
    val modelRef = modelRefKey(modelKey)
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


class TestEnvRelationshipRole {
    val runtime = ModelTestEnv()
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
            ModelAction.Entity_Create2(
                modelRef = modelRef,
                entityKey = primaryEntityKey,
                name = null,
                description = null,
                documentationHome = null
            )
        )
        runtime.dispatch(
            ModelAction.Entity_Create2(
                modelRef = modelRef,
                entityKey = secondaryEntityKey,
                name = null,
                description = null,
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
