package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.domain.*
import io.medatarun.model.domain.TypeKey
import io.medatarun.model.domain.fixtures.ModelTestEnv
import io.medatarun.tags.core.actions.TagAction
import io.medatarun.tags.core.domain.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@EnableDatabaseTests
class ModelAndTag_Event_Test {

    private class CookingModelSetup(env: ModelTestEnv) {
        val modelRef: ModelRef = ModelRef.modelRefKey("cooking")
        val ingredientRef: EntityRef = EntityRef.ByKey(EntityKey("ingredient"))
        val recipeRef: EntityRef = EntityRef.ByKey(EntityKey("recipe"))
        val usageRef: RelationshipRef = RelationshipRef.ByKey(RelationshipKey("usage"))
        val recipeDescriptionRef: EntityAttributeRef = EntityAttributeRef.ByKey(AttributeKey("description"))
        val usageQuantityRef: RelationshipAttributeRef = RelationshipAttributeRef.ByKey(AttributeKey("quantity"))

        init {
            env.dispatch(
                ModelAction.Model_Create(
                    key = ModelKey("cooking"),
                    name = TextSingleLine("Cooking"),
                    description = null,
                    version = ModelVersion("1.0.0")
                )
            )
            env.dispatch(ModelAction.Type_Create(modelRef, TypeKey("String"), null, null))
            env.dispatch(
                ModelAction.Entity_Create(
                    modelRef = modelRef,
                    entityKey = EntityKey("ingredient"),
                    name = null,
                    description = null,
                    documentationHome = null
                )
            )
            env.dispatch(
                ModelAction.EntityAttribute_Create(
                    modelRef = modelRef,
                    entityRef = ingredientRef,
                    attributeKey = AttributeKey("name"),
                    type = TypeRef.typeRefKey(TypeKey("String")),
                    optional = false,
                    name = null,
                    description = null
                )
            )
            env.dispatch(
                ModelAction.EntityAttribute_Create(
                    modelRef = modelRef,
                    entityRef = ingredientRef,
                    attributeKey = AttributeKey("code"),
                    type = TypeRef.typeRefKey(TypeKey("String")),
                    optional = false,
                    name = null,
                    description = null
                )
            )
            env.dispatch(
                ModelAction.Entity_Create(
                    modelRef = modelRef,
                    entityKey = EntityKey("recipe"),
                    name = null,
                    description = null,
                    documentationHome = null
                )
            )
            env.dispatch(
                ModelAction.EntityAttribute_Create(
                    modelRef = modelRef,
                    entityRef = recipeRef,
                    attributeKey = AttributeKey("description"),
                    type = TypeRef.typeRefKey(TypeKey("String")),
                    optional = false,
                    name = null,
                    description = null
                )
            )
            env.dispatch(
                ModelAction.Relationship_Create(
                    modelRef = modelRef,
                    relationshipKey = RelationshipKey("usage"),
                    name = null,
                    description = null,
                    roleAKey = RelationshipRoleKey("ingredient"),
                    roleAEntityRef = ingredientRef,
                    roleAName = null,
                    roleACardinality = RelationshipCardinality.Many,
                    roleBKey = RelationshipRoleKey("recipe"),
                    roleBEntityRef = recipeRef,
                    roleBName = null,
                    roleBCardinality = RelationshipCardinality.Many
                )
            )
            env.dispatch(
                ModelAction.RelationshipAttribute_Create(
                    modelRef = modelRef,
                    relationshipRef = usageRef,
                    attributeKey = AttributeKey("quantity"),
                    type = TypeRef.typeRefKey(TypeKey("String")),
                    optional = false,
                    name = null,
                    description = null
                )
            )
            env.dispatch(
                ModelAction.RelationshipAttribute_Create(
                    modelRef = modelRef,
                    relationshipRef = usageRef,
                    attributeKey = AttributeKey("unit"),
                    type = TypeRef.typeRefKey(TypeKey("String")),
                    optional = false,
                    name = null,
                    description = null
                )
            )
        }
    }

    private class CrmModelSetup(env: ModelTestEnv) {
        val modelRef: ModelRef = ModelRef.modelRefKey("crm")
        val personRef: EntityRef = EntityRef.ByKey(EntityKey("person"))
        val companyRef: EntityRef = EntityRef.ByKey(EntityKey("company"))
        val employmentRef: RelationshipRef = RelationshipRef.ByKey(RelationshipKey("employment"))
        val personEmailRef: EntityAttributeRef = EntityAttributeRef.ByKey(AttributeKey("email"))
        val employmentSinceRef: RelationshipAttributeRef = RelationshipAttributeRef.ByKey(AttributeKey("since"))

