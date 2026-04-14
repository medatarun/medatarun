package io.medatarun.model.actions

import io.medatarun.model.domain.*
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.TypeKey
import io.medatarun.model.domain.fixtures.ModelTestEnv
import io.medatarun.model.ports.needs.ModelTagResolver.Companion.modelTagScopeRef
import io.medatarun.tags.core.actions.TagAction
import io.medatarun.tags.core.domain.*

@Suppress("ClassName")
internal object refs {
    object tags {
        object global {
            object gdpr {
                val key = TagGroupKey("gdpr")
                val ref = TagGroupRef.ByKey(key)

                object personal_data {
                    val key = TagKey("personal-data")
                    val ref = TagRef.ByKey(
                        scopeRef = TagScopeRef.Global,
                        groupKey = gdpr.key,
                        key = key
                    )
                }

                object special_category_data {
                    val key = TagKey("special-category-data")
                    val ref = TagRef.ByKey(
                        scopeRef = TagScopeRef.Global,
                        groupKey = gdpr.key,
                        key = key
                    )
                }
            }

            object security {
                val key = TagGroupKey("security")
                val ref = TagGroupRef.ByKey(key)

                object public {
                    val key = TagKey("public")
                    val ref = TagRef.ByKey(
                        scopeRef = TagScopeRef.Global,
                        groupKey = security.key,
                        key = key
                    )
                }

                object internal {
                    val key = TagKey("internal")
                    val ref = TagRef.ByKey(
                        scopeRef = TagScopeRef.Global,
                        groupKey = security.key,
                        key = key
                    )
                }

                object confidential {
                    val key = TagKey("confidential")
                    val ref = TagRef.ByKey(
                        scopeRef = TagScopeRef.Global,
                        groupKey = security.key,
                        key = key
                    )
                }
            }
        }

        object local {
            object crm {
                object ui_result {
                    val key = TagKey("ui-result")
                    fun ref(modelId: ModelId): TagRef = TagRef.ByKey(
                        scopeRef = modelTagScopeRef(modelId),
                        groupKey = null,
                        key = key
                    )
                }

                object ui_search {
                    val key = TagKey("ui-search")
                    fun ref(modelId: ModelId): TagRef = TagRef.ByKey(
                        scopeRef = modelTagScopeRef(modelId),
                        groupKey = null,
                        key = key
                    )
                }
            }

            object cooking {
                object imported {
                    val key = TagKey("imported")
                    fun ref(modelId: ModelId): TagRef = TagRef.ByKey(
                        scopeRef = modelTagScopeRef(modelId),
                        groupKey = null,
                        key = key
                    )
                }
            }
        }
    }

    object crm {
        val key = ModelKey("crm")
        val ref = modelRefKey(key)

        object person {
            val key = EntityKey("person")
            val ref = EntityRef.ByKey(key)

            object attr {
                object id {
                    val key = AttributeKey("id")
                    val ref = EntityAttributeRef.ByKey(key)
                }

                object name {
                    val key = AttributeKey("name")
                    val ref = EntityAttributeRef.ByKey(key)
                }

                object email {
                    val key = AttributeKey("email")
                    val ref = EntityAttributeRef.ByKey(key)
                }

                object password {
                    val key = AttributeKey("password")
                    val ref = EntityAttributeRef.ByKey(key)
                }
            }
        }

        object company {
            val key = EntityKey("company")
            val ref = EntityRef.ByKey(key)

            object attr {
                object id {
                    val key = AttributeKey("id")
                    val ref = EntityAttributeRef.ByKey(key)
                }

                object name {
                    val key = AttributeKey("name")
                    val ref = EntityAttributeRef.ByKey(key)
                }

                object website {
                    val key = AttributeKey("website")
                    val ref = EntityAttributeRef.ByKey(key)
                }
            }
        }

        object employment {
            val key = RelationshipKey("employment")
            val ref = RelationshipRef.ByKey(key)
            val personRoleKey = RelationshipRoleKey("person")
            val companyRoleKey = RelationshipRoleKey("company")

