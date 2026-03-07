package io.medatarun.actions.internal

import io.medatarun.actions.domain.ActionCmdDescriptor
import io.medatarun.actions.domain.ActionSemantics
import io.medatarun.actions.domain.ActionSemanticsAutoInferenceException
import io.medatarun.actions.domain.ActionSemanticsSubject
import io.medatarun.actions.domain.ActionSemanticsSubjectReferencingParam
import io.medatarun.actions.ports.needs.ActionDocSemanticsIntent

/**
 * AUTO mode semantics inference only.
 */
class ActionSemanticsInferer(
    private val vocabulary: SemanticsVocabulary
) {
    fun infer(action: ActionCmdDescriptor): ActionSemantics {
        val intent = inferIntent(action.key)
        val subjectType = inferSubject(action.key)
        val referencingParams = inferReferencingParams(action)

        if (
            (intent == ActionDocSemanticsIntent.UPDATE || intent == ActionDocSemanticsIntent.DELETE) &&
            referencingParams.isEmpty()
        ) {
            throw ActionSemanticsAutoInferenceException(
                actionKey = action.key,
                reason = "no reference parameter found for update/delete action"
            )
        }

        return semantics(
            intent = intent,
            subjects = listOf(
                InferredActionSemanticsSubject(
                    type = subjectType,
                    referencingParams = referencingParams
                )
            ),
            returns = listOf(subjectType)
        )
    }

    private fun inferIntent(actionKey: String): ActionDocSemanticsIntent {
        val candidates = verbCandidates(tokenizeKey(actionKey))
        for (candidate in candidates) {
            val intent = vocabulary.lookupIntentBySynonymOptional(candidate)
            if (intent != null) {
                return intent
            }
        }
        throw ActionSemanticsAutoInferenceException(
            actionKey = actionKey,
            reason = "unknown action verb"
        )
    }

    private fun inferSubject(actionKey: String): String {
        val tokens = tokenizeKey(actionKey)
        var prefixLength = tokens.size
        while (prefixLength >= 1) {
            val candidate = tokens.take(prefixLength).joinToString("_")
            if (vocabulary.isKnownSubject(candidate)) {
                return candidate
            }
            prefixLength -= 1
        }
        throw ActionSemanticsAutoInferenceException(
            actionKey = actionKey,
            reason = "unknown action subject"
        )
    }

    private fun inferReferencingParams(
        action: ActionCmdDescriptor
    ): List<ActionSemanticsSubjectReferencingParam> {
        val referencingParams = mutableListOf<ActionSemanticsSubjectReferencingParam>()
        for (parameter in action.parameters) {
            val kind = vocabulary.toReferencingParamKindOptional(parameter.name)
            if (kind != null) {
                referencingParams.add(
                    ActionSemanticsSubjectReferencingParam(
                        name = parameter.name,
                        kind = kind
                    )
                )
            }
        }
        return referencingParams.distinctBy { it.name }
    }

    private fun tokenizeKey(actionKey: String): List<String> {
        return actionKey.split("_").filter { it.isNotEmpty() }
    }

    private fun verbCandidates(tokens: List<String>): List<String> {
        val candidates = mutableListOf<String>()
        var index = 0
        while (index < tokens.size - 1) {
            candidates.add(tokens[index] + "_" + tokens[index + 1])
            index += 1
        }
        candidates.addAll(tokens)
        return candidates
    }

    private fun semantics(
        intent: ActionDocSemanticsIntent,
        subjects: List<ActionSemanticsSubject>,
        returns: List<String>
    ): ActionSemantics {
        return InferredActionSemantics(intent = intent, subjects = subjects, returns = returns)
    }

    private data class InferredActionSemantics(
        override val intent: ActionDocSemanticsIntent,
        override val subjects: List<ActionSemanticsSubject>,
        override val returns: List<String>
    ) : ActionSemantics

    private data class InferredActionSemanticsSubject(
        override val type: String,
        override val referencingParams: List<ActionSemanticsSubjectReferencingParam>
    ) : ActionSemanticsSubject
}
