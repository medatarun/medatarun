package io.medatarun.model.domain.diff

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.model.domain.*
import io.medatarun.tags.core.domain.TagId
import java.net.URL

/**
 * ModelDiff is the master domain contract used to compare two [ModelAggregate] states.
 *
 * Why this exists:
 * Medatarun stores domain models as operational data. Models are versioned and must be
 * reviewable like source code. This contract allows the platform to expose model changes
 * in a stable way for both machines and humans.
 *
 * Two comparison scopes are supported:
 * - STRUCTURAL: drift-oriented comparison for system vs canonical checks.
 *   This focuses on structure only (types, entities, relationship roles, attributes, keys,
 *   cardinalities, ownership, required/optional, referenced types, etc.).
 * - COMPLETE: full review comparison.
 *   This includes STRUCTURAL changes plus descriptive/documentation content such as
 *   localized texts, markdown descriptions, tags, and documentation URLs.
 *
 * Typical consumers:
 * - Drift analysis workflows ("does production schema still match canonical model?")
 * - Notifications and change summaries sent to users
 * - Side-by-side UI views with "show only differences"
 *
 * Identity and matching strategy:
 * Diff pairing must be done by domain keys (ModelKey, EntityKey, AttributeKey, ...),
 * not by UUID ids. IDs can be regenerated during import pipelines; keys represent the
 * stable business identity used to align objects across model states.
 *
 * Shape design choice:
 * The contract intentionally exposes only changed entries:
 * - [ModelDiffEntry.Added]
 * - [ModelDiffEntry.Deleted]
 * - [ModelDiffEntry.Modified]
 * No unchanged entry is emitted.
 *
 * About snapshots:
 * A snapshot is the value object for one side (left or right) at one logical location.
 * For one diff entry:
 * - Added   => right exists
 * - Deleted => left exists
 * - Modified=> left and right exist
 * This allows callers to render side-by-side details without fetching extra model data.
 *
 * Evolution policy:
 * If new fields are added to domain objects later, they become diffable only when added
 * to snapshots and comparison rules. This keeps behavior explicit and avoids hidden diffs.
 */

/**
 * Scope applied by the diff engine.
 *
 * STRUCTURAL compares only structural properties.
 * COMPLETE compares structural properties plus descriptive content (texts, tags, docs).
 */
enum class ModelDiffScope {
    STRUCTURAL,
    COMPLETE;

    val code: String
        get() = when (this) {
            STRUCTURAL -> "structural"
            COMPLETE -> "complete"
        }

    companion object {
        private val map = entries.associateBy(ModelDiffScope::code)

        fun valueOfCodeOptional(code: String): ModelDiffScope? {
            return map[code]
        }

        fun valueOfCode(code: String): ModelDiffScope {
            return valueOfCodeOptional(code) ?: throw ModelDiffScopeIllegalCodeException(code)
        }
    }
}

class ModelDiffScopeIllegalCodeException(code: String) :
    MedatarunException("Unknown model diff scope code: $code")

/**
 * Diff result for two model references.
 *
 * The details are carried by [entries]. Aggregate numbers are intentionally not duplicated
 * in this contract because they can be derived from entries when needed.
 */
data class ModelDiff(
    val scopeApplied: ModelDiffScope,
    val left: ModelDiffModelSide,
    val right: ModelDiffModelSide,
    val entries: List<ModelDiffEntry>
)

data class ModelDiffModelSide(
    val modelId: ModelId,
    val modelKey: ModelKey,
    val modelVersion: ModelVersion,
    val modelAuthority: ModelAuthority
)

/**
 * One changed element in the model.
 */
sealed interface ModelDiffEntry {
    val location: ModelDiffLocation

    data class Added(
        override val location: ModelDiffLocation,
        val right: ModelDiffSnapshot
    ) : ModelDiffEntry

    data class Deleted(
        override val location: ModelDiffLocation,
        val left: ModelDiffSnapshot
    ) : ModelDiffEntry

    data class Modified(
        override val location: ModelDiffLocation,
        val left: ModelDiffSnapshot,
        val right: ModelDiffSnapshot
    ) : ModelDiffEntry
}

sealed class ModelDiffLocation(val objectType: String)

data class ModelDiffModelLocation(
    val modelKey: ModelKey
) : ModelDiffLocation("model")

data class ModelDiffTypeLocation(
    val modelKey: ModelKey,
    val typeKey: TypeKey
) : ModelDiffLocation("type")

data class ModelDiffEntityLocation(
    val modelKey: ModelKey,
    val entityKey: EntityKey
) : ModelDiffLocation("entity")

data class ModelDiffEntityAttributeLocation(
    val modelKey: ModelKey,
    val entityKey: EntityKey,
    val attributeKey: AttributeKey
) : ModelDiffLocation("entityAttribute")

data class ModelDiffRelationshipLocation(
    val modelKey: ModelKey,
    val relationshipKey: RelationshipKey
) : ModelDiffLocation("relationship")

data class ModelDiffRelationshipRoleLocation(
    val modelKey: ModelKey,
    val relationshipKey: RelationshipKey,
    val roleKey: RelationshipRoleKey
) : ModelDiffLocation("relationshipRole")

data class ModelDiffRelationshipAttributeLocation(
    val modelKey: ModelKey,
    val relationshipKey: RelationshipKey,
    val attributeKey: AttributeKey
) : ModelDiffLocation("relationshipAttribute")

/**
 * Snapshot = value object captured for one side of the comparison at one location.
 *
 * The same location can therefore provide two snapshots:
 * one from left model state and one from right model state.
 */
sealed interface ModelDiffSnapshot {
    val objectType: String
}

data class ModelDiffModelSnapshot(
    val key: ModelKey,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?,
    val version: ModelVersion,
    val origin: ModelOrigin,
    val authority: ModelAuthority,
    val documentationHome: URL?,
    val tags: List<TagId>
) : ModelDiffSnapshot {
    override val objectType: String = "model"
}

data class ModelDiffTypeSnapshot(
    val key: TypeKey,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?
) : ModelDiffSnapshot {
    override val objectType: String = "type"
}

data class ModelDiffEntitySnapshot(
    val key: EntityKey,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?,
    val identifierAttributeKey: AttributeKey,
    val origin: EntityOrigin,
    val documentationHome: URL?,
    val tags: List<TagId>
) : ModelDiffSnapshot {
    override val objectType: String = "entity"
}

data class ModelDiffEntityAttributeSnapshot(
    val key: AttributeKey,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?,
    val typeKey: TypeKey,
    val optional: Boolean,
    val tags: List<TagId>
) : ModelDiffSnapshot {
    override val objectType: String = "entityAttribute"
}

data class ModelDiffRelationshipSnapshot(
    val key: RelationshipKey,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?,
    val tags: List<TagId>
) : ModelDiffSnapshot {
    override val objectType: String = "relationship"
}

data class ModelDiffRelationshipRoleSnapshot(
    val key: RelationshipRoleKey,
    val entityKey: EntityKey,
    val name: LocalizedText?,
    val cardinality: RelationshipCardinality
) : ModelDiffSnapshot {
    override val objectType: String = "relationshipRole"
}

data class ModelDiffRelationshipAttributeSnapshot(
    val key: AttributeKey,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?,
    val typeKey: TypeKey,
    val optional: Boolean,
    val tags: List<TagId>
) : ModelDiffSnapshot {
    override val objectType: String = "relationshipAttribute"
}