        init {
            env.dispatch(
                ModelAction.Model_Create(
                    key = ModelKey("crm"),
                    name = TextSingleLine("CRM"),
                    description = null,
                    version = ModelVersion("1.0.0")
                )
            )
            env.dispatch(ModelAction.Type_Create(modelRef, TypeKey("String"), null, null))
            env.dispatch(
                ModelAction.Entity_Create(
                    modelRef = modelRef,
                    entityKey = EntityKey("person"),
                    name = null,
                    description = null,
                    documentationHome = null
                )
            )
            env.dispatch(
                ModelAction.EntityAttribute_Create(
                    modelRef = modelRef,
                    entityRef = personRef,
                    attributeKey = AttributeKey("name"),
                    type = TypeRef.typeRefKey(TypeKey("String")),
                    optional = false,
                    name = null,
                    description = null
                )
            )
            env.dispatch(
                ModelAction.EntityAttribute_Create(
                    modelRef = modelRef,
                    entityRef = personRef,
                    attributeKey = AttributeKey("email"),
                    type = TypeRef.typeRefKey(TypeKey("String")),
                    optional = false,
                    name = null,
                    description = null
                )
            )
            env.dispatch(
                ModelAction.Entity_Create(
                    modelRef = modelRef,
                    entityKey = EntityKey("company"),
                    name = null,
                    description = null,
                    documentationHome = null
                )
            )
            env.dispatch(
                ModelAction.EntityAttribute_Create(
                    modelRef = modelRef,
                    entityRef = companyRef,
                    attributeKey = AttributeKey("name"),
                    type = TypeRef.typeRefKey(TypeKey("String")),
                    optional = false,
                    name = null,
                    description = null
                )
            )
            env.dispatch(
                ModelAction.EntityAttribute_Create(
                    modelRef = modelRef,
                    entityRef = companyRef,
                    attributeKey = AttributeKey("website"),
                    type = TypeRef.typeRefKey(TypeKey("String")),
                    optional = false,
                    name = null,
                    description = null
                )
            )
            env.dispatch(
                ModelAction.Relationship_Create(
                    modelRef = modelRef,
                    relationshipKey = RelationshipKey("employment"),
                    name = null,
                    description = null,
                    roleAKey = RelationshipRoleKey("person"),
                    roleAEntityRef = personRef,
                    roleAName = null,
                    roleACardinality = RelationshipCardinality.One,
                    roleBKey = RelationshipRoleKey("company"),
                    roleBEntityRef = companyRef,
                    roleBName = null,
                    roleBCardinality = RelationshipCardinality.Many
                )
            )
            env.dispatch(
                ModelAction.RelationshipAttribute_Create(
                    modelRef = modelRef,
                    relationshipRef = employmentRef,
                    attributeKey = AttributeKey("since"),
                    type = TypeRef.typeRefKey(TypeKey("String")),
                    optional = false,
                    name = null,
                    description = null
                )
            )
        }
    }

    /**
     * Verifies that deleting one global tag removes only that tag from every model location where it was attached.
     *
     * The test deliberately spreads the same tag across several places and across two models, then keeps other tags
     * in place so we can detect accidental cleanup on unrelated data.
     */
    @Test
    fun `delete a global tag removes it from every model location`() {
        val env = ModelTestEnv()
        val cooking = CookingModelSetup(env)
        val crm = CrmModelSetup(env)

        val publicVisibilityTag = env.createGlobalTag("security", "public")
        val personalDataTag = env.createGlobalTag("gdpr", "personal-data")
        env.dispatch(ModelAction.Model_AddTag(cooking.modelRef, personalDataTag.ref))
        env.dispatch(ModelAction.Entity_AddTag(cooking.modelRef, cooking.recipeRef, personalDataTag.ref))
        env.dispatch(
            ModelAction.EntityAttribute_AddTag(
                cooking.modelRef,
                cooking.recipeRef,
                cooking.recipeDescriptionRef,
                personalDataTag.ref
            )
        )
        env.dispatch(
            ModelAction.Relationship_AddTag(
                cooking.modelRef,
                cooking.usageRef,
                personalDataTag.ref
            )
        )
        env.dispatch(
            ModelAction.RelationshipAttribute_AddTag(
                cooking.modelRef,
                cooking.usageRef,
                cooking.usageQuantityRef,
                personalDataTag.ref
            )
        )
        env.dispatch(ModelAction.Model_AddTag(crm.modelRef, personalDataTag.ref))
        env.dispatch(ModelAction.Entity_AddTag(crm.modelRef, crm.personRef, personalDataTag.ref))

        env.dispatch(ModelAction.Entity_AddTag(cooking.modelRef, cooking.recipeRef, publicVisibilityTag.ref))
        // Delete the tag through the tag module so the model-side cleanup is triggered by the event bridge.
        env.dispatchTag(TagAction.TagGlobalDelete(personalDataTag.ref))

        // Only the removed tag disappears. Tags that were not part of the deletion stay attached.
        env.replayWithRebuild {
            assertEquals(emptyList(), env.queries.findModelAggregate(cooking.modelRef).tags)
            assertEquals(listOf(publicVisibilityTag.id), env.queries.findModelAggregate(cooking.modelRef).findEntity(cooking.recipeRef).tags)
            assertEquals(
                emptyList(),
                env.queries.findEntityAttribute(
                    cooking.modelRef,
                    cooking.recipeRef,
                    cooking.recipeDescriptionRef
                ).tags
            )
            assertEquals(emptyList(), env.queries.findModelAggregate(cooking.modelRef).findRelationship(cooking.usageRef).tags)
            assertEquals(
                emptyList(),
                env.queries.findModelAggregate(cooking.modelRef)
                    .findRelationshipAttributeOptional(cooking.usageRef, cooking.usageQuantityRef)!!
                    .tags
            )

            assertEquals(emptyList(), env.queries.findModelAggregate(crm.modelRef).tags)
            assertEquals(emptyList(), env.queries.findModelAggregate(crm.modelRef).findEntity(crm.personRef).tags)
        }
    }

