package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.actions.ModelAction
import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.EntityRef
import io.medatarun.model.domain.ModelNotFoundException
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.ModelTypeDeleteUsedException
import io.medatarun.model.domain.RelationshipCardinality
import io.medatarun.model.domain.RelationshipKey
import io.medatarun.model.domain.RelationshipRef
import io.medatarun.model.domain.RelationshipRoleKey
import io.medatarun.model.domain.TypeKey
import io.medatarun.model.domain.TypeNotFoundException
import io.medatarun.model.domain.TypeRef
import io.medatarun.model.domain.typeRef
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@EnableDatabaseTests
class Type_Delete_Test {

    @Test
    fun `delete type model not found`() {
        val env = TestEnvTypes()
        val typeKey = TypeKey("String")
        val typeRef = TypeRef.ByKey(typeKey)
        env.dispatch(ModelAction.Type_Create(env.modelRef, typeKey, null, null))
        assertThrows<ModelNotFoundException> {
            env.dispatch(ModelAction.Type_Delete(modelRefKey("unknown"), typeRef))
        }
    }

    @Test
    fun `delete type type not found`() {
        val env = TestEnvTypes()
        val typeKey = TypeKey("String")
        val typeKeyWrong = TypeKey("String2")
        val typeRefWrong = TypeRef.ByKey(typeKeyWrong)
        env.dispatch(ModelAction.Type_Create(env.modelRef, typeKey, null, null))
        assertThrows<TypeNotFoundException> {
            env.dispatch(ModelAction.Type_Delete(env.modelRef, typeRefWrong))
        }
    }

    @Test
    fun `delete type used in entity or relationship attributes then error`() {
        val env = TestEnvTypes()
        env.dispatch(ModelAction.Type_Create(env.modelRef, TypeKey("String"), null, null))
        env.dispatch(ModelAction.Type_Create(env.modelRef, TypeKey("Markdown"), null, null))
        env.dispatch(ModelAction.Type_Create(env.modelRef, TypeKey("PhoneNumber"), null, null))
        env.dispatch(ModelAction.Type_Create(env.modelRef, TypeKey("Int"), null, null))
        val entityKey = EntityKey("contact")
        val entityRef = EntityRef.ByKey(entityKey)
        val relationshipKey = RelationshipKey("rel")
        val relationshipRef = RelationshipRef.ByKey(relationshipKey)
        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = env.modelRef,
                entityKey = entityKey,
                name = null,
                description = null,
                identityAttributeKey = AttributeKey("name"),
                identityAttributeType = typeRef("String"),
                identityAttributeName = null,
                documentationHome = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = env.modelRef,
                entityRef = entityRef,
                attributeKey = AttributeKey("infos"),
                type = TypeRef.ByKey(TypeKey("Markdown")),
                optional = false,
                name = null,
                description = null
            )
        )
        env.dispatch(
            ModelAction.Relationship_Create(
                modelRef = env.modelRef,
                relationshipKey = relationshipKey,
                name = null,
                description = null,
                roleAKey = RelationshipRoleKey("a"),
                roleAEntityRef = entityRef,
                roleAName = null,
                roleACardinality = RelationshipCardinality.One,
                roleBKey = RelationshipRoleKey("b"),
                roleBEntityRef = entityRef,
                roleBName = null,
                roleBCardinality = RelationshipCardinality.One,
            )
        )

        env.dispatch(
            ModelAction.RelationshipAttribute_Create(
                modelRef = env.modelRef,
                relationshipRef = relationshipRef,
                attributeKey = AttributeKey("attr"),
                optional = true,
                type = TypeRef.ByKey(TypeKey("PhoneNumber")),
                name = null,
                description = null
            )
        )

        assertThrows<ModelTypeDeleteUsedException> {
            env.dispatch(ModelAction.Type_Delete(env.modelRef, typeRef("String")))
        }
        assertThrows<ModelTypeDeleteUsedException> {
            env.dispatch(ModelAction.Type_Delete(env.modelRef, TypeRef.ByKey(TypeKey("Markdown"))))
        }
        assertThrows<ModelTypeDeleteUsedException> {
            env.dispatch(ModelAction.Type_Delete(env.modelRef, TypeRef.ByKey(TypeKey("PhoneNumber"))))
        }
        env.dispatch(ModelAction.Type_Delete(env.modelRef, TypeRef.ByKey(TypeKey("Int"))))

    }

    @Test
    fun `delete type success`() {
        val env = TestEnvTypes()
        env.dispatch(ModelAction.Type_Create(env.modelRef, TypeKey("String"), null, null))
        env.dispatch(ModelAction.Type_Create(env.modelRef, TypeKey("Markdown"), null, null))
        env.dispatch(ModelAction.Type_Create(env.modelRef, TypeKey("Int"), null, null))
        env.dispatch(ModelAction.Type_Delete(env.modelRef, TypeRef.ByKey(TypeKey("Int"))))
        assertNull(env.model.findTypeOptional(TypeRef.ByKey(TypeKey("Int"))))
        assertNotNull(env.model.findTypeOptional(typeRef("String")))
        assertNotNull(env.model.findTypeOptional(TypeRef.ByKey(TypeKey("Markdown"))))

    }

}