package io.medatarun.model.domain

fun modelRef(value: String) = ModelRef.ByKey(ModelKey(value))
fun modelRef(value: ModelKey) = ModelRef.ByKey(value)
fun entityRef(value: String) = EntityRef.ByKey(EntityKey(value))
fun entityAttributeRef(value: String) = EntityAttributeRef.ByKey(AttributeKey(value))
fun entityAttributeRef(value: AttributeKey) = EntityAttributeRef.ByKey(value)
fun entityAttributeRef(value: AttributeId) = EntityAttributeRef.ById(value)
fun relAttrRef(value: String) = RelationshipAttributeRef.ByKey(AttributeKey(value))
fun typeRef(value: String) = TypeRef.ByKey(TypeKey(value))
fun typeRef(value: TypeId) = TypeRef.ById(value)
fun typeRef(value: TypeKey) = TypeRef.ByKey(value)