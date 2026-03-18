package io.medatarun.model.actions

import io.medatarun.model.actions.ModelAction
import io.medatarun.model.actions.ModelAction.EntityAttribute_UpdateType
import io.medatarun.model.domain.TypeKey
import io.medatarun.model.domain.TypeNotFoundException
import io.medatarun.model.domain.entityAttributeRef
import io.medatarun.model.domain.typeRef
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class EntityAttribute_UpdateType_Test {

    @Test
    fun `update attribute type is persisted`() {
        val env = _root_ide_package_.io.medatarun.model.actions.TestEnvEntityAttribute()
        env.addSampleEntity()
        val typeMarkdownKey = TypeKey("Markdown")
        env.dispatch(ModelAction.Type_Create(env.sampleModelRef, typeMarkdownKey, null, null))
        val typeMarkdownId = env.query.findType(env.sampleModelRef, typeRef(typeMarkdownKey)).id
        val type = env.query.findType(env.sampleModelRef, typeRef("String"))
        val attr = env.createAttribute(type = typeRef(type.id))


        val attributeRef = entityAttributeRef(attr.key)
        env.runtime.dispatch(
            EntityAttribute_UpdateType(
                env.sampleModelRef,
                env.sampleEntityRef,
                attributeRef,
                typeRef(typeMarkdownKey)
            )
        )
        val reloaded = env.reloadAttribute(attributeRef)
        assertEquals(typeMarkdownId, reloaded.typeId)
    }

    @Test
    fun `update attribute unknown type then error`() {
        val env = _root_ide_package_.io.medatarun.model.actions.TestEnvEntityAttribute()
        env.addSampleEntity()
        val type = env.query.findType(env.sampleModelRef, typeRef("String"))
        val attr = env.createAttribute(type = typeRef(type.id))
        assertThrows<TypeNotFoundException> {
            env.dispatch(
                ModelAction.EntityAttribute_UpdateType(
                    env.sampleModelRef,
                    env.sampleEntityRef,
                    entityAttributeRef(attr.key),
                    typeRef("String2")
                )
            )
        }
    }
}