package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
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
import io.medatarun.model.domain.fixtures.ModelTestEnv
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@EnableDatabaseTests
class Type_Delete_Test {

    @Test
    fun `delete type model not found`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("m1")
        env.modelCreate(modelRef.key)
        val typeKey = TypeKey("String")
        val typeRef = TypeRef.ByKey(typeKey)
        env.dispatch(ModelAction.Type_Create(modelRef, typeKey, null, null))
        assertThrows<ModelNotFoundException> {
            env.dispatch(ModelAction.Type_Delete(modelRefKey("unknown"), typeRef))
        }
    }

    @Test
    fun `delete type type not found`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("m1")
        env.modelCreate(modelRef.key)
        val typeKey = TypeKey("String")
        val typeKeyWrong = TypeKey("String2")
        val typeRefWrong = TypeRef.ByKey(typeKeyWrong)
        env.dispatch(ModelAction.Type_Create(modelRef, typeKey, null, null))
        assertThrows<TypeNotFoundException> {
            env.dispatch(ModelAction.Type_Delete(modelRef, typeRefWrong))
        }
    }

    @Test
    fun `delete type used in entity or relationship attributes then error`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("m1")
        env.modelCreate(modelRef.key)

        val typeString = TypeRef.typeRefKey(TypeKey("String"))
        val typeMarkdown = TypeRef.typeRefKey(TypeKey("Markdown"))
        val typePhoneNumber = TypeRef.typeRefKey(TypeKey("PhoneNumber"))
        val typeInt = TypeRef.typeRefKey(TypeKey("Int"))

        env.dispatch(ModelAction.Type_Create(modelRef, typeString.key, null, null))
        env.dispatch(ModelAction.Type_Create(modelRef, typeMarkdown.key, null, null))
        env.dispatch(ModelAction.Type_Create(modelRef, typePhoneNumber.key, null, null))
        env.dispatch(ModelAction.Type_Create(modelRef, typeInt.key, null, null))
        val entityKey = EntityKey("contact")
        val entityRef = EntityRef.ByKey(entityKey)
        val relationshipKey = RelationshipKey("rel")
        val relationshipRef = RelationshipRef.ByKey(relationshipKey)

        env.dispatch(
            ModelAction.Entity_Create2(
                modelRef = modelRef,
                entityKey = entityKey,
                name = null,
                description = null,
                documentationHome = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeKey = AttributeKey("name"),
                type = typeString,
                optional = false,
                name = null,
                description = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeKey = AttributeKey("infos"),
                type = typeMarkdown,
                optional = false,
                name = null,
                description = null
            )
        )
        env.dispatch(
            ModelAction.Relationship_Create(
                modelRef = modelRef,
                relationshipKey = relationshipRef.key,
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
                modelRef = modelRef,
                relationshipRef = relationshipRef,
                attributeKey = AttributeKey("attr"),
                optional = true,
                type = typePhoneNumber,
                name = null,
                description = null
            )
        )

        assertThrows<ModelTypeDeleteUsedException> {
            env.dispatch(ModelAction.Type_Delete(modelRef, typeString))
        }
        assertThrows<ModelTypeDeleteUsedException> {
            env.dispatch(ModelAction.Type_Delete(modelRef, typeMarkdown))
        }
        assertThrows<ModelTypeDeleteUsedException> {
            env.dispatch(ModelAction.Type_Delete(modelRef, typePhoneNumber))
        }
        env.dispatch(ModelAction.Type_Delete(modelRef, typeInt))

    }

    @Test
    fun `delete type success`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("m1")
        env.modelCreate(modelRef.key)
        val typeString = TypeRef.typeRefKey(TypeKey("String"))
        val typeMarkdown = TypeRef.typeRefKey(TypeKey("Markdown"))
        val typeInt = TypeRef.typeRefKey(TypeKey("Int"))
        env.dispatch(ModelAction.Type_Create(modelRef, typeString.key, null, null))
        env.dispatch(ModelAction.Type_Create(modelRef, typeMarkdown.key, null, null))
        env.dispatch(ModelAction.Type_Create(modelRef, typeInt.key, null, null))
        env.dispatch(ModelAction.Type_Delete(modelRef, typeInt))
        assertNull(env.queries.findTypeOptional(modelRef, typeInt))
        assertNotNull(env.queries.findTypeOptional(modelRef, typeString))
        assertNotNull(env.queries.findTypeOptional(modelRef, typeMarkdown))

    }

}