            object attr {
                object since {
                    val key = AttributeKey("since")
                    val ref = RelationshipAttributeRef.ByKey(key)
                }
            }
        }
    }

    object cooking {
        val key = ModelKey("cooking")
        val ref = modelRefKey(key)

        object ingredient {
            val key = EntityKey("ingredient")
            val ref = EntityRef.ByKey(key)

            object attr {
                object id {
                    val key = AttributeKey("id")
                    val ref = EntityAttributeRef.ByKey(key)
                }

                object name {
                    val key = AttributeKey("name")
                    val ref = EntityAttributeRef.ByKey(key)
                }

                object code {
                    val key = AttributeKey("code")
                    val ref = EntityAttributeRef.ByKey(key)
                }
            }
        }

        object recipe {
            val key = EntityKey("recipe")
            val ref = EntityRef.ByKey(key)

            object attr {
                object id {
                    val key = AttributeKey("id")
                    val ref = EntityAttributeRef.ByKey(key)
                }

                object name {
                    val key = AttributeKey("name")
                    val ref = EntityAttributeRef.ByKey(key)
                }

                object description {
                    val key = AttributeKey("description")
                    val ref = EntityAttributeRef.ByKey(key)
                }
            }
        }

        object chef {
            val key = EntityKey("chef")
            val ref = EntityRef.ByKey(key)

            object attr {
                object id {
                    val key = AttributeKey("id")
                    val ref = EntityAttributeRef.ByKey(key)
                }

                object name {
                    val key = AttributeKey("name")
                    val ref = EntityAttributeRef.ByKey(key)
                }

                object email {
                    val key = AttributeKey("email")
                    val ref = EntityAttributeRef.ByKey(key)
                }

                object fingerprint {
                    val key = AttributeKey("fingerprint")
                    val ref = EntityAttributeRef.ByKey(key)
                }
            }
        }

        object usage {
            val key = RelationshipKey("usage")
            val ref = RelationshipRef.ByKey(key)
            val ingredientRoleKey = RelationshipRoleKey("ingredient")
            val recipeRoleKey = RelationshipRoleKey("recipe")

            object attr {
                object quantity {
                    val key = AttributeKey("quantity")
                    val ref = RelationshipAttributeRef.ByKey(key)
                }

                object unit {
                    val key = AttributeKey("unit")
                    val ref = RelationshipAttributeRef.ByKey(key)
                }
            }
        }

        object author {
            val key = RelationshipKey("author")
            val ref = RelationshipRef.ByKey(key)
            val chefRoleKey = RelationshipRoleKey("chef")
            val recipeRoleKey = RelationshipRoleKey("recipe")

            object attr {
                object date {
                    val key = AttributeKey("date")
                    val ref = RelationshipAttributeRef.ByKey(key)
                }
            }
        }
    }
}

/**
 * Test fixture for model search tests.
 *
 * Functional dataset and tag semantics are documented in `SearchFixture.md`.
 */
