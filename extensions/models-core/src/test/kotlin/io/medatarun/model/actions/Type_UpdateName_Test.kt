package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.domain.LocalizedTextNotLocalized
import io.medatarun.model.domain.TypeKey
import io.medatarun.model.domain.TypeRef
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@EnableDatabaseTests
class Type_UpdateName_Test {

    @Test
    fun `update type name `() {
        val env = TestEnvTypes()
        val typeRef = TypeRef.typeRefKey(TypeKey("String"))
        env.dispatch(ModelAction.Type_Create(env.modelRef, typeRef.key, null, null))
        env.dispatch(
            ModelAction.Type_UpdateName(
                modelRef = env.modelRef,
                typeRef = typeRef,
                value = LocalizedTextNotLocalized("This is a string")
            )
        )
        val t = env.query.findTypeOptional(env.modelRef, typeRef)
        assertNotNull(t)
        assertEquals(LocalizedTextNotLocalized("This is a string"), t.name)
    }

    @Test
    fun `update type name with null`() {
        val env = TestEnvTypes()
        val typeRef = TypeRef.typeRefKey(TypeKey("String"))
        env.dispatch(ModelAction.Type_Create(env.modelRef, typeRef.key, null, null))
        env.dispatch(ModelAction.Type_UpdateName(env.modelRef, typeRef, null))
        val t = env.query.findTypeOptional(env.modelRef, typeRef)
        assertNotNull(t)
        assertNull(t.name)
    }

}