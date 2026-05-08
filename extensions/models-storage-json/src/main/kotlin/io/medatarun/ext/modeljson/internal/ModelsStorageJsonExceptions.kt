package io.medatarun.ext.modeljson.internal

import io.medatarun.lang.exceptions.MedatarunTechnicalException

internal class ModelJsonEntityIdentifierAttributeNotFound(attributeKey: String) :
    MedatarunTechnicalException("Error on entity storage. Specified key [$attributeKey] in Json doesn't match any known attribute. Storage needs to be fixed manually.")

internal class ModelJsonEntityAttributeTypeNotFoundException(attributeKey: String, attributeType: String) :
    MedatarunTechnicalException("Error on entity storage. In Json, key [$attributeKey] declares type [$attributeType] that was not found in Json types. Storage needs to be fixed manually.")

internal class ModelJsonReadBusinessKeyAttributeNotFoundException(entity: String, attribute: String) :
    MedatarunTechnicalException("Error while reading model, attribute $attribute not found in entity $entity.")

internal class ModelJsonReadEntityReferencedInRelationshipNotFound(
    relationJsonKey: String,
    relationshipRoleId: String,
    entityJsnoKey: String
) :
    MedatarunTechnicalException("Error while reading model. Relationship [$relationJsonKey] has role [$relationshipRoleId] which references entity with key [$entityJsnoKey] that could not be found.")

internal class ModelJsonReadBusinessKeyEntityReferencedNotFoundException(entityRef: String) :
    MedatarunTechnicalException("Business key references entity $entityRef but this entity is not found.")
