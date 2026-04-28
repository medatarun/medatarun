package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.TypeKey
import io.medatarun.model.domain.TypeRef
import io.medatarun.model.domain.fixtures.ModelTestEnv
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@EnableDatabaseTests
class Type_UpdateDescription_Test {

    @Test
    fun `update type description`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("m1")
        env.modelCreate(modelRef.key)
        val typeKey = TypeKey("String")
        val typeRef = TypeRef.ByKey(typeKey)
        env.dispatch(ModelAction.Type_Create(modelRef, typeKey, null, null))
        env.dispatch(
            ModelAction.Type_UpdateDescription(
                modelRef = modelRef,
                typeRef = typeRef,
                value = LocalizedMarkdown("This is a string")
            )
        )
        val t = env.queries.findTypeOptional(modelRef, typeRef)
        assertNotNull(t)
        assertEquals(LocalizedMarkdown("This is a string"), t.description)
    }

    @Test
    fun `update type description with null`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("m1")
        env.modelCreate(modelRef.key)
        val typeKey = TypeKey("String")
        val typeRef = TypeRef.ByKey(typeKey)
        env.dispatch(ModelAction.Type_Create(modelRef, typeKey, null, null))
        env.dispatch(ModelAction.Type_UpdateDescription(modelRef, typeRef, null))
        val t = env.queries.findTypeOptional(modelRef, typeRef)
        assertNotNull(t)
        assertNull(t.description)
    }
}
