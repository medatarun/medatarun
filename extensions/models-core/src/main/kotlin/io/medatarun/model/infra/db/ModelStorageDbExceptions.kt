package io.medatarun.model.infra.db

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.EntityId
import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.RelationshipId
import io.medatarun.model.domain.TypeId
import io.medatarun.type.commons.id.Id


class ModelStorageDbInvalidIdentifierAttributeException(entityId: String) :
    MedatarunException("Entity $entityId has no identifier attribute in sqlite storage")

class ModelStorageDbMissingCurrentHeadModelSnapshotException(modelId: ModelId) :
    MedatarunException("Could not find CURRENT_HEAD model snapshot for model [${modelId.asString()}]")

class ModelStorageDbMissingTypeSnapshotException(typeId: TypeId) :
    MedatarunException("Could not find CURRENT_HEAD type snapshot for type [${typeId.asString()}]")

class ModelStorageDbMissingEntitySnapshotException(entityId: EntityId) :
    MedatarunException("Could not find CURRENT_HEAD entity snapshot for entity [${entityId.asString()}]")

class ModelStorageDbMissingAttributeSnapshotException(attributeId: AttributeId) :
    MedatarunException("Could not find CURRENT_HEAD attribute snapshot for attribute [${attributeId.asString()}]")

class ModelStorageDbMissingRelationshipSnapshotException(relationshipId: RelationshipId) :
    MedatarunException("Could not find CURRENT_HEAD relationship snapshot for relationship [${relationshipId.asString()}]")


class ModelStorageDbSearchUnknownItemTypeException(itemType: String) :
    MedatarunException("Unknown denormalized search item type [$itemType]")

class ModelStorageDbSearchMissingProjectionReferenceException(columnName: String) :
    MedatarunException("Search projection is missing required reference column [$columnName]")

class ModelStorageDbSearchMissingSourceRowException(rowType: String, rowId: Id<*>) :
    MedatarunException("Search projection update could not find source [$rowType] row [${rowId.asString()}]")

class ModelEventRecordFactoryUnsupportedCommandException(className: String) :
    MedatarunException("ModelEventRecordFactory cannot extract model id from command [$className].")


class ModelRepoCmdEventMissingContractAnnotationException(className: String) :
    MedatarunException("ModelRepoCmd class [$className] is missing @ModelEventContract.")

class ModelRepoCmdEventContractOnNonDataClassException(className: String) :
    MedatarunException("ModelRepoCmd class [$className] declares @ModelEventContract but is not a data class.")

class ModelRepoCmdEventDuplicateContractException(eventType: String, eventVersion: Int) :
    MedatarunException("Duplicate model event contract [$eventType@$eventVersion] in ModelRepoCmd event registry.")

class ModelRepoCmdEventCommandNotRegisteredException(className: String) :
    MedatarunException("ModelRepoCmd class [$className] is not registered in the model event registry.")

class ModelRepoCmdEventUnknownContractException(eventType: String, eventVersion: Int) :
    MedatarunException("Unknown model event contract [$eventType@$eventVersion].")

class ModelRepoCmdEventPayloadEncodeException(eventType: String, eventVersion: Int, cause: Throwable) :
    MedatarunException("Could not encode model event payload for [$eventType@$eventVersion]. Cause: ${cause.message}")

class ModelRepoCmdEventPayloadDecodeException(eventType: String, eventVersion: Int, cause: Throwable) :
    MedatarunException("Could not decode model event payload for [$eventType@$eventVersion]. Cause: ${cause.message}")

class ModelRepoCmdEventInvalidOriginJsonException(originScope: String, missingField: String) :
    MedatarunException("Invalid $originScope origin JSON. Missing field [$missingField].")

class ModelRepoCmdEventUnknownOriginTypeException(originScope: String, originType: String) :
    MedatarunException("Unknown $originScope origin type [$originType] in model event JSON.")
