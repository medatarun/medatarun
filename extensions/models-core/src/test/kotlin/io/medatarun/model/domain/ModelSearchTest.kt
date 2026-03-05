package io.medatarun.model.domain

import io.medatarun.model.actions.ModelAction
import io.medatarun.model.domain.search.*
import io.medatarun.model.ports.needs.ModelTagResolver.Companion.modelTagScopeRef
import io.medatarun.tags.core.domain.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ModelSearchTest {

    // ------------------------------------------------------------------------
    // Filter semantics (single filter)
    // ------------------------------------------------------------------------

    /**
     * This test defines the expected result of `AnyOf` with one tag filter.
     * We search with `security/public` and `security/confidential`.
     * We write the exact list of expected objects in the test.
     * If one object is missing, the test fails.
     * If one extra object appears, the test fails.
     */
    @Test
    fun `matches objects having any of the requested tags`() {
        val fixture = SearchFixture.builder()
            .addCrmCookingAndTags()
            .build()

        val results = search(
            fixture,
            SearchFilterTags.AnyOf(
                listOf(
                    refs.tags.global.security.public.ref,
                    refs.tags.global.security.confidential.ref
                )
            )
        )

        assertEquals(
            setOf(
                Hit.EntityAttribute(refs.crm.key, refs.crm.person.key, refs.crm.person.attr.password.key),
                Hit.EntityAttribute(refs.crm.key, refs.crm.company.key, refs.crm.company.attr.name.key),
                Hit.EntityAttribute(refs.crm.key, refs.crm.company.key, refs.crm.company.attr.website.key),
                Hit.EntityAttribute(
                    refs.cooking.key,
                    refs.cooking.ingredient.key,
                    refs.cooking.ingredient.attr.name.key
                ),
                Hit.EntityAttribute(refs.cooking.key, refs.cooking.recipe.key, refs.cooking.recipe.attr.name.key),
                Hit.EntityAttribute(
                    refs.cooking.key,
                    refs.cooking.recipe.key,
                    refs.cooking.recipe.attr.description.key
                ),
                Hit.EntityAttribute(refs.cooking.key, refs.cooking.chef.key, refs.cooking.chef.attr.fingerprint.key),
                Hit.RelationshipAttribute(
                    refs.cooking.key,
                    refs.cooking.usage.key,
                    refs.cooking.usage.attr.quantity.key
                ),
                Hit.RelationshipAttribute(refs.cooking.key, refs.cooking.usage.key, refs.cooking.usage.attr.unit.key),
                Hit.RelationshipAttribute(refs.cooking.key, refs.cooking.author.key, refs.cooking.author.attr.date.key)
            ),
            resultHits(results)
        )
    }

    /**
     * This test defines the expected result of `AllOf` with one tag filter.
     * We search with `gdpr/personal-data` and `security/internal`.
     * We write the exact list of expected objects in the test.
     * If one object is missing, the test fails.
     * If one extra object appears, the test fails.
     */
    @Test
    fun `matches objects having all requested tags`() {
        val fixture = SearchFixture.builder()
            .addCrmCookingAndTags()
            .build()

        val results = search(
            fixture,
            SearchFilterTags.AllOf(
                listOf(
                    refs.tags.global.gdpr.personal_data.ref,
                    refs.tags.global.security.internal.ref
                )
            )
        )

        assertEquals(
            setOf(
                Hit.EntityAttribute(refs.crm.key, refs.crm.person.key, refs.crm.person.attr.name.key),
                Hit.EntityAttribute(refs.crm.key, refs.crm.person.key, refs.crm.person.attr.email.key)
            ),
            resultHits(results)
        )
    }

    /**
     * This test defines the expected result of `NoneOf` with one tag filter.
     * We exclude `security/public`.
     * We compare the result with the known tagged objects and remove the public ones from that list.
     * If a public object remains, the test fails.
     * If a non-public tagged object is missing, the test fails.
     */
    @Test
    fun `matches objects having none of the excluded tags`() {
        val fixture = SearchFixture.builder()
            .addCrmCookingAndTags()
            .build()

        val allHits = resultHits(search(fixture, SearchFilterTags.NotEmpty))
        val noPublicHits = resultHits(
            search(
                fixture,
                SearchFilterTags.NoneOf(
                    listOf(refs.tags.global.security.public.ref)
                )
            )
        )

        assertEquals(
            allHits - setOf(
                Hit.EntityAttribute(refs.crm.key, refs.crm.company.key, refs.crm.company.attr.name.key),
                Hit.EntityAttribute(refs.crm.key, refs.crm.company.key, refs.crm.company.attr.website.key),
                Hit.EntityAttribute(
                    refs.cooking.key,
                    refs.cooking.ingredient.key,
                    refs.cooking.ingredient.attr.name.key
                ),
                Hit.EntityAttribute(refs.cooking.key, refs.cooking.recipe.key, refs.cooking.recipe.attr.name.key),
                Hit.EntityAttribute(
                    refs.cooking.key,
                    refs.cooking.recipe.key,
                    refs.cooking.recipe.attr.description.key
                ),
                Hit.RelationshipAttribute(
                    refs.cooking.key,
                    refs.cooking.usage.key,
                    refs.cooking.usage.attr.quantity.key
                ),
                Hit.RelationshipAttribute(refs.cooking.key, refs.cooking.usage.key, refs.cooking.usage.attr.unit.key),
                Hit.RelationshipAttribute(refs.cooking.key, refs.cooking.author.key, refs.cooking.author.attr.date.key)
            ),
            noPublicHits.intersect(allHits)
        )
    }

    /**
     * This test locks the exact result of `Empty` on the current fixture.
     * We write the exact list of expected objects in the test, including auto-created `id` attributes.
     * If one object is missing, the test fails.
     * If one extra object appears, the test fails.
     */
    @Test
    fun `matches objects with no tags`() {
        val fixture = SearchFixture.builder()
            .addCrmCookingAndTags()
            .build()

        val emptyHits = resultHits(search(fixture, SearchFilterTags.Empty))
        assertEquals(
            setOf(
                Hit.Model(refs.crm.key),
                Hit.EntityAttribute(refs.crm.key, refs.crm.person.key, refs.crm.person.attr.id.key),
                Hit.EntityAttribute(refs.crm.key, refs.crm.company.key, refs.crm.company.attr.id.key),
                Hit.EntityAttribute(refs.cooking.key, refs.cooking.ingredient.key, refs.cooking.ingredient.attr.id.key),
                Hit.EntityAttribute(refs.cooking.key, refs.cooking.recipe.key, refs.cooking.recipe.attr.id.key),
                Hit.EntityAttribute(refs.cooking.key, refs.cooking.chef.key, refs.cooking.chef.attr.id.key),
            ),
            emptyHits
        )
    }

    /**
     * This test defines the expected result of `NotEmpty` for the current fixture.
     * `NotEmpty` must return all indexed objects that have at least one tag.
     * We write the exact list of expected objects in the test.
     * If one object is missing, the test fails.
     * If one extra object appears, the test fails.
     */
    @Test
    fun `nonEmpty matches objects with at least one tag`() {
        val fixture = SearchFixture.builder()
            .addCrmCookingAndTags()
            .build()

        val nonEmptyHits = resultHits(search(fixture, SearchFilterTags.NotEmpty))

        assertEquals(
            setOf(
                Hit.Entity(refs.crm.key, refs.crm.person.key),
                Hit.EntityAttribute(refs.crm.key, refs.crm.person.key, refs.crm.person.attr.name.key),
                Hit.EntityAttribute(refs.crm.key, refs.crm.person.key, refs.crm.person.attr.email.key),
                Hit.EntityAttribute(refs.crm.key, refs.crm.person.key, refs.crm.person.attr.password.key),
                Hit.Entity(refs.crm.key, refs.crm.company.key),
                Hit.EntityAttribute(refs.crm.key, refs.crm.company.key, refs.crm.company.attr.name.key),
                Hit.EntityAttribute(refs.crm.key, refs.crm.company.key, refs.crm.company.attr.website.key),
                Hit.Relationship(refs.crm.key, refs.crm.employment.key),
                Hit.RelationshipAttribute(refs.crm.key, refs.crm.employment.key, refs.crm.employment.attr.since.key),

                Hit.Model(refs.cooking.key),
                Hit.Entity(refs.cooking.key, refs.cooking.ingredient.key),
                Hit.EntityAttribute(
                    refs.cooking.key,
                    refs.cooking.ingredient.key,
                    refs.cooking.ingredient.attr.name.key
                ),
                Hit.EntityAttribute(
                    refs.cooking.key,
                    refs.cooking.ingredient.key,
                    refs.cooking.ingredient.attr.code.key
                ),
                Hit.Entity(refs.cooking.key, refs.cooking.recipe.key),
                Hit.EntityAttribute(refs.cooking.key, refs.cooking.recipe.key, refs.cooking.recipe.attr.name.key),
                Hit.EntityAttribute(
                    refs.cooking.key,
                    refs.cooking.recipe.key,
                    refs.cooking.recipe.attr.description.key
                ),
                Hit.Entity(refs.cooking.key, refs.cooking.chef.key),
                Hit.EntityAttribute(refs.cooking.key, refs.cooking.chef.key, refs.cooking.chef.attr.name.key),
                Hit.EntityAttribute(refs.cooking.key, refs.cooking.chef.key, refs.cooking.chef.attr.email.key),
                Hit.EntityAttribute(refs.cooking.key, refs.cooking.chef.key, refs.cooking.chef.attr.fingerprint.key),
                Hit.Relationship(refs.cooking.key, refs.cooking.usage.key),
                Hit.RelationshipAttribute(
                    refs.cooking.key,
                    refs.cooking.usage.key,
                    refs.cooking.usage.attr.quantity.key
                ),
                Hit.RelationshipAttribute(refs.cooking.key, refs.cooking.usage.key, refs.cooking.usage.attr.unit.key),
                Hit.Relationship(refs.cooking.key, refs.cooking.author.key),
                Hit.RelationshipAttribute(refs.cooking.key, refs.cooking.author.key, refs.cooking.author.attr.date.key),
            ),
            nonEmptyHits
        )
    }

    /**
     * This test checks the relationship between `Empty` and `NotEmpty`.
     * The two result sets must not overlap.
     * The two result sets together must cover all indexed objects.
     * If one object is in both sets, the test fails.
     * If one indexed object is in neither set, the test fails.
     */
    @Test
    fun `empty and not-empty partition indexed objects`() {
        val fixture = SearchFixture.builder()
            .addCrmCookingAndTags()
            .build()

        val emptyHits = resultHits(search(fixture, SearchFilterTags.Empty))
        val nonEmptyHits = resultHits(search(fixture, SearchFilterTags.NotEmpty))

        assertTrue(emptyHits.intersect(nonEmptyHits).isEmpty())
        assertEquals(allIndexedHits(fixture), emptyHits + nonEmptyHits)
    }

    // ------------------------------------------------------------------------
    // Tag resolution (global and local scopes)
    // ------------------------------------------------------------------------

    /**
     * This test will define how a global tag reference is resolved (`group/tag`) before filtering.
     * It is separate from `AnyOf` / `AllOf` / `NoneOf` tests because this one is about tag lookup, not tag matching rules.
     */
    @Test
    fun `resolves global tags by group and key`() {
        val fixture = SearchFixture.builder()
            .addCrmCookingAndTags()
            .build()

        val results = search(
            fixture,
            SearchFilterTags.AnyOf(listOf(refs.tags.global.gdpr.special_category_data.ref))
        )

        assertEquals(
            setOf(
                Hit.EntityAttribute(refs.cooking.key, refs.cooking.chef.key, refs.cooking.chef.attr.fingerprint.key)
            ),
            resultHits(results)
        )
    }

    /**
     * This test will define how local tag lookup works in CRM scope.
     * It must show that a local CRM tag matches CRM objects and does not match objects from another model.
     */
    @Test
    fun `resolves local tags in crm scope`() {
        val fixture = SearchFixture.builder()
            .addCrmCookingAndTags()
            .build()
        val crmModelId = fixture.env.queries.findModel(refs.crm.ref).id

        val results = search(
            fixture,
            SearchFilterTags.AnyOf(listOf(refs.tags.local.crm.ui_search.ref(crmModelId)))
        )

        assertEquals(
            setOf(
                Hit.EntityAttribute(refs.crm.key, refs.crm.person.key, refs.crm.person.attr.name.key),
                Hit.EntityAttribute(refs.crm.key, refs.crm.person.key, refs.crm.person.attr.email.key),
                Hit.EntityAttribute(refs.crm.key, refs.crm.company.key, refs.crm.company.attr.name.key),
                Hit.EntityAttribute(refs.crm.key, refs.crm.company.key, refs.crm.company.attr.website.key),
            ),
            resultHits(results)
        )
    }

    /**
     * This test will define how local tag lookup works in Cooking scope.
     * It complements the CRM local-tag test and checks the same rule on another model.
     */
    @Test
    fun `resolves local tags in cooking scope`() {
        val fixture = SearchFixture.builder()
            .addCrmCookingAndTags()
            .build()
        val cookingModelId = fixture.env.queries.findModel(refs.cooking.ref).id

        val results = search(
            fixture,
            SearchFilterTags.AnyOf(listOf(refs.tags.local.cooking.imported.ref(cookingModelId)))
        )

        assertEquals(
            setOf(
                Hit.Model(refs.cooking.key),
                Hit.Entity(refs.cooking.key, refs.cooking.ingredient.key),
                Hit.EntityAttribute(
                    refs.cooking.key,
                    refs.cooking.ingredient.key,
                    refs.cooking.ingredient.attr.name.key
                ),
                Hit.EntityAttribute(
                    refs.cooking.key,
                    refs.cooking.ingredient.key,
                    refs.cooking.ingredient.attr.code.key
                ),
                Hit.Entity(refs.cooking.key, refs.cooking.recipe.key),
                Hit.EntityAttribute(refs.cooking.key, refs.cooking.recipe.key, refs.cooking.recipe.attr.name.key),
                Hit.EntityAttribute(
                    refs.cooking.key,
                    refs.cooking.recipe.key,
                    refs.cooking.recipe.attr.description.key
                ),
                Hit.Entity(refs.cooking.key, refs.cooking.chef.key),
                Hit.Relationship(refs.cooking.key, refs.cooking.usage.key),
                Hit.RelationshipAttribute(
                    refs.cooking.key,
                    refs.cooking.usage.key,
                    refs.cooking.usage.attr.quantity.key
                ),
                Hit.RelationshipAttribute(refs.cooking.key, refs.cooking.usage.key, refs.cooking.usage.attr.unit.key),
                Hit.Relationship(refs.cooking.key, refs.cooking.author.key),
                Hit.RelationshipAttribute(refs.cooking.key, refs.cooking.author.key, refs.cooking.author.attr.date.key),
            ),
            resultHits(results)
        )
    }

    /**
     * This test will define what happens when a global tag does not exist.
     * Search must fail.
     * If search silently ignores the missing tag, this test must fail.
     */
    @Test
    fun `fails when any requested global tag does not exist`() {
        val fixture = SearchFixture.builder()
            .addCrmCookingAndTags()
            .build()
        val unknownGlobalTag = TagRef.ByKey(
            scopeRef = TagScopeRef.Global,
            groupKey = refs.tags.global.security.key,
            key = TagKey("missing-global-tag")
        )

        assertFailsWith<TagNotFoundException> {
            search(fixture, SearchFilterTags.AnyOf(listOf(unknownGlobalTag)))
        }
    }

    /**
     * This test will define what happens when a local tag does not exist in the requested model scope.
     * Search must fail.
     * If search silently ignores the missing tag, this test must fail.
     */
    @Test
    fun `fails when any requested local tag does not exist`() {
        val fixture = SearchFixture.builder()
            .addCrmCookingAndTags()
            .build()
        val crmModelId = fixture.env.queries.findModel(refs.crm.ref).id
        val unknownLocalTag = TagRef.ByKey(
            scopeRef = modelTagScopeRef(crmModelId),
            groupKey = null,
            key = TagKey("missing-local-tag")
        )

        assertFailsWith<TagNotFoundException> {
            search(fixture, SearchFilterTags.AnyOf(listOf(unknownLocalTag)))
        }
    }

    // ------------------------------------------------------------------------
    // Filter combination
    // ------------------------------------------------------------------------

    /**
     * This test will define how `AND` combines two filters.
     * It must return only objects that match both filters.
     * This test is about filter combination, not about tag lookup.
     */
    @Test
    fun `intersects filter results with and`() {
        val fixture = SearchFixture.builder()
            .addCrmCookingAndTags()
            .build()

        val results = search(
            fixture,
            SearchFilters(
                operator = SearchFiltersLogicalOperator.AND,
                items = listOf(
                    SearchFilterTags.AnyOf(listOf(refs.tags.global.security.public.ref)),
                    SearchFilterTags.AnyOf(
                        listOf(
                            refs.tags.local.cooking.imported.ref(
                                fixture.env.queries.findModel(
                                    refs.cooking.ref
                                ).id
                            )
                        )
                    )
                )
            )
        )

        assertEquals(
            setOf(
                Hit.EntityAttribute(
                    refs.cooking.key,
                    refs.cooking.ingredient.key,
                    refs.cooking.ingredient.attr.name.key
                ),
                Hit.EntityAttribute(refs.cooking.key, refs.cooking.recipe.key, refs.cooking.recipe.attr.name.key),
                Hit.EntityAttribute(
                    refs.cooking.key,
                    refs.cooking.recipe.key,
                    refs.cooking.recipe.attr.description.key
                ),
                Hit.RelationshipAttribute(
                    refs.cooking.key,
                    refs.cooking.usage.key,
                    refs.cooking.usage.attr.quantity.key
                ),
                Hit.RelationshipAttribute(refs.cooking.key, refs.cooking.usage.key, refs.cooking.usage.attr.unit.key),
                Hit.RelationshipAttribute(refs.cooking.key, refs.cooking.author.key, refs.cooking.author.attr.date.key),
            ),
            resultHits(results)
        )
    }

    /**
     * This test will define how `OR` combines two filters.
     * It must return objects that match one filter, the other, or both.
     * This test is about filter combination, not about tag lookup.
     */
    @Test
    fun `unions filter results with or`() {
        val fixture = SearchFixture.builder()
            .addCrmCookingAndTags()
            .build()

        val results = search(
            fixture,
            SearchFilters(
                operator = SearchFiltersLogicalOperator.OR,
                items = listOf(
                    SearchFilterTags.AnyOf(listOf(refs.tags.local.crm.ui_result.ref(fixture.env.queries.findModel(refs.crm.ref).id))),
                    SearchFilterTags.AnyOf(listOf(refs.tags.global.gdpr.special_category_data.ref))
                )
            )
        )

        val hits = resultHits(results)
        val expectedHits = setOf(
            Hit.Entity(refs.crm.key, refs.crm.person.key),
            Hit.Entity(refs.crm.key, refs.crm.company.key),
            Hit.EntityAttribute(refs.crm.key, refs.crm.company.key, refs.crm.company.attr.website.key),
            Hit.Relationship(refs.crm.key, refs.crm.employment.key),
            Hit.RelationshipAttribute(refs.crm.key, refs.crm.employment.key, refs.crm.employment.attr.since.key),
            Hit.EntityAttribute(refs.cooking.key, refs.cooking.chef.key, refs.cooking.chef.attr.fingerprint.key),
        )

        assertEquals(
            expectedHits,
            hits
        )
    }

    // ------------------------------------------------------------------------
    // Edge cases: requested tags inside one filter
    // ------------------------------------------------------------------------

    /**
     * This test records the current behavior of `AnyOf(emptyList())`.
     * We keep it to make the behavior explicit.
     * If the behavior changes later, the test will show it.
     */
    @Test
    fun `handles any-of with empty requested tags list`() {
        val fixture = SearchFixture.builder()
            .addCrmCookingAndTags()
            .build()

        val hits = resultHits(search(fixture, SearchFilterTags.AnyOf(emptyList())))

        assertEquals(emptySet(), hits)
    }

    /**
     * This test records the current behavior of `AllOf(emptyList())`.
     * We keep it to make the behavior explicit.
     * If the behavior changes later, the test will show it.
     */
    @Test
    fun `handles all-of with empty requested tags list`() {
        val fixture = SearchFixture.builder()
            .addCrmCookingAndTags()
            .build()

        val hits = resultHits(search(fixture, SearchFilterTags.AllOf(emptyList())))

        assertEquals(allIndexedHits(fixture), hits)
    }

    /**
     * This test records the current behavior of `NoneOf(emptyList())`.
     * We keep it to make the behavior explicit.
     * If the behavior changes later, the test will show it.
     */
    @Test
    fun `handles none-of with empty excluded tags list`() {
        val fixture = SearchFixture.builder()
            .addCrmCookingAndTags()
            .build()

        val hits = resultHits(search(fixture, SearchFilterTags.NoneOf(emptyList())))

        assertEquals(allIndexedHits(fixture), hits)
    }

    /**
     * This test records the current behavior when `AnyOf` contains the same tag more than once.
     * We keep it to make this edge case explicit.
     * If the behavior changes later, the test will show it.
     */
    @Test
    fun `ignores or handles duplicate requested tags in any-of`() {
        val fixture = SearchFixture.builder()
            .addCrmCookingAndTags()
            .build()

        val uniqueHits = resultHits(
            search(
                fixture,
                SearchFilterTags.AnyOf(
                    listOf(
                        refs.tags.global.security.public.ref,
                        refs.tags.global.security.confidential.ref
                    )
                )
            )
        )
        val duplicateHits = resultHits(
            search(
                fixture,
                SearchFilterTags.AnyOf(
                    listOf(
                        refs.tags.global.security.public.ref,
                        refs.tags.global.security.public.ref,
                        refs.tags.global.security.confidential.ref
                    )
                )
            )
        )

        assertEquals(uniqueHits, duplicateHits)
    }

    /**
     * This test records the current behavior when `AllOf` contains the same tag more than once.
     * We keep it to make this edge case explicit.
     * If the behavior changes later, the test will show it.
     */
    @Test
    fun `ignores or handles duplicate requested tags in all-of`() {
        val fixture = SearchFixture.builder()
            .addCrmCookingAndTags()
            .build()

        val uniqueHits = resultHits(
            search(
                fixture,
                SearchFilterTags.AllOf(
                    listOf(
                        refs.tags.global.gdpr.personal_data.ref,
                        refs.tags.global.security.internal.ref
                    )
                )
            )
        )
        val duplicateHits = resultHits(
            search(
                fixture,
                SearchFilterTags.AllOf(
                    listOf(
                        refs.tags.global.gdpr.personal_data.ref,
                        refs.tags.global.gdpr.personal_data.ref,
                        refs.tags.global.security.internal.ref
                    )
                )
            )
        )

        assertEquals(uniqueHits, duplicateHits)
    }

    /**
     * This test records the current behavior when `NoneOf` contains the same tag more than once.
     * We keep it to make this edge case explicit.
     * If the behavior changes later, the test will show it.
     */
    @Test
    fun `ignores or handles duplicate requested tags in none-of`() {
        val fixture = SearchFixture.builder()
            .addCrmCookingAndTags()
            .build()

        val uniqueHits = resultHits(
            search(
                fixture,
                SearchFilterTags.NoneOf(listOf(refs.tags.global.security.public.ref))
            )
        )
        val duplicateHits = resultHits(
            search(
                fixture,
                SearchFilterTags.NoneOf(
                    listOf(
                        refs.tags.global.security.public.ref,
                        refs.tags.global.security.public.ref
                    )
                )
            )
        )

        assertEquals(uniqueHits, duplicateHits)
    }

    // ------------------------------------------------------------------------
    // Edge cases: filter chain
    // ------------------------------------------------------------------------

    /**
     * This test records what search returns with `AND` and no filters.
     * This can happen when a search form is submitted without selecting any filter.
     * If the behavior changes later, the test will show it.
     */
    @Test
    fun `handles and with no filters`() {
        val fixture = SearchFixture.builder()
            .addCrmCookingAndTags()
            .build()

        val hits = resultHits(
            search(
                fixture,
                SearchFilters(
                    operator = SearchFiltersLogicalOperator.AND,
                    items = emptyList()
                )
            )
        )

        assertEquals(allIndexedHits(fixture), hits)
    }

    /**
     * This test records what search returns with `OR` and no filters.
     * This complements the `AND` case for an empty filter list.
     * If the behavior changes later, the test will show it.
     */
    @Test
    fun `handles or with no filters`() {
        val fixture = SearchFixture.builder()
            .addCrmCookingAndTags()
            .build()

        val hits = resultHits(
            search(
                fixture,
                SearchFilters(
                    operator = SearchFiltersLogicalOperator.OR,
                    items = emptyList()
                )
            )
        )

        assertEquals(emptySet(), hits)
    }

    // ------------------------------------------------------------------------
    // Query index coverage (what search actually filters)
    // ------------------------------------------------------------------------

    /**
     * This test will check search index coverage.
     * It must show that search can return model, entity, entity attribute, relationship, and relationship attribute objects.
     */
    @Test
    fun `search applies to all indexed object kinds`() {
        val fixture = SearchFixture.builder()
            .addCrmCookingAndTags()
            .build()

        val hits = resultHits(search(fixture, SearchFilterTags.NotEmpty))

        assertTrue(hits.contains(Hit.Model(refs.cooking.key)))
        assertTrue(hits.contains(Hit.Entity(refs.crm.key, refs.crm.person.key)))
        assertTrue(
            hits.contains(
                Hit.EntityAttribute(
                    refs.crm.key,
                    refs.crm.person.key,
                    refs.crm.person.attr.email.key
                )
            )
        )
        assertTrue(hits.contains(Hit.Relationship(refs.crm.key, refs.crm.employment.key)))
        assertTrue(
            hits.contains(
                Hit.RelationshipAttribute(
                    refs.cooking.key,
                    refs.cooking.usage.key,
                    refs.cooking.usage.attr.unit.key
                )
            )
        )
    }

    /**
     * This test will document that auto-created entity `id` attributes are indexed.
     * This matters because these generated attributes appear in `Empty` / `NotEmpty` results.
     */
    @Test
    fun `search includes auto-created entity id attributes in indexed objects`() {
        val fixture = SearchFixture.builder()
            .addCrmCookingAndTags()
            .build()

        val emptyHits = resultHits(search(fixture, SearchFilterTags.Empty))

        assertTrue(
            emptyHits.contains(
                Hit.EntityAttribute(
                    refs.crm.key,
                    refs.crm.person.key,
                    refs.crm.person.attr.id.key
                )
            )
        )
        assertTrue(
            emptyHits.contains(
                Hit.EntityAttribute(
                    refs.crm.key,
                    refs.crm.company.key,
                    refs.crm.company.attr.id.key
                )
            )
        )
        assertTrue(
            emptyHits.contains(
                Hit.EntityAttribute(
                    refs.cooking.key,
                    refs.cooking.ingredient.key,
                    refs.cooking.ingredient.attr.id.key
                )
            )
        )
        assertTrue(
            emptyHits.contains(
                Hit.EntityAttribute(
                    refs.cooking.key,
                    refs.cooking.recipe.key,
                    refs.cooking.recipe.attr.id.key
                )
            )
        )
        assertTrue(
            emptyHits.contains(
                Hit.EntityAttribute(
                    refs.cooking.key,
                    refs.cooking.chef.key,
                    refs.cooking.chef.attr.id.key
                )
            )
        )
    }

    @Test
    fun `text contains matches model names`() {
        val fixture = SearchFixture.builder()
            .addCrmCookingAndTags()
            .build()

        val hits = resultHits(search(fixture, SearchFilterText.Contains("cook")))

        assertTrue(hits.contains(Hit.Model(refs.cooking.key)))
    }

    @Test
    fun `text contains matches object keys`() {
        val fixture = SearchFixture.builder()
            .addCrmCookingAndTags()
            .build()

        val hits = resultHits(search(fixture, SearchFilterText.Contains("fingerprint")))

        assertEquals(
            setOf(Hit.EntityAttribute(refs.cooking.key, refs.cooking.chef.key, refs.cooking.chef.attr.fingerprint.key)),
            hits
        )
    }

    @Test
    fun `text contains matches object descriptions`() {
        val fixture = SearchFixture.builder()
            .addCrmCookingAndTags()
            .build()

        fixture.env.dispatch(
            ModelAction.RelationshipAttribute_UpdateDescription(
                refs.cooking.ref,
                refs.cooking.author.ref,
                refs.cooking.author.attr.date.ref,
                LocalizedMarkdownNotLocalized("Publication calendar")
            )
        )

        val hits = resultHits(search(fixture, SearchFilterText.Contains("calendar")))

        assertEquals(
            setOf(
                Hit.RelationshipAttribute(
                    refs.cooking.key,
                    refs.cooking.author.key,
                    refs.cooking.author.attr.date.key
                )
            ),
            hits
        )
    }

    private fun search(fixture: SearchFixture, filter: SearchFilter): JsonObject {
        return search(
            fixture,
            SearchFilters(
                operator = SearchFiltersLogicalOperator.AND,
                items = listOf(filter)
            )
        )
    }

    private fun search(fixture: SearchFixture, filters: SearchFilters): JsonObject {
        return fixture.env.dispatchResult(
            ModelAction.Search(
                filters = filters,
                fields = SearchFields(emptyList())
            )
        ) as JsonObject
    }

    private fun allIndexedHits(fixture: SearchFixture): Set<Hit> {
        return resultHits(search(fixture, SearchFilterTags.Empty)) +
                resultHits(search(fixture, SearchFilterTags.NotEmpty))
    }

    private fun resultHits(results: JsonObject): Set<Hit> {
        return results.getValue("items").jsonArray.map { item ->
            val location = item.jsonObject.getValue("location").jsonObject
            when (val objectType = location.getValue("objectType").jsonPrimitive.content) {
                "model" -> Hit.Model(
                    modelKey = ModelKey(location.getValue("modelKey").jsonPrimitive.content)
                )

                "entity" -> Hit.Entity(
                    modelKey = ModelKey(location.getValue("modelKey").jsonPrimitive.content),
                    entityKey = EntityKey(location.getValue("entityKey").jsonPrimitive.content)
                )

                "entityAttribute" -> Hit.EntityAttribute(
                    modelKey = ModelKey(location.getValue("modelKey").jsonPrimitive.content),
                    entityKey = EntityKey(location.getValue("entityKey").jsonPrimitive.content),
                    attributeKey = AttributeKey(location.getValue("entityAttributeKey").jsonPrimitive.content)
                )

                "relationship" -> Hit.Relationship(
                    modelKey = ModelKey(location.getValue("modelKey").jsonPrimitive.content),
                    relationshipKey = RelationshipKey(location.getValue("relationshipKey").jsonPrimitive.content)
                )

                "relationshipAttribute" -> Hit.RelationshipAttribute(
                    modelKey = ModelKey(location.getValue("modelKey").jsonPrimitive.content),
                    relationshipKey = RelationshipKey(location.getValue("relationshipKey").jsonPrimitive.content),
                    attributeKey = AttributeKey(location.getValue("relationshipAttributeKey").jsonPrimitive.content)
                )

                else -> error("Unexpected location objectType in model search tests: $objectType")
            }
        }.toSet()
    }

    private sealed interface Hit {
        data class Model(val modelKey: ModelKey) : Hit
        data class Entity(val modelKey: ModelKey, val entityKey: EntityKey) : Hit
        data class EntityAttribute(
            val modelKey: ModelKey,
            val entityKey: EntityKey,
            val attributeKey: AttributeKey
        ) : Hit

        data class Relationship(val modelKey: ModelKey, val relationshipKey: RelationshipKey) : Hit
        data class RelationshipAttribute(
            val modelKey: ModelKey,
            val relationshipKey: RelationshipKey,
            val attributeKey: AttributeKey
        ) : Hit
    }
}
