package io.medatarun.model.infra.db

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.exceptions.MedatarunTechnicalException
import io.medatarun.model.domain.*


class ModelStorageDbMissingCurrentHeadModelSnapshotException(modelId: ModelId) :
    MedatarunTechnicalException("Could not find CURRENT_HEAD model snapshot for model [${modelId.asString()}]")

class ModelStorageDbMissingReleaseEventException(modelId: ModelId, version: String) :
    MedatarunTechnicalException("Could not find model_release event for model [${modelId.asString()}] and version [$version]")

class ModelStorageDbNoReleaseException(modelId: ModelId) :
    MedatarunTechnicalException("Could not find any model_release event for model [${modelId.asString()}]")

class ModelStorageDbInvalidReleaseEventException(modelId: ModelId, eventId: ModelEventId) :
    MedatarunTechnicalException("model_release event [${eventId.asString()}] for model [${modelId.asString()}] has no model_version.")

class ModelStorageDbMissingTypeSnapshotException(typeId: TypeId) :
    MedatarunTechnicalException("Could not find CURRENT_HEAD type snapshot for type [${typeId.asString()}]")

class ModelStorageDbMissingEntitySnapshotException(entityId: EntityId) :
    MedatarunTechnicalException("Could not find CURRENT_HEAD entity snapshot for entity [${entityId.asString()}]")

class ModelStorageDbMissingAttributeSnapshotException(attributeId: AttributeId) :
    MedatarunTechnicalException("Could not find CURRENT_HEAD attribute snapshot for attribute [${attributeId.asString()}]")

class ModelStorageDbMissingCompatibilityIdentifierPrimaryKeyException(entityId: EntityId) :
    MedatarunTechnicalException(
        "Could not derive entity identifier attribute from compatibility primary key for entity [${entityId.asString()}]."
    )

class ModelStorageDbMissingRelationshipSnapshotException(relationshipId: RelationshipId) :
    MedatarunTechnicalException("Could not find CURRENT_HEAD relationship snapshot for relationship [${relationshipId.asString()}]")

class ModelStorageDbMissingBusinessKeySnapshotException(businessKeyId: BusinessKeyId) :
    MedatarunTechnicalException("Could not find CURRENT_HEAD business key snapshot for business key [${businessKeyId.asString()}]")

class ModelStorageDbUnsupportedProjectedDeleteException(eventType: String) :
    MedatarunTechnicalException("CURRENT_HEAD projector does not handle delete event [$eventType].")


class ModelStorageDbSearchUnknownItemTypeException(itemType: String) :
    MedatarunTechnicalException("Unknown denormalized search item type [$itemType]")

class ModelStorageDbSearchMissingProjectionReferenceException(columnName: String) :
    MedatarunTechnicalException("Search projection is missing required reference column [$columnName]")

class ModelEventRecordFactoryUnsupportedCommandException(className: String) :
    MedatarunTechnicalException("ModelEventRecordFactory cannot extract model id from command [$className].")


class ModelRepoCmdEventInvalidOriginJsonException(originScope: String, missingField: String) :
    MedatarunTechnicalException("Invalid $originScope origin JSON. Missing field [$missingField].")

class ModelRepoCmdEventUnknownOriginTypeException(originScope: String, originType: String) :
    MedatarunTechnicalException("Unknown $originScope origin type [$originType] in model event JSON.")

class ModelStorageDbStoreModelAggregatePKEntityNotFound(entityId: EntityId) :
    MedatarunTechnicalException("Could not store model aggregate, entity id ${entityId.asString()} specified in primary keys not found.")

class ModelStorageDbStoreModelAggregatePKAttributeNotFound(attributeId: AttributeId) :
    MedatarunTechnicalException("Could not store model aggregate, attribute id ${attributeId.asString()} specified in primary keys not found.")

class ModelStorageDbStoreModelAggregateBKEntityNotFound(entityId: EntityId) :
    MedatarunTechnicalException("Could not store model aggregate, entity id ${entityId.asString()} specified in business keys not found.")

class ModelStorageDbStoreModelAggregateBKAttributeNotFound(attributeId: AttributeId) :
    MedatarunTechnicalException("Could not store model aggregate, attribute id ${attributeId.asString()} specified in business keys not found.")
