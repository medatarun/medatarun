package io.medatarun.actions.internal

import io.medatarun.actions.domain.ActionSemanticsSubjectReferencingParamKind
import io.medatarun.actions.ports.needs.ActionDocSemanticsIntent

internal class SemanticsVocabulary(
    val knownSubjects: List<String>,
    val updateIntentSynonyms: List<String>,
    val createIntentSynonyms: List<String>,
    val deleteIntentSynonyms: List<String>,
    val readIntentSynonyms: List<String>
) {
    /**
     * Resolves an intent from a synonym token such as "create" or "add_tag".
     */
    fun lookupIntentBySynonymOptional(synonym: String): ActionDocSemanticsIntent? {
        if (updateIntentSynonyms.contains(synonym)) {
            return ActionDocSemanticsIntent.UPDATE
        }
        if (createIntentSynonyms.contains(synonym)) {
            return ActionDocSemanticsIntent.CREATE
        }
        if (deleteIntentSynonyms.contains(synonym)) {
            return ActionDocSemanticsIntent.DELETE
        }
        if (readIntentSynonyms.contains(synonym)) {
            return ActionDocSemanticsIntent.READ
        }
        return null
    }

    /**
     * Converts a parameter name/token to a referencing kind using the project naming convention.
     */
    fun toReferencingParamKindOptional(value: String): ActionSemanticsSubjectReferencingParamKind? {
        if (value.endsWith("Ref")) {
            return ActionSemanticsSubjectReferencingParamKind.REF
        }
        if (value.endsWith("Id")) {
            return ActionSemanticsSubjectReferencingParamKind.ID
        }
        if (value.endsWith("Key") || value == "key") {
            return ActionSemanticsSubjectReferencingParamKind.KEY
        }
        return null
    }

    fun isKnownSubject(subject: String): Boolean {
        return knownSubjects.contains(subject)
    }
}
