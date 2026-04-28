package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.domain.TextMarkdown
import io.medatarun.model.domain.TextSingleLine
import io.medatarun.model.domain.ModelNotFoundException
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.TypeCreateDuplicateException
import io.medatarun.model.domain.TypeKey
import io.medatarun.model.domain.TypeRef
import io.medatarun.model.domain.fixtures.ModelTestEnv
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@EnableDatabaseTests
class Type_Create_Test {

    @Test
    fun `create type`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("m1")
        env.modelCreate(modelRef.key)
        env.dispatch(
            ModelAction.Type_Create(
                modelRef = modelRef,
                typeKey = TypeKey("String"),
                name = TextSingleLine("Simple string"),
                description = TextMarkdown("Simple string description")
            )
        )

        assertEquals(1, env.queries.findTypes(modelRef).size)

        val type = env.queries.findTypeOptional(modelRef, TypeRef.typeRefKey(TypeKey("String")))
        assertNotNull(type)
        assertEquals(TextSingleLine("Simple string"), type.name)
        assertEquals(TextMarkdown("Simple string description"), type.description)
    }

    @Test
    fun `create type without name and description`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("m1")
        env.modelCreate(modelRef.key)
        env.dispatch(
            ModelAction.Type_Create(
                modelRef = modelRef,
                typeKey = TypeKey("String"),
                name = null,
                description = null
            )
        )
        val types = env.queries.findTypes(modelRef)
        assertEquals(1, types.size)

        val type = env.queries.findTypeOptional(modelRef, TypeRef.typeRefKey(TypeKey("String")))
        assertNotNull(type)
        assertNull(type.name)
        assertNull(type.description)
    }

    @Test
    fun `create type on unknown model throw ModelNotFoundException`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("m1")
        env.modelCreate(modelRef.key)
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
        val env = ModelTestEnv()
        val modelRef = modelRefKey("m1")
        env.modelCreate(modelRef.key)
        env.dispatch(ModelAction.Type_Create(modelRef, TypeKey("String"), null, null))
        assertThrows<TypeCreateDuplicateException> {
            env.dispatch(
                ModelAction.Type_Create(
                    modelRef = modelRef,
                    typeKey = TypeKey("String"),
                    name = null,
                    description = null
                )
            )
        }
    }
}
