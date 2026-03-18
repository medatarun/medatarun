package io.medatarun.model.actions

import io.medatarun.model.actions.ModelAction
import io.medatarun.model.domain.LocalizedMarkdownNotLocalized
import io.medatarun.model.domain.LocalizedTextNotLocalized
import io.medatarun.model.domain.ModelNotFoundException
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.TypeCreateDuplicateException
import io.medatarun.model.domain.TypeKey
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class Type_Create_Test {

    @Test
    fun `create type`() {
        val env = TestEnvTypes()
        env.runtime.dispatch(
            ModelAction.Type_Create(
                modelRef = env.modelRef,
                typeKey = TypeKey("String"),
                name = LocalizedTextNotLocalized("Simple string"),
                description = LocalizedMarkdownNotLocalized("Simple string description")
            )
        )
        assertEquals(1, env.model.types.size)
        val type = env.model.findTypeOptional(TypeKey("String"))
        assertNotNull(type)
        assertEquals(LocalizedTextNotLocalized("Simple string"), type.name)
        assertEquals(LocalizedMarkdownNotLocalized("Simple string description"), type.description)
    }

    @Test
    fun `create type without name and description`() {
        val env = TestEnvTypes()
        env.runtime.dispatch(
            ModelAction.Type_Create(
                modelRef = env.modelRef,
                typeKey = TypeKey("String"),
                name = null,
                description = null
            )
        )
        assertEquals(1, env.model.types.size)
        val type = env.model.findTypeOptional(TypeKey("String"))
        assertNotNull(type)
        assertNull(type.name)
        assertNull(type.description)
    }

    @Test
    fun `create type on unknown model throw ModelNotFoundException`() {
        val env = TestEnvTypes()
        assertThrows<ModelNotFoundException> {
            env.dispatch(
                ModelAction.Type_Create(
                    modelRef = modelRefKey("unknown"),
                    typeKey = TypeKey("String"),
                    name = null,
                    description = null
                )
            )
        }
    }

    @Test
    fun `create type with duplicate name throws DuplicateTypeException`() {
        val env = TestEnvTypes()
        env.dispatch(ModelAction.Type_Create(env.modelRef, TypeKey("String"), null, null))
        assertThrows<TypeCreateDuplicateException> {
            env.dispatch(
                ModelAction.Type_Create(
                    modelRef = env.modelRef,
                    typeKey = TypeKey("String"),
                    name = null,
                    description = null
                )
            )
        }
    }
}