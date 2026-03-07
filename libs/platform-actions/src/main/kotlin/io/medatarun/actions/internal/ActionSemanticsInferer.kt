package io.medatarun.actions.internal

import io.medatarun.actions.domain.ActionCmdDescriptor
import io.medatarun.actions.domain.ActionSemanticsInvalidSubjectFormatException
import io.medatarun.actions.domain.ActionSemantics
import io.medatarun.actions.domain.ActionSemanticsConfig
import io.medatarun.actions.domain.ActionSemanticsSubject
import io.medatarun.actions.ports.needs.ActionDocSemanticsIntent

class ActionSemanticsInferer {
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
        val subjects = decodeSubjects(declaration.subjects)

        return object : ActionSemantics {
            override val intent: ActionDocSemanticsIntent
                get() = declaration.intent
            override val subjects: List<ActionSemanticsSubject>
                get() = subjects
        }
    }


    private fun createSemanticsUnknwon(
        action: ActionCmdDescriptor,
        declaration: ActionSemanticsConfig.Unknown
    ): ActionSemantics {
        return object : ActionSemantics {
            override val intent: ActionDocSemanticsIntent
                get() = ActionDocSemanticsIntent.OTHER
            override val subjects: List<ActionSemanticsSubject>
                get() = emptyList()
        }

    }

    private fun createSemanticsInferred(
        action: ActionCmdDescriptor,
        declaration: ActionSemanticsConfig.Auto
    ): ActionSemantics {
        return object : ActionSemantics {
            override val intent: ActionDocSemanticsIntent
                get() = ActionDocSemanticsIntent.OTHER
            override val subjects: List<ActionSemanticsSubject>
                get() = emptyList()
        }

    }

    private fun createSemanticsNone(
        action: ActionCmdDescriptor,
        declaration: ActionSemanticsConfig.None
    ): ActionSemantics {
        return object : ActionSemantics {
            override val intent: ActionDocSemanticsIntent
                get() = ActionDocSemanticsIntent.OTHER
            override val subjects: List<ActionSemanticsSubject>
                get() = emptyList()
        }
    }

    companion object {
        fun decodeSubjects(declaredSubjects: List<String>): MutableList<ActionSemanticsSubject> {
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

                val type = subjectString.substring(0, openParenthesisIndex).trim()
                if (type.isEmpty()) {
                    throw ActionSemanticsInvalidSubjectFormatException(declaredSubject)
                }

                val referencingParams = subjectString
                    .substring(openParenthesisIndex + 1, closeParenthesisIndex)
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                subjects.add(object : ActionSemanticsSubject {
                    override val type: String
                        get() = type
                    override val referencingParams: List<String>
                        get() = referencingParams
                })
            }
            return subjects
        }

    }
}