internal class SearchFixture private constructor(val env: ModelTestEnv) {
    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }

    class Builder {
        private val fixture = SearchFixture(ModelTestEnv())

        fun addGlobalTags(): Builder {
            createTagGroup(
                key = refs.tags.global.gdpr.key,
                name = "GDPR",
                description = "Global tags used to classify data under GDPR-related categories."
            )
            createGlobalTag(
                groupKey = refs.tags.global.gdpr.key,
                key = refs.tags.global.gdpr.personal_data.key,
                name = "Personal data",
                description = "Data related to an identified or identifiable natural person."
            )
            createGlobalTag(
                groupKey = refs.tags.global.gdpr.key,
                key = refs.tags.global.gdpr.special_category_data.key,
                name = "Special category data",
                description = "Special categories of personal data under GDPR article 9 (health, religion, political opinions, biometrics, etc.)."
            )

            createTagGroup(
                key = refs.tags.global.security.key,
                name = "Security",
                description = "Global tags used to classify security exposure and visibility levels."
            )
            createGlobalTag(
                groupKey = refs.tags.global.security.key,
                key = refs.tags.global.security.public.key,
                name = "Public",
                description = "Data intentionally public outside the company."
            )
            createGlobalTag(
                groupKey = refs.tags.global.security.key,
                key = refs.tags.global.security.internal.key,
                name = "Internal",
                description = "Internal company data that is not public."
            )
            createGlobalTag(
                groupKey = refs.tags.global.security.key,
                key = refs.tags.global.security.confidential.key,
                name = "Confidential",
                description = "Data with limited or hidden visibility, such as passwords or PINs."
            )
            return this
        }

        fun addCrmCookingAndTags(): Builder {
            return this
                .addGlobalTags()
                .addCrm()
                .addCooking()
                .declareCrmTags()
                .declareCookingTags()
                .tagWithCrmTags()
                .tagWithCookingTags()
        }

        fun addCrm(): Builder {
            fixture.createCrm()
            return this
        }

        fun addCooking(): Builder {
            fixture.createCooking()
            return this
        }

        fun declareCrmTags(): Builder {
            val modelId = fixture.env.queries.findModelAggregate(refs.crm.ref).id
            createLocalTag(
                modelId = modelId,
                key = refs.tags.local.crm.ui_result.key,
                name = "UI result",
                description = "Displayed as a result item in CRM screens."
            )
            createLocalTag(
                modelId = modelId,
                key = refs.tags.local.crm.ui_search.key,
                name = "UI search",
                description = "Used as a search criterion in CRM screens."
            )
            return this
        }

        fun declareCookingTags(): Builder {
            val modelId = fixture.env.queries.findModelAggregate(refs.cooking.ref).id
            createLocalTag(
                modelId = modelId,
                key = refs.tags.local.cooking.imported.key,
                name = "Imported",
                description = "Imported from another system in the Cooking model."
            )
            return this
        }

        fun tagWithCrmTags(): Builder {
            val modelId = fixture.env.queries.findModelAggregate(refs.crm.ref).id
            val uiResult = refs.tags.local.crm.ui_result.ref(modelId)
            val uiSearch = refs.tags.local.crm.ui_search.ref(modelId)
            val gdprPersonalData = refs.tags.global.gdpr.personal_data.ref
            val securityInternal = refs.tags.global.security.internal.ref
            val securityPublic = refs.tags.global.security.public.ref
            val securityConfidential = refs.tags.global.security.confidential.ref

            //@formatter:off
            fixture.env.dispatch(ModelAction.Entity_AddTag(refs.crm.ref, refs.crm.person.ref, uiResult))
            fixture.env.dispatch(ModelAction.EntityAttribute_AddTag(refs.crm.ref, refs.crm.person.ref, refs.crm.person.attr.name.ref, gdprPersonalData))
            fixture.env.dispatch(ModelAction.EntityAttribute_AddTag(refs.crm.ref, refs.crm.person.ref, refs.crm.person.attr.name.ref, securityInternal))
            fixture.env.dispatch(ModelAction.EntityAttribute_AddTag(refs.crm.ref, refs.crm.person.ref, refs.crm.person.attr.name.ref, uiSearch))
            fixture.env.dispatch(ModelAction.EntityAttribute_AddTag(refs.crm.ref, refs.crm.person.ref, refs.crm.person.attr.email.ref, gdprPersonalData))
            fixture.env.dispatch(ModelAction.EntityAttribute_AddTag(refs.crm.ref, refs.crm.person.ref, refs.crm.person.attr.email.ref, securityInternal))
            fixture.env.dispatch(ModelAction.EntityAttribute_AddTag(refs.crm.ref, refs.crm.person.ref, refs.crm.person.attr.email.ref, uiSearch))
            fixture.env.dispatch(ModelAction.EntityAttribute_AddTag(refs.crm.ref, refs.crm.person.ref, refs.crm.person.attr.password.ref, securityConfidential))

            fixture.env.dispatch(ModelAction.Entity_AddTag(refs.crm.ref, refs.crm.company.ref, uiResult))
            fixture.env.dispatch(ModelAction.EntityAttribute_AddTag(refs.crm.ref, refs.crm.company.ref, refs.crm.company.attr.name.ref, securityPublic))
            fixture.env.dispatch(ModelAction.EntityAttribute_AddTag(refs.crm.ref, refs.crm.company.ref, refs.crm.company.attr.name.ref, uiSearch))
            fixture.env.dispatch(ModelAction.EntityAttribute_AddTag(refs.crm.ref, refs.crm.company.ref, refs.crm.company.attr.website.ref, securityPublic))
            fixture.env.dispatch(ModelAction.EntityAttribute_AddTag(refs.crm.ref, refs.crm.company.ref, refs.crm.company.attr.website.ref, uiSearch))
            fixture.env.dispatch(ModelAction.EntityAttribute_AddTag(refs.crm.ref, refs.crm.company.ref, refs.crm.company.attr.website.ref, uiResult))

            fixture.env.dispatch(ModelAction.Relationship_AddTag(refs.crm.ref, refs.crm.employment.ref, uiResult))
            fixture.env.dispatch(ModelAction.RelationshipAttribute_AddTag(refs.crm.ref, refs.crm.employment.ref, refs.crm.employment.attr.since.ref, securityInternal))
            fixture.env.dispatch(ModelAction.RelationshipAttribute_AddTag(refs.crm.ref, refs.crm.employment.ref, refs.crm.employment.attr.since.ref, uiResult))
            //@formatter:on
            return this
        }

        fun tagWithCookingTags(): Builder {
            val modelId = fixture.env.queries.findModelAggregate(refs.cooking.ref).id
            val imported = refs.tags.local.cooking.imported.ref(modelId)
            val gdprSpecialCategoryData = refs.tags.global.gdpr.special_category_data.ref
            val securityPublic = refs.tags.global.security.public.ref
            val securityInternal = refs.tags.global.security.internal.ref
            val securityConfidential = refs.tags.global.security.confidential.ref
            //@formatter:off
            fixture.env.dispatch(ModelAction.Model_AddTag(refs.cooking.ref, imported))
            fixture.env.dispatch(ModelAction.Entity_AddTag(refs.cooking.ref, refs.cooking.ingredient.ref, imported))
            fixture.env.dispatch(ModelAction.EntityAttribute_AddTag(refs.cooking.ref, refs.cooking.ingredient.ref, refs.cooking.ingredient.attr.name.ref, securityPublic))
            fixture.env.dispatch(ModelAction.EntityAttribute_AddTag(refs.cooking.ref, refs.cooking.ingredient.ref, refs.cooking.ingredient.attr.name.ref, imported))
            fixture.env.dispatch(ModelAction.EntityAttribute_AddTag(refs.cooking.ref, refs.cooking.ingredient.ref, refs.cooking.ingredient.attr.code.ref, securityInternal))
            fixture.env.dispatch(ModelAction.EntityAttribute_AddTag(refs.cooking.ref, refs.cooking.ingredient.ref, refs.cooking.ingredient.attr.code.ref, imported))

            fixture.env.dispatch(ModelAction.Entity_AddTag(refs.cooking.ref, refs.cooking.recipe.ref, imported))
            fixture.env.dispatch(ModelAction.EntityAttribute_AddTag(refs.cooking.ref, refs.cooking.recipe.ref, refs.cooking.recipe.attr.name.ref, securityPublic))
            fixture.env.dispatch(ModelAction.EntityAttribute_AddTag(refs.cooking.ref, refs.cooking.recipe.ref, refs.cooking.recipe.attr.name.ref, imported))
            fixture.env.dispatch(ModelAction.EntityAttribute_AddTag(refs.cooking.ref, refs.cooking.recipe.ref, refs.cooking.recipe.attr.description.ref, securityPublic))
            fixture.env.dispatch(ModelAction.EntityAttribute_AddTag(refs.cooking.ref, refs.cooking.recipe.ref, refs.cooking.recipe.attr.description.ref, imported))

            fixture.env.dispatch(ModelAction.Entity_AddTag(refs.cooking.ref, refs.cooking.chef.ref, imported))
            fixture.env.dispatch(ModelAction.EntityAttribute_AddTag(refs.cooking.ref, refs.cooking.chef.ref, refs.cooking.chef.attr.name.ref, securityInternal))
            fixture.env.dispatch(ModelAction.EntityAttribute_AddTag(refs.cooking.ref, refs.cooking.chef.ref, refs.cooking.chef.attr.email.ref, securityInternal))
            fixture.env.dispatch(ModelAction.EntityAttribute_AddTag(refs.cooking.ref, refs.cooking.chef.ref, refs.cooking.chef.attr.fingerprint.ref, gdprSpecialCategoryData))
            fixture.env.dispatch(ModelAction.EntityAttribute_AddTag(refs.cooking.ref, refs.cooking.chef.ref, refs.cooking.chef.attr.fingerprint.ref, securityConfidential))

            fixture.env.dispatch(ModelAction.Relationship_AddTag(refs.cooking.ref, refs.cooking.usage.ref, imported))
            fixture.env.dispatch(ModelAction.RelationshipAttribute_AddTag(refs.cooking.ref, refs.cooking.usage.ref, refs.cooking.usage.attr.quantity.ref, securityPublic))
            fixture.env.dispatch(ModelAction.RelationshipAttribute_AddTag(refs.cooking.ref, refs.cooking.usage.ref, refs.cooking.usage.attr.quantity.ref, imported))
            fixture.env.dispatch(ModelAction.RelationshipAttribute_AddTag(refs.cooking.ref, refs.cooking.usage.ref, refs.cooking.usage.attr.unit.ref, securityPublic))
            fixture.env.dispatch(ModelAction.RelationshipAttribute_AddTag(refs.cooking.ref, refs.cooking.usage.ref, refs.cooking.usage.attr.unit.ref, imported))

            fixture.env.dispatch(ModelAction.Relationship_AddTag(refs.cooking.ref, refs.cooking.author.ref, imported))
            fixture.env.dispatch(ModelAction.RelationshipAttribute_AddTag(refs.cooking.ref, refs.cooking.author.ref, refs.cooking.author.attr.date.ref, securityPublic))
            fixture.env.dispatch(ModelAction.RelationshipAttribute_AddTag(refs.cooking.ref, refs.cooking.author.ref, refs.cooking.author.attr.date.ref, imported))
            //@formatter:on
            return this
        }

        fun build(): SearchFixture {
            return fixture
        }

        private fun createTagGroup(key: TagGroupKey, name: String, description: String) {
            fixture.env.dispatchTag(
                TagAction.TagGroupCreate(
                    key = key,
                    name = name,
                    description = description
                )
            )
        }

        private fun createGlobalTag(groupKey: TagGroupKey, key: TagKey, name: String, description: String) {
            fixture.env.dispatchTag(
                TagAction.TagGlobalCreate(
                    groupRef = TagGroupRef.ByKey(groupKey),
                    key = key,
                    name = name,
                    description = description
                )
            )
        }

        private fun createLocalTag(modelId: ModelId, key: TagKey, name: String, description: String) {
            val scopeRef = modelTagScopeRef(modelId)
            fixture.env.dispatchTag(
                TagAction.TagLocalCreate(
                    scopeRef = scopeRef,
                    key = key,
                    name = name,
                    description = description
                )
            )
        }
    }

    private fun createCrm() {
        env.dispatch(
            ModelAction.Model_Create(
                key = refs.crm.key,
                name = LocalizedTextNotLocalized("CRM"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )
        env.dispatch(ModelAction.Type_Create(refs.crm.ref, TypeKey("String"), null, null))

        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = refs.crm.ref,
                entityKey = refs.crm.person.key,
                name = null,
                description = null,
                documentationHome = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = refs.crm.ref,
                entityRef = refs.crm.person.ref,
                attributeKey = refs.crm.person.attr.id.key,
                type = TypeRef.typeRefKey(TypeKey("String")),
                optional = false,
                name = null,
                description = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = refs.crm.ref,
                entityRef = refs.crm.person.ref,
                attributeKey = refs.crm.person.attr.name.key,
                type = TypeRef.typeRefKey(TypeKey("String")),
                optional = false,
                name = null,
                description = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = refs.crm.ref,
                entityRef = refs.crm.person.ref,
                attributeKey = refs.crm.person.attr.email.key,
                type = TypeRef.typeRefKey(TypeKey("String")),
                optional = false,
                name = null,
                description = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = refs.crm.ref,
                entityRef = refs.crm.person.ref,
                attributeKey = refs.crm.person.attr.password.key,
                type = TypeRef.typeRefKey(TypeKey("String")),
                optional = false,
                name = null,
                description = null
            )
        )

        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = refs.crm.ref,
                entityKey = refs.crm.company.key,
                name = null,
                description = null,
                documentationHome = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = refs.crm.ref,
                entityRef = refs.crm.company.ref,
                attributeKey = refs.crm.company.attr.id.key,
                type = TypeRef.typeRefKey(TypeKey("String")),
                optional = false,
                name = null,
                description = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = refs.crm.ref,
                entityRef = refs.crm.company.ref,
                attributeKey = refs.crm.company.attr.name.key,
                type = TypeRef.typeRefKey(TypeKey("String")),
                optional = false,
                name = null,
                description = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = refs.crm.ref,
                entityRef = refs.crm.company.ref,
                attributeKey = refs.crm.company.attr.website.key,
                type = TypeRef.typeRefKey(TypeKey("String")),
                optional = false,
                name = null,
                description = null
            )
        )

        env.dispatch(
            ModelAction.Relationship_Create(
                modelRef = refs.crm.ref,
                relationshipKey = refs.crm.employment.key,
                name = null,
                description = null,
                roleAKey = refs.crm.employment.personRoleKey,
                roleAEntityRef = refs.crm.person.ref,
                roleAName = null,
                roleACardinality = RelationshipCardinality.Many,
                roleBKey = refs.crm.employment.companyRoleKey,
                roleBEntityRef = refs.crm.company.ref,
                roleBName = null,
                roleBCardinality = RelationshipCardinality.One
            )
        )
        env.dispatch(
            ModelAction.RelationshipAttribute_Create(
                modelRef = refs.crm.ref,
                relationshipRef = refs.crm.employment.ref,
                attributeKey = refs.crm.employment.attr.since.key,
                type = TypeRef.typeRefKey(TypeKey("String")),
                optional = false,
                name = null,
                description = null
            )
        )
    }

    private fun createCooking() {
        env.dispatch(
            ModelAction.Model_Create(
                key = refs.cooking.key,
                name = LocalizedTextNotLocalized("Cooking"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )
        env.dispatch(ModelAction.Type_Create(refs.cooking.ref, TypeKey("String"), null, null))

        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = refs.cooking.ref,
                entityKey = refs.cooking.ingredient.key,
                name = null,
                description = null,
                documentationHome = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = refs.cooking.ref,
                entityRef = refs.cooking.ingredient.ref,
                attributeKey = refs.cooking.ingredient.attr.id.key,
                type = TypeRef.typeRefKey(TypeKey("String")),
                optional = false,
                name = null,
                description = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = refs.cooking.ref,
                entityRef = refs.cooking.ingredient.ref,
                attributeKey = refs.cooking.ingredient.attr.name.key,
                type = TypeRef.typeRefKey(TypeKey("String")),
                optional = false,
                name = null,
                description = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = refs.cooking.ref,
                entityRef = refs.cooking.ingredient.ref,
                attributeKey = refs.cooking.ingredient.attr.code.key,
                type = TypeRef.typeRefKey(TypeKey("String")),
                optional = false,
                name = null,
                description = null
            )
        )

        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = refs.cooking.ref,
                entityKey = refs.cooking.recipe.key,
                name = null,
                description = null,
                documentationHome = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = refs.cooking.ref,
                entityRef = refs.cooking.recipe.ref,
                attributeKey = refs.cooking.recipe.attr.id.key,
                type = TypeRef.typeRefKey(TypeKey("String")),
                optional = false,
                name = null,
                description = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = refs.cooking.ref,
                entityRef = refs.cooking.recipe.ref,
                attributeKey = refs.cooking.recipe.attr.name.key,
                type = TypeRef.typeRefKey(TypeKey("String")),
                optional = false,
                name = null,
                description = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = refs.cooking.ref,
                entityRef = refs.cooking.recipe.ref,
                attributeKey = refs.cooking.recipe.attr.description.key,
                type = TypeRef.typeRefKey(TypeKey("String")),
                optional = false,
                name = null,
                description = null
            )
        )

        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = refs.cooking.ref,
                entityKey = refs.cooking.chef.key,
                name = null,
                description = null,
                documentationHome = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = refs.cooking.ref,
                entityRef = refs.cooking.chef.ref,
                attributeKey = refs.cooking.chef.attr.id.key,
                type = TypeRef.typeRefKey(TypeKey("String")),
                optional = false,
                name = null,
                description = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = refs.cooking.ref,
                entityRef = refs.cooking.chef.ref,
                attributeKey = refs.cooking.chef.attr.name.key,
                type = TypeRef.typeRefKey(TypeKey("String")),
                optional = false,
                name = null,
                description = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = refs.cooking.ref,
                entityRef = refs.cooking.chef.ref,
                attributeKey = refs.cooking.chef.attr.email.key,
                type = TypeRef.typeRefKey(TypeKey("String")),
                optional = false,
                name = null,
                description = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = refs.cooking.ref,
                entityRef = refs.cooking.chef.ref,
                attributeKey = refs.cooking.chef.attr.fingerprint.key,
                type = TypeRef.typeRefKey(TypeKey("String")),
                optional = false,
                name = null,
                description = null
            )
        )

        env.dispatch(
            ModelAction.Relationship_Create(
                modelRef = refs.cooking.ref,
                relationshipKey = refs.cooking.usage.key,
                name = null,
                description = null,
                roleAKey = refs.cooking.usage.ingredientRoleKey,
                roleAEntityRef = refs.cooking.ingredient.ref,
                roleAName = null,
                roleACardinality = RelationshipCardinality.Many,
                roleBKey = refs.cooking.usage.recipeRoleKey,
                roleBEntityRef = refs.cooking.recipe.ref,
                roleBName = null,
                roleBCardinality = RelationshipCardinality.Many
            )
        )
        env.dispatch(
            ModelAction.RelationshipAttribute_Create(
                modelRef = refs.cooking.ref,
                relationshipRef = refs.cooking.usage.ref,
                attributeKey = refs.cooking.usage.attr.quantity.key,
                type = TypeRef.typeRefKey(TypeKey("String")),
                optional = false,
                name = null,
                description = null
            )
        )
        env.dispatch(
            ModelAction.RelationshipAttribute_Create(
                modelRef = refs.cooking.ref,
                relationshipRef = refs.cooking.usage.ref,
                attributeKey = refs.cooking.usage.attr.unit.key,
                type = TypeRef.typeRefKey(TypeKey("String")),
                optional = false,
                name = null,
                description = null
            )
        )

        env.dispatch(
            ModelAction.Relationship_Create(
                modelRef = refs.cooking.ref,
                relationshipKey = refs.cooking.author.key,
                name = null,
                description = null,
                roleAKey = refs.cooking.author.chefRoleKey,
                roleAEntityRef = refs.cooking.chef.ref,
                roleAName = null,
                roleACardinality = RelationshipCardinality.One,
                roleBKey = refs.cooking.author.recipeRoleKey,
                roleBEntityRef = refs.cooking.recipe.ref,
                roleBName = null,
                roleBCardinality = RelationshipCardinality.Many
            )
        )
        env.dispatch(
            ModelAction.RelationshipAttribute_Create(
                modelRef = refs.cooking.ref,
                relationshipRef = refs.cooking.author.ref,
                attributeKey = refs.cooking.author.attr.date.key,
                type = TypeRef.typeRefKey(TypeKey("String")),
                optional = false,
                name = null,
                description = null
            )
        )
    }

}
