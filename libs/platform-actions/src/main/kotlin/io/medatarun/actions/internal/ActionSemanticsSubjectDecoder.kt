package io.medatarun.actions.internal

import io.medatarun.actions.domain.ActionSemanticsInvalidSubjectFormatException
import io.medatarun.actions.domain.ActionSemanticsSubject
import io.medatarun.actions.domain.ActionSemanticsSubjectReferencingParam

internal class ActionSemanticsSubjectDecoder(val vocabulary: SemanticsVocabulary) {
    fun decodeSubjects(declaredSubjects: List<String>): List<ActionSemanticsSubject> {
        val subjects = mutableListOf<ActionSemanticsSubject>()

        for (declaredSubject in declaredSubjects) {
            val subjectString = declaredSubject.trim()
            if (subjectString.isEmpty()) {
                throw ActionSemanticsInvalidSubjectFormatException(declaredSubject)
            }

            val openParenthesisIndex = subjectString.indexOf('(')
            val closeParenthesisIndex = subjectString.lastIndexOf(')')
            if (openParenthesisIndex <= 0 || closeParenthesisIndex <= openParenthesisIndex) {
                throw ActionSemanticsInvalidSubjectFormatException(declaredSubject)
            }
            if (closeParenthesisIndex != subjectString.length - 1) {
                throw ActionSemanticsInvalidSubjectFormatException(declaredSubject)
            }

            val subjectType = subjectString.substring(0, openParenthesisIndex).trim()
            if (subjectType.isEmpty()) {
                throw ActionSemanticsInvalidSubjectFormatException(declaredSubject)
            }

            val referencingParams = subjectString
                .substring(openParenthesisIndex + 1, closeParenthesisIndex)
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { paramName ->
                    val kind = vocabulary.toReferencingParamKindOptional(paramName)
                        ?: throw ActionSemanticsInvalidSubjectFormatException(declaredSubject)
                    ActionSemanticsSubjectReferencingParam(name = paramName, kind = kind)
                }

            subjects.add(
                ResolvedActionSemanticsSubject(
                    type = subjectType,
                    referencingParams = referencingParams
                )
            )
        }
        return subjects
    }
    private data class ResolvedActionSemanticsSubject(
        override val type: String,
        override val referencingParams: List<ActionSemanticsSubjectReferencingParam>
    ) : ActionSemanticsSubject
}