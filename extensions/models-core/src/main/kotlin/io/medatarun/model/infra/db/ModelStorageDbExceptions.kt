package io.medatarun.model.infra.db

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.EntityId
import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.RelationshipId
import io.medatarun.model.domain.TypeId
import io.medatarun.model.infra.db.snapshots.SnapshotSelector
import io.medatarun.type.commons.id.Id
import kotlin.reflect.KClass


class ModelStorageDbInvalidIdentifierAttributeException(entityId: String) :
    MedatarunException("Entity $entityId has no identifier attribute in sqlite storage")

class ModelStorageDbMissingCurrentHeadModelSnapshotException(modelId: ModelId) :
    MedatarunException("Could not find CURRENT_HEAD model snapshot for model [${modelId.asString()}]")

class ModelStorageDbMissingReleaseEventException(modelId: ModelId, version: String) :
    MedatarunException("Could not find model_release event for model [${modelId.asString()}] and version [$version]")

class ModelStorageDbNoReleaseException(modelId: ModelId) :
    MedatarunException("Could not find any model_release event for model [${modelId.asString()}]")

class ModelStorageDbInvalidReleaseEventException(modelId: ModelId, eventId: String) :
    MedatarunException("model_release event [$eventId] for model [${modelId.asString()}] has no model_version.")

class ModelStorageDbMissingTypeSnapshotException(typeId: TypeId) :
    MedatarunException("Could not find CURRENT_HEAD type snapshot for type [${typeId.asString()}]")

class ModelStorageDbMissingEntitySnapshotException(entityId: EntityId) :
    MedatarunException("Could not find CURRENT_HEAD entity snapshot for entity [${entityId.asString()}]")

class ModelStorageDbMissingAttributeSnapshotException(attributeId: AttributeId) :
    MedatarunException("Could not find CURRENT_HEAD attribute snapshot for attribute [${attributeId.asString()}]")

class ModelStorageDbMissingRelationshipSnapshotException(relationshipId: RelationshipId) :
    MedatarunException("Could not find CURRENT_HEAD relationship snapshot for relationship [${relationshipId.asString()}]")

class ModelStorageDbMissingRelationshipRoleSnapshotException(relationshipRoleId: String) :
    MedatarunException("Could not find CURRENT_HEAD relationship role snapshot for relationship role [$relationshipRoleId]")

class ModelStorageDbUnsupportedProjectedDeleteException(eventType: String) :
    MedatarunException("CURRENT_HEAD projector does not handle delete event [$eventType].")


class ModelStorageDbSearchUnknownItemTypeException(itemType: String) :
    MedatarunException("Unknown denormalized search item type [$itemType]")

class ModelStorageDbSearchMissingProjectionReferenceException(columnName: String) :
    MedatarunException("Search projection is missing required reference column [$columnName]")

class ModelEventRecordFactoryUnsupportedCommandException(className: String) :
    MedatarunException("ModelEventRecordFactory cannot extract model id from command [$className].")




class ModelRepoCmdEventInvalidOriginJsonException(originScope: String, missingField: String) :
    MedatarunException("Invalid $originScope origin JSON. Missing field [$missingField].")

class ModelRepoCmdEventUnknownOriginTypeException(originScope: String, originType: String) :
    MedatarunException("Unknown $originScope origin type [$originType] in model event JSON.")
