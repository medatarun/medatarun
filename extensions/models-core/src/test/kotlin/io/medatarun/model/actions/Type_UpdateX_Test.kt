package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.actions.ModelAction
import io.medatarun.model.domain.ModelNotFoundException
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.TypeKey
import io.medatarun.model.domain.TypeNotFoundException
import io.medatarun.model.domain.TypeRef
import io.medatarun.model.domain.fixtures.ModelTestEnv
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@EnableDatabaseTests
class Type_UpdateX_Test {

    @Test
    fun `update type with model not found`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("m1")
        env.modelCreate(modelRef.key)
        val typeKey = TypeKey("String")
        val typeRef = TypeRef.ByKey(typeKey)
        env.dispatch(ModelAction.Type_Create(modelRef, typeKey, null, null))
        assertThrows<ModelNotFoundException> {
            env.dispatch(
                ModelAction.Type_UpdateDescription(
                    modelRef = modelRefKey("unknown"),
                    typeRef = typeRef,
                    value = null
                )
            )
        }
    }

    @Test
    fun `update type with type not found`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("m1")
        env.modelCreate(modelRef.key)
        val typeKey = TypeKey("String")
        env.dispatch(ModelAction.Type_Create(modelRef, typeKey, null, null))
        assertThrows<TypeNotFoundException> {
            env.dispatch(
                ModelAction.Type_UpdateDescription(
                    modelRef = modelRef,
                    typeRef = TypeRef.ByKey(TypeKey("String2")),
                    value = null
                )
            )
        }
    }

}
