package io.medatarun.model.infra.db

import io.medatarun.lang.exceptions.MedatarunException


class ModelStorageDbInvalidIdentifierAttributeException(entityId: String) :
    MedatarunException("Entity $entityId has no identifier attribute in sqlite storage")


class ModelStorageDbSearchUnknownItemTypeException(itemType: String) :
    MedatarunException("Unknown denormalized search item type [$itemType]")

class ModelStorageDbSearchMissingProjectionReferenceException(columnName: String) :
    MedatarunException("Search projection is missing required reference column [$columnName]")

class ModelStorageDbSearchMissingSourceRowException(rowType: String, rowId: String) :
    MedatarunException("Search projection update could not find source [$rowType] row [$rowId]")
