package io.medatarun.actions.internal

import io.medatarun.actions.domain.*
import io.medatarun.actions.ports.needs.ActionDocSemanticsIntent

class ActionSemanticsResolver {

    val vocabulary = buildDefaultVocabulary()
    val subjectDecoder = ActionSemanticsSubjectDecoder(vocabulary)

    fun createSemantics(action: ActionCmdDescriptor): ActionSemantics {
        val declaration = action.semantics
        return when (declaration) {
            is ActionSemanticsConfig.None -> createSemanticsNone(action, declaration)
            is ActionSemanticsConfig.Auto -> createSemanticsInferred(action, declaration)
            is ActionSemanticsConfig.Unknown -> createSemanticsUnknwon(action, declaration)
            is ActionSemanticsConfig.Declared -> createSemanticsDeclared(action, declaration)
        }
    }

    private fun createSemanticsDeclared(
        action: ActionCmdDescriptor,
        declaration: ActionSemanticsConfig.Declared
    ): ActionSemantics {
        return ActionSemanticsResolved(
            intent = declaration.intent,
            subjects = subjectDecoder.decodeSubjects(declaration.subjects),
            returns = normalizeReturns(declaration.returns)
        )
    }

    private fun createSemanticsUnknwon(
        action: ActionCmdDescriptor,
        declaration: ActionSemanticsConfig.Unknown
    ): ActionSemantics {
        return ActionSemanticsResolved(intent = ActionDocSemanticsIntent.OTHER,
            subjects = emptyList(),
            returns = emptyList()
        )
    }

    private fun createSemanticsInferred(
        action: ActionCmdDescriptor,
        declaration: ActionSemanticsConfig.Auto
    ): ActionSemantics {

        return ActionSemanticsInferer(vocabulary).infer(action)
    }

    private fun createSemanticsNone(
        action: ActionCmdDescriptor,
        declaration: ActionSemanticsConfig.None
    ): ActionSemantics {
        return ActionSemanticsResolved(
            intent = ActionDocSemanticsIntent.OTHER,
            subjects = emptyList(),
            returns = emptyList()
        )
    }

    companion object {

        private fun buildDefaultVocabulary(): SemanticsVocabulary {
            return SemanticsVocabulary(
                knownSubjects = listOf(
                    "actor",
                    "tag",
                    "tag_free",
                    "tag_managed",
                    "tag_group",
                    "model",
                    "type",
                    "entity",
                    "entity_attribute",
                    "relationship",
                    "relationship_attribute",
                    "user"
                ),
                updateIntentSynonyms = listOf("update", "add_tag", "delete_tag", "disable", "enable", "set_roles"),
                createIntentSynonyms = listOf("create", "copy", "import"),
                deleteIntentSynonyms = listOf("delete"),
                readIntentSynonyms = listOf("list", "export", "search", "inspect", "get")
            )
        }

        private data class ActionSemanticsResolved(
            override val intent: ActionDocSemanticsIntent,
            override val subjects: List<ActionSemanticsSubject>,
            override val returns: List<String>
        ) : ActionSemantics

        /**
         * Returns are declared as simple type tokens.
         * They are normalized for transport but intentionally not validated
         * against reference syntax.
         */
        private fun normalizeReturns(returns: List<String>): List<String> {
            return returns.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        }


    }
}