    @Test
    fun `delete a global tag removes model-level tags after rebuild`() {
        val env = ModelTestEnv()
        val cookingRef = ModelRef.modelRefKey("cooking-rebuild")
        val crmRef = ModelRef.modelRefKey("crm-rebuild")
        env.dispatch(
            ModelAction.Model_Create(
                key = ModelKey("cooking-rebuild"),
                name = TextSingleLine("Cooking rebuild"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )
        env.dispatch(
            ModelAction.Model_Create(
                key = ModelKey("crm-rebuild"),
                name = TextSingleLine("CRM rebuild"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )

        val personalDataTag = env.createGlobalTag("gdpr-rebuild", "personal-data")
        val publicVisibilityTag = env.createGlobalTag("security-rebuild", "public")
        env.dispatch(ModelAction.Model_AddTag(cookingRef, personalDataTag.ref))
        env.dispatch(ModelAction.Model_AddTag(cookingRef, publicVisibilityTag.ref))
        env.dispatch(ModelAction.Model_AddTag(crmRef, personalDataTag.ref))

        env.dispatchTag(TagAction.TagGlobalDelete(personalDataTag.ref))

        env.replayWithRebuild {
            assertEquals(listOf(publicVisibilityTag.id), env.queries.findModelAggregate(cookingRef).tags)
            assertEquals(emptyList(), env.queries.findModelAggregate(crmRef).tags)
        }
    }

    /**
     * Verifies that deleting a group cascades to all global tags from that group and only those tags.
     *
     * The setup uses two global tags from the same group and one unrelated tag from another group to prove that
     * cleanup is scoped to the deleted group only.
     */
    @Test
    fun `delete a tag group removes its global tags from all model locations`() {
        val env = ModelTestEnv()
        val cooking = CookingModelSetup(env)
        val crm = CrmModelSetup(env)

        val groupKey = TagGroupKey("gdpr")
        val personalDataTagKey = TagKey("personal-data")
        val specialCategoryDataTagKey = TagKey("special-category-data")

        env.dispatchTag(TagAction.TagGroupCreate(groupKey, null, null))
        env.dispatchTag(TagAction.TagGlobalCreate(TagGroupRef.ByKey(groupKey), personalDataTagKey, null, null))
        env.dispatchTag(TagAction.TagGlobalCreate(TagGroupRef.ByKey(groupKey), specialCategoryDataTagKey, null, null))

        val personalDataTagRef = TagRef.ByKey(TagScopeRef.Global, groupKey, personalDataTagKey)
        val specialCategoryDataTagRef = TagRef.ByKey(TagScopeRef.Global, groupKey, specialCategoryDataTagKey)
        val personalDataTag = env.tagQueries.findTagByRef(personalDataTagRef)
        val specialCategoryDataTag = env.tagQueries.findTagByRef(specialCategoryDataTagRef)

        val securityPublicTag = env.createGlobalTag("security", "public")

        env.dispatch(ModelAction.Model_AddTag(cooking.modelRef, personalDataTag.ref))
        env.dispatch(ModelAction.Entity_AddTag(cooking.modelRef, cooking.recipeRef, personalDataTag.ref))
        env.dispatch(
            ModelAction.EntityAttribute_AddTag(
                cooking.modelRef,
                cooking.recipeRef,
                cooking.recipeDescriptionRef,
                specialCategoryDataTag.ref
            )
        )
        env.dispatch(
            ModelAction.RelationshipAttribute_AddTag(
                crm.modelRef,
                crm.employmentRef,
                crm.employmentSinceRef,
                specialCategoryDataTag.ref
            )
        )
        env.dispatch(ModelAction.Relationship_AddTag(crm.modelRef, crm.employmentRef, securityPublicTag.ref))

        // Group deletion should emit a before-delete event for each global tag in the group.
        env.dispatchTag(TagAction.TagGroupDelete(TagGroupRef.ByKey(groupKey)))

        // The deleted group tags disappear everywhere, but the unrelated tag remains attached.
        assertEquals(emptyList(), env.queries.findModelAggregate(cooking.modelRef).tags)
        assertEquals(emptyList(), env.queries.findModelAggregate(cooking.modelRef).findEntity(cooking.recipeRef).tags)
        assertEquals(
            emptyList(),
            env.queries.findModelAggregate(cooking.modelRef).findEntityAttribute(
                cooking.recipeRef,
                cooking.recipeDescriptionRef
            ).tags
        )
        assertEquals(
            listOf(securityPublicTag.id),
            env.queries.findModelAggregate(crm.modelRef)
                .findRelationship(crm.employmentRef)
                .tags
        )
        assertEquals(
            emptyList(),
            env.queries.findModelAggregate(crm.modelRef)
                .findRelationshipAttributeOptional(crm.employmentRef, crm.employmentSinceRef)!!
                .tags
        )
    }

    /**
     * Verifies that deleting a scoped tag removes it from every place inside its model and leaves other local tags
     * untouched.
     *
     * This covers the model-scoped deletion path, which should follow the same cleanup bridge as global tags but only
     * within the owning model.
     */
    @Test
    fun `delete a scoped tag removes it from every location in the model`() {
        val env = ModelTestEnv()
        val cooking = CookingModelSetup(env)

        val draftOnlyTag = env.createLocalTagInModelScope(cooking.modelRef, "draft-only")
        val reviewedTag = env.createLocalTagInModelScope(cooking.modelRef, "reviewed")

        env.dispatch(ModelAction.Model_AddTag(cooking.modelRef, draftOnlyTag.ref))
        env.dispatch(ModelAction.Entity_AddTag(cooking.modelRef, cooking.recipeRef, draftOnlyTag.ref))
        env.dispatch(
            ModelAction.EntityAttribute_AddTag(
                cooking.modelRef,
                cooking.recipeRef,
                cooking.recipeDescriptionRef,
                draftOnlyTag.ref
            )
        )
        env.dispatch(
            ModelAction.Relationship_AddTag(
                cooking.modelRef,
                cooking.usageRef,
                draftOnlyTag.ref
            )
        )
        env.dispatch(
            ModelAction.RelationshipAttribute_AddTag(
                cooking.modelRef,
                cooking.usageRef,
                cooking.usageQuantityRef,
                draftOnlyTag.ref
            )
        )

        env.dispatch(ModelAction.Entity_AddTag(cooking.modelRef, cooking.recipeRef, reviewedTag.ref))
        env.dispatch(
            ModelAction.RelationshipAttribute_AddTag(
                cooking.modelRef,
                cooking.usageRef,
                cooking.usageQuantityRef,
                reviewedTag.ref
            )
        )

        // Deleting the scoped tag must clean every attachment inside the model and keep the sibling tag alive.
        env.dispatchTag(TagAction.TagLocalDelete(draftOnlyTag.ref))

        assertEquals(emptyList(), env.queries.findModelAggregate(cooking.modelRef).tags)
        assertEquals(listOf(reviewedTag.id), env.queries.findModelAggregate(cooking.modelRef).findEntity(cooking.recipeRef).tags)
        assertEquals(
            emptyList(),
            env.queries.findModelAggregate(cooking.modelRef).findEntityAttribute(
                cooking.recipeRef,
                cooking.recipeDescriptionRef
            ).tags
        )
        assertEquals(emptyList(), env.queries.findModelAggregate(cooking.modelRef).findRelationship(cooking.usageRef).tags)
        assertEquals(
            listOf(reviewedTag.id),
            env.queries.findModelAggregate(cooking.modelRef)
                .findRelationshipAttributeOptional(cooking.usageRef, cooking.usageQuantityRef)!!
                .tags
        )
    }
}
