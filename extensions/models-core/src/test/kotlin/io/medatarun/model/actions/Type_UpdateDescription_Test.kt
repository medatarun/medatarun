package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.actions.ModelAction
import io.medatarun.model.domain.LocalizedMarkdownNotLocalized
import io.medatarun.model.domain.TypeKey
import io.medatarun.model.domain.TypeRef
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@EnableDatabaseTests
class Type_UpdateDescription_Test {

    @Test
    fun `update type description`() {
        val env = TestEnvTypes()
        val typeKey = TypeKey("String")
        val typeRef = TypeRef.ByKey(typeKey)
        env.dispatch(ModelAction.Type_Create(env.modelRef, typeKey, null, null))
        env.dispatch(
            ModelAction.Type_UpdateDescription(
                modelRef = env.modelRef,
                typeRef = typeRef,
                value = LocalizedMarkdownNotLocalized("This is a string")
            )
        )
        val t = env.model.findTypeOptional(typeRef)
        assertNotNull(t)
        assertEquals(LocalizedMarkdownNotLocalized("This is a string"), t.description)
    }

    @Test
    fun `update type description with null`() {
        val env = TestEnvTypes()
        val typeKey = TypeKey("String")
        val typeRef = TypeRef.ByKey(typeKey)
        env.dispatch(ModelAction.Type_Create(env.modelRef, typeKey, null, null))
        env.dispatch(ModelAction.Type_UpdateDescription(env.modelRef, typeRef, null))
        val t = env.model.findTypeOptional(typeRef)
        assertNotNull(t)
        assertNull(t.description)
    }
}