package io.medatarun.model.actions

import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.EntityId
import io.medatarun.model.domain.LocalizedMarkdownNotLocalized
import io.medatarun.model.domain.LocalizedTextNotLocalized
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.RelationshipKey
import io.medatarun.model.domain.RelationshipId
import io.medatarun.model.domain.search.SearchFields
import io.medatarun.model.domain.search.SearchFilter
import io.medatarun.model.domain.search.SearchFilterTags
import io.medatarun.model.domain.search.SearchFilterText
import io.medatarun.model.domain.search.SearchFilters
import io.medatarun.model.domain.search.SearchFiltersLogicalOperator
import io.medatarun.model.ports.needs.ModelTagResolver
import io.medatarun.tags.core.domain.TagKey
import io.medatarun.tags.core.domain.TagNotFoundException
import io.medatarun.tags.core.domain.TagRef
import io.medatarun.tags.core.domain.TagScopeRef
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class Search_Test {

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

        assertTrue(
            results.containsExactly(
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
                )
            )
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

        assertTrue(
            results.containsExactly(
                setOf(
                Hit.EntityAttribute(refs.crm.key, refs.crm.person.key, refs.crm.person.attr.name.key),
                Hit.EntityAttribute(refs.crm.key, refs.crm.person.key, refs.crm.person.attr.email.key)
                )
            )
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

        val allResults = search(fixture, SearchFilterTags.NotEmpty)
        val noPublicResults = search(
            fixture,
            SearchFilterTags.NoneOf(
                listOf(refs.tags.global.security.public.ref)
            )
        )
        val allHits = allResults.collectHits()
        val noPublicHits = noPublicResults.collectHits()

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

        val results = search(fixture, SearchFilterTags.Empty)
        assertTrue(
            results.containsExactly(
                setOf(
                Hit.Model(refs.crm.key),
                Hit.EntityAttribute(refs.crm.key, refs.crm.person.key, refs.crm.person.attr.id.key),
                Hit.EntityAttribute(refs.crm.key, refs.crm.company.key, refs.crm.company.attr.id.key),
                Hit.EntityAttribute(refs.cooking.key, refs.cooking.ingredient.key, refs.cooking.ingredient.attr.id.key),
                Hit.EntityAttribute(refs.cooking.key, refs.cooking.recipe.key, refs.cooking.recipe.attr.id.key),
                Hit.EntityAttribute(refs.cooking.key, refs.cooking.chef.key, refs.cooking.chef.attr.id.key),
                )
            )
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

        val results = search(fixture, SearchFilterTags.NotEmpty)

        assertTrue(
            results.containsExactly(
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
                )
            )
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

        val emptyResults = search(fixture, SearchFilterTags.Empty)
        val nonEmptyResults = search(fixture, SearchFilterTags.NotEmpty)
        val emptyHits = emptyResults.collectHits()
        val nonEmptyHits = nonEmptyResults.collectHits()
        val allIndexedHits = emptyHits + nonEmptyHits

        assertTrue(emptyHits.intersect(nonEmptyHits).isEmpty())
        assertEquals(allIndexedHits, emptyHits + nonEmptyHits)
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

        assertTrue(
            results.containsExactly(
                setOf(
                Hit.EntityAttribute(refs.cooking.key, refs.cooking.chef.key, refs.cooking.chef.attr.fingerprint.key)
                )
            )
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

        assertTrue(
            results.containsExactly(
                setOf(
                Hit.EntityAttribute(refs.crm.key, refs.crm.person.key, refs.crm.person.attr.name.key),
                Hit.EntityAttribute(refs.crm.key, refs.crm.person.key, refs.crm.person.attr.email.key),
                Hit.EntityAttribute(refs.crm.key, refs.crm.company.key, refs.crm.company.attr.name.key),
                Hit.EntityAttribute(refs.crm.key, refs.crm.company.key, refs.crm.company.attr.website.key),
                )
            )
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

        assertTrue(
            results.containsExactly(
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
                )
            )
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
            scopeRef = ModelTagResolver.modelTagScopeRef(crmModelId),
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

        assertTrue(
            results.containsExactly(
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
                )
            )
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

        val expectedHits = setOf(
            Hit.Entity(refs.crm.key, refs.crm.person.key),
            Hit.Entity(refs.crm.key, refs.crm.company.key),
            Hit.EntityAttribute(refs.crm.key, refs.crm.company.key, refs.crm.company.attr.website.key),
            Hit.Relationship(refs.crm.key, refs.crm.employment.key),
            Hit.RelationshipAttribute(refs.crm.key, refs.crm.employment.key, refs.crm.employment.attr.since.key),
            Hit.EntityAttribute(refs.cooking.key, refs.cooking.chef.key, refs.cooking.chef.attr.fingerprint.key),
        )

        assertTrue(results.containsExactly(expectedHits))
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

        val results = search(fixture, SearchFilterTags.AnyOf(emptyList()))
        assertTrue(results.containsExactly(emptySet()))
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

        val results = search(fixture, SearchFilterTags.AllOf(emptyList()))
        val emptyResults = search(fixture, SearchFilterTags.Empty)
        val nonEmptyResults = search(fixture, SearchFilterTags.NotEmpty)
        val allIndexedHits = emptyResults.collectHits() + nonEmptyResults.collectHits()
        assertTrue(results.containsExactly(allIndexedHits))
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

        val results = search(fixture, SearchFilterTags.NoneOf(emptyList()))
        val emptyResults = search(fixture, SearchFilterTags.Empty)
        val nonEmptyResults = search(fixture, SearchFilterTags.NotEmpty)
        val allIndexedHits = emptyResults.collectHits() + nonEmptyResults.collectHits()
        assertTrue(results.containsExactly(allIndexedHits))
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

        val uniqueResults = (
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
        val duplicateResults = (
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

        assertTrue(duplicateResults.containsExactly(uniqueResults))
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

        val uniqueResults = (
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
        val duplicateResults = (
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

        assertTrue(duplicateResults.containsExactly(uniqueResults))
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

        val uniqueResults = (
            search(
                fixture,
                SearchFilterTags.NoneOf(listOf(refs.tags.global.security.public.ref))
            )
        )
        val duplicateResults = (
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

        assertTrue(duplicateResults.containsExactly(uniqueResults))
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

        val results = search(
            fixture,
            SearchFilters(
                operator = SearchFiltersLogicalOperator.AND,
                items = emptyList()
            )
        )

        val emptyResults = search(fixture, SearchFilterTags.Empty)
        val nonEmptyResults = search(fixture, SearchFilterTags.NotEmpty)
        val allIndexedHits = emptyResults.collectHits() + nonEmptyResults.collectHits()
        assertTrue(results.containsExactly(allIndexedHits))
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

        val results = search(
            fixture,
            SearchFilters(
                operator = SearchFiltersLogicalOperator.OR,
                items = emptyList()
            )
        )

        assertTrue(results.containsExactly(emptySet()))
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

        val results = search(fixture, SearchFilterTags.NotEmpty)

        assertTrue(results.contains(Hit.Model(refs.cooking.key)))
        assertTrue(results.contains(Hit.Entity(refs.crm.key, refs.crm.person.key)))
        assertTrue(
            results.contains(
                Hit.EntityAttribute(
                    refs.crm.key,
                    refs.crm.person.key,
                    refs.crm.person.attr.email.key
                )
            )
        )
        assertTrue(results.contains(Hit.Relationship(refs.crm.key, refs.crm.employment.key)))
        assertTrue(
            results.contains(
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

        val results = search(fixture, SearchFilterTags.Empty)

        assertTrue(
            results.contains(
                Hit.EntityAttribute(
                    refs.crm.key,
                    refs.crm.person.key,
                    refs.crm.person.attr.id.key
                )
            )
        )
        assertTrue(
            results.contains(
                Hit.EntityAttribute(
                    refs.crm.key,
                    refs.crm.company.key,
                    refs.crm.company.attr.id.key
                )
            )
        )
        assertTrue(
            results.contains(
                Hit.EntityAttribute(
                    refs.cooking.key,
                    refs.cooking.ingredient.key,
                    refs.cooking.ingredient.attr.id.key
                )
            )
        )
        assertTrue(
            results.contains(
                Hit.EntityAttribute(
                    refs.cooking.key,
                    refs.cooking.recipe.key,
                    refs.cooking.recipe.attr.id.key
                )
            )
        )
        assertTrue(
            results.contains(
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

        val results = search(fixture, SearchFilterText.Contains("cook"))

        assertTrue(results.contains(Hit.Model(refs.cooking.key)))
    }

    @Test
    fun `text contains matches object keys`() {
        val fixture = SearchFixture.builder()
            .addCrmCookingAndTags()
            .build()

        val results = search(fixture, SearchFilterText.Contains("fingerprint"))

        assertTrue(
            results.containsExactly(
                setOf(
                    Hit.EntityAttribute(refs.cooking.key, refs.cooking.chef.key, refs.cooking.chef.attr.fingerprint.key)
                )
            )
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

        val results = search(fixture, SearchFilterText.Contains("calendar"))

        assertTrue(
            results.containsExactly(
                setOf(
                Hit.RelationshipAttribute(
                    refs.cooking.key,
                    refs.cooking.author.key,
                    refs.cooking.author.attr.date.key
                )
                )
            )
        )
    }

    @Test
    fun `rename model refreshes denormalized model labels across matching search rows`() {
        val fixture = SearchFixture.builder()
            .addCrmCookingAndTags()
            .build()

        fixture.env.dispatch(
            ModelAction.Model_UpdateName(
                modelRef = refs.cooking.ref,
                value = LocalizedTextNotLocalized("Cooking (canonical)")
            )
        )

        val results = search(fixture, SearchFilterTags.NotEmpty)
        val cookingLabels = mutableSetOf<String>()
        results.forEachModelItems(refs.cooking.key) { row ->
            cookingLabels.add(row.modelLabel)
        }

        assertEquals(
            setOf("Cooking (canonical)"),
            cookingLabels
        )
    }

    @Test
    fun `rename entity refreshes denormalized entity labels across matching search rows`() {
        val fixture = SearchFixture.builder()
            .addCrmCookingAndTags()
            .build()

        fixture.env.dispatch(
            ModelAction.Entity_UpdateName(
                modelRef = refs.cooking.ref,
                entityRef = refs.cooking.chef.ref,
                value = LocalizedTextNotLocalized("Chef (canonical)")
            )
        )

        val results = search(fixture, SearchFilterTags.NotEmpty)
        val chefLabels = mutableSetOf<String>()
        results.forEachEntityItems(refs.cooking.key, refs.cooking.chef.key) { row ->
            if (row is SearchRow.Entity) {
                chefLabels.add(row.entityLabel)
            }
            if (row is SearchRow.EntityAttribute) {
                chefLabels.add(row.entityLabel)
            }
        }

        assertEquals(
            setOf("Chef (canonical)"),
            chefLabels
        )
    }

    @Test
    fun `update entity key refreshes denormalized entity keys across matching search rows`() {
        val fixture = SearchFixture.builder()
            .addCrmCookingAndTags()
            .build()
        val newEntityKey = EntityKey("chef-canonical")

        fixture.env.dispatch(
            ModelAction.Entity_UpdateKey(
                modelRef = refs.cooking.ref,
                entityRef = refs.cooking.chef.ref,
                value = newEntityKey
            )
        )

        val results = search(fixture, SearchFilterTags.NotEmpty)

        assertFalse(results.contains(Hit.Entity(refs.cooking.key, refs.cooking.chef.key)))
        assertFalse(
            results.contains(
                Hit.EntityAttribute(
                    refs.cooking.key,
                    refs.cooking.chef.key,
                    refs.cooking.chef.attr.name.key
                )
            )
        )
        assertFalse(
            results.contains(
                Hit.EntityAttribute(
                    refs.cooking.key,
                    refs.cooking.chef.key,
                    refs.cooking.chef.attr.email.key
                )
            )
        )
        assertFalse(
            results.contains(
                Hit.EntityAttribute(
                    refs.cooking.key,
                    refs.cooking.chef.key,
                    refs.cooking.chef.attr.fingerprint.key
                )
            )
        )

        assertTrue(results.contains(Hit.Entity(refs.cooking.key, newEntityKey)))
        assertTrue(
            results.contains(
                Hit.EntityAttribute(
                    refs.cooking.key,
                    newEntityKey,
                    refs.cooking.chef.attr.fingerprint.key
                )
            )
        )
    }

    @Test
    fun `rename relationship refreshes denormalized relationship labels across matching search rows`() {
        val fixture = SearchFixture.builder()
            .addCrmCookingAndTags()
            .build()

        fixture.env.dispatch(
            ModelAction.Relationship_UpdateName(
                modelRef = refs.cooking.ref,
                relationshipRef = refs.cooking.usage.ref,
                value = LocalizedTextNotLocalized("Usage (canonical)")
            )
        )

        val results = search(fixture, SearchFilterTags.NotEmpty)
        val usageLabels = mutableSetOf<String>()
        results.forEachRelationshipItems(refs.cooking.key, refs.cooking.usage.key) { row ->
            if (row is SearchRow.Relationship) {
                usageLabels.add(row.relationshipLabel)
            }
            if (row is SearchRow.RelationshipAttribute) {
                usageLabels.add(row.relationshipLabel)
            }
        }

        assertEquals(
            setOf("Usage (canonical)"),
            usageLabels
        )
    }

    @Test
    fun `update relationship key refreshes denormalized relationship keys across matching search rows`() {
        val fixture = SearchFixture.builder()
            .addCrmCookingAndTags()
            .build()
        val newRelationshipKey = RelationshipKey("usage-canonical")

        fixture.env.dispatch(
            ModelAction.Relationship_UpdateKey(
                modelRef = refs.cooking.ref,
                relationshipRef = refs.cooking.usage.ref,
                value = newRelationshipKey
            )
        )

        val results = search(fixture, SearchFilterTags.NotEmpty)

        assertFalse(results.contains(Hit.Relationship(refs.cooking.key, refs.cooking.usage.key)))
        assertFalse(
            results.contains(
                Hit.RelationshipAttribute(
                    refs.cooking.key,
                    refs.cooking.usage.key,
                    refs.cooking.usage.attr.quantity.key
                )
            )
        )
        assertFalse(
            results.contains(
                Hit.RelationshipAttribute(
                    refs.cooking.key,
                    refs.cooking.usage.key,
                    refs.cooking.usage.attr.unit.key
                )
            )
        )

        assertTrue(results.contains(Hit.Relationship(refs.cooking.key, newRelationshipKey)))
        assertTrue(
            results.contains(
                Hit.RelationshipAttribute(
                    refs.cooking.key,
                    newRelationshipKey,
                    refs.cooking.usage.attr.quantity.key
                )
            )
        )
    }

    private fun search(fixture: SearchFixture, filter: SearchFilter): SearchResult {
        return search(
            fixture,
            SearchFilters(
                operator = SearchFiltersLogicalOperator.AND,
                items = listOf(filter)
            )
        )
    }

    private fun search(fixture: SearchFixture, filters: SearchFilters): SearchResult {
        val json = fixture.env.dispatch(
            ModelAction.Search(
                filters = filters,
                fields = SearchFields(emptyList())
            )
        ) as JsonObject
        return SearchResult.fromJson(json)
    }

    private sealed interface Hit {
        data class Model(val modelKey: ModelKey) : Hit
        data class Type(val modelKey: ModelKey, val typeKey: EntityKey) : Hit
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

    private sealed interface SearchRow {
        val id: String
        val objectType: String
        val modelId: ModelId
        val modelKey: ModelKey
        val modelLabel: String

        data class Model(
            override val id: String,
            override val objectType: String,
            override val modelId: ModelId,
            override val modelKey: ModelKey,
            override val modelLabel: String
        ) : SearchRow

        data class Type(
            override val id: String,
            override val objectType: String,
            override val modelId: ModelId,
            override val modelKey: ModelKey,
            override val modelLabel: String,
            val typeId: EntityId,
            val typeKey: EntityKey,
            val typeLabel: String
        ) : SearchRow

        data class Entity(
            override val id: String,
            override val objectType: String,
            override val modelId: ModelId,
            override val modelKey: ModelKey,
            override val modelLabel: String,
            val entityId: EntityId,
            val entityKey: EntityKey,
            val entityLabel: String
        ) : SearchRow

        data class EntityAttribute(
            override val id: String,
            override val objectType: String,
            override val modelId: ModelId,
            override val modelKey: ModelKey,
            override val modelLabel: String,
            val entityId: EntityId,
            val entityKey: EntityKey,
            val entityLabel: String,
            val entityAttributeId: AttributeId,
            val entityAttributeKey: AttributeKey,
            val entityAttributeLabel: String
        ) : SearchRow

        data class Relationship(
            override val id: String,
            override val objectType: String,
            override val modelId: ModelId,
            override val modelKey: ModelKey,
            override val modelLabel: String,
            val relationshipId: RelationshipId,
            val relationshipKey: RelationshipKey,
            val relationshipLabel: String
        ) : SearchRow

        data class RelationshipAttribute(
            override val id: String,
            override val objectType: String,
            override val modelId: ModelId,
            override val modelKey: ModelKey,
            override val modelLabel: String,
            val relationshipId: RelationshipId,
            val relationshipKey: RelationshipKey,
            val relationshipLabel: String,
            val relationshipAttributeId: AttributeId,
            val relationshipAttributeKey: AttributeKey,
            val relationshipAttributeLabel: String
        ) : SearchRow
    }

    private class SearchResult(
        private val rows: Set<SearchRow>
    ) {
        private val hits: Set<Hit> = rows.mapTo(mutableSetOf()) { row ->
            when (row) {
                is SearchRow.Model -> Hit.Model(row.modelKey)
                is SearchRow.Type -> Hit.Type(row.modelKey, row.typeKey)
                is SearchRow.Entity -> Hit.Entity(row.modelKey, row.entityKey)
                is SearchRow.EntityAttribute ->
                    Hit.EntityAttribute(row.modelKey, row.entityKey, row.entityAttributeKey)
                is SearchRow.Relationship -> Hit.Relationship(row.modelKey, row.relationshipKey)
                is SearchRow.RelationshipAttribute ->
                    Hit.RelationshipAttribute(row.modelKey, row.relationshipKey, row.relationshipAttributeKey)
            }
        }

        companion object {
            fun fromJson(results: JsonObject): SearchResult {
                val rows = results.getValue("items").jsonArray.map { item ->
                    val id = item.jsonObject.getValue("id").jsonPrimitive.content
                    val location = item.jsonObject.getValue("location").jsonObject
                    when (val objectType = location.getValue("objectType").jsonPrimitive.content) {
                        "model" -> SearchRow.Model(
                            id = id,
                            objectType = objectType,
                            modelId = ModelId.fromString(location.getValue("modelId").jsonPrimitive.content),
                            modelKey = ModelKey(location.getValue("modelKey").jsonPrimitive.content),
                            modelLabel = location.getValue("modelLabel").jsonPrimitive.content
                        )

                        "type" -> SearchRow.Type(
                            id = id,
                            objectType = objectType,
                            modelId = ModelId.fromString(location.getValue("modelId").jsonPrimitive.content),
                            modelKey = ModelKey(location.getValue("modelKey").jsonPrimitive.content),
                            modelLabel = location.getValue("modelLabel").jsonPrimitive.content,
                            typeId = EntityId.fromString(location.getValue("typeId").jsonPrimitive.content),
                            typeKey = EntityKey(location.getValue("typeKey").jsonPrimitive.content),
                            typeLabel = location.getValue("typeLabel").jsonPrimitive.content
                        )

                        "entity" -> SearchRow.Entity(
                            id = id,
                            objectType = objectType,
                            modelId = ModelId.fromString(location.getValue("modelId").jsonPrimitive.content),
                            modelKey = ModelKey(location.getValue("modelKey").jsonPrimitive.content),
                            modelLabel = location.getValue("modelLabel").jsonPrimitive.content,
                            entityId = EntityId.fromString(location.getValue("entityId").jsonPrimitive.content),
                            entityKey = EntityKey(location.getValue("entityKey").jsonPrimitive.content),
                            entityLabel = location.getValue("entityLabel").jsonPrimitive.content
                        )

                        "entityAttribute" -> SearchRow.EntityAttribute(
                            id = id,
                            objectType = objectType,
                            modelId = ModelId.fromString(location.getValue("modelId").jsonPrimitive.content),
                            modelKey = ModelKey(location.getValue("modelKey").jsonPrimitive.content),
                            modelLabel = location.getValue("modelLabel").jsonPrimitive.content,
                            entityId = EntityId.fromString(location.getValue("entityId").jsonPrimitive.content),
                            entityKey = EntityKey(location.getValue("entityKey").jsonPrimitive.content),
                            entityLabel = location.getValue("entityLabel").jsonPrimitive.content,
                            entityAttributeId = AttributeId.fromString(
                                location.getValue("entityAttributeId").jsonPrimitive.content
                            ),
                            entityAttributeKey = AttributeKey(
                                location.getValue("entityAttributeKey").jsonPrimitive.content
                            ),
                            entityAttributeLabel = location.getValue("entityAttributeLabel").jsonPrimitive.content
                        )

                        "relationship" -> SearchRow.Relationship(
                            id = id,
                            objectType = objectType,
                            modelId = ModelId.fromString(location.getValue("modelId").jsonPrimitive.content),
                            modelKey = ModelKey(location.getValue("modelKey").jsonPrimitive.content),
                            modelLabel = location.getValue("modelLabel").jsonPrimitive.content,
                            relationshipId = RelationshipId.fromString(
                                location.getValue("relationshipId").jsonPrimitive.content
                            ),
                            relationshipKey = RelationshipKey(location.getValue("relationshipKey").jsonPrimitive.content),
                            relationshipLabel = location.getValue("relationshipLabel").jsonPrimitive.content
                        )

                        "relationshipAttribute" -> SearchRow.RelationshipAttribute(
                            id = id,
                            objectType = objectType,
                            modelId = ModelId.fromString(location.getValue("modelId").jsonPrimitive.content),
                            modelKey = ModelKey(location.getValue("modelKey").jsonPrimitive.content),
                            modelLabel = location.getValue("modelLabel").jsonPrimitive.content,
                            relationshipId = RelationshipId.fromString(
                                location.getValue("relationshipId").jsonPrimitive.content
                            ),
                            relationshipKey = RelationshipKey(location.getValue("relationshipKey").jsonPrimitive.content),
                            relationshipLabel = location.getValue("relationshipLabel").jsonPrimitive.content,
                            relationshipAttributeId = AttributeId.fromString(
                                location.getValue("relationshipAttributeId").jsonPrimitive.content
                            ),
                            relationshipAttributeKey = AttributeKey(
                                location.getValue("relationshipAttributeKey").jsonPrimitive.content
                            ),
                            relationshipAttributeLabel = location.getValue("relationshipAttributeLabel").jsonPrimitive.content
                        )

                        else -> error("Unexpected location objectType in model search tests: $objectType")
                    }
                }.toSet()

                return SearchResult(rows)
            }
        }

        fun forEachItem(block: (SearchRow) -> Unit) {
            rows.forEach(block)
        }

        fun forEachHit(block: (Hit) -> Unit) {
            hits.forEach(block)
        }

        fun collectHits(): Set<Hit> {
            return hits
        }

        fun contains(hit: Hit): Boolean {
            return hits.contains(hit)
        }

        fun containsExactly(expected: Set<Hit>): Boolean {
            return hits == expected
        }

        fun containsExactly(expected: SearchResult): Boolean {
            return hits == expected.hits
        }

        fun forEachModelItems(modelKey: ModelKey, block: (SearchRow) -> Unit) {
            rows.forEach { row ->
                if (row.modelKey == modelKey) {
                    block(row)
                }
            }
        }

        fun forEachEntityItems(modelKey: ModelKey, entityKey: EntityKey, block: (SearchRow) -> Unit) {
            rows.filter { row -> row.modelKey == modelKey }
                .filter { row ->
                    (row is SearchRow.Entity && row.entityKey == entityKey) ||
                            (row is SearchRow.EntityAttribute && row.entityKey == entityKey)
                }
                .forEach { row -> block(row) }
        }

        fun forEachRelationshipItems(
            modelKey: ModelKey,
            relationshipKey: RelationshipKey,
            block: (SearchRow) -> Unit
        ) {
            rows.filter { row -> row.modelKey == modelKey }
                .filter { row ->
                    (row is SearchRow.Relationship && row.relationshipKey == relationshipKey) ||
                            (row is SearchRow.RelationshipAttribute && row.relationshipKey == relationshipKey)
                }
                .forEach { row -> block(row) }
        }
    }
}
