package io.medatarun.model.infra.db

import io.medatarun.lang.exceptions.MedatarunException

class ModelStorageSearchSQLiteUnknownItemTypeException(itemType: String) :
    MedatarunException("Unknown denormalized search item type [$itemType]")

class ModelStorageSearchSQLiteMissingProjectionReferenceException(columnName: String) :
    MedatarunException("Search projection is missing required reference column [$columnName]")

class ModelStorageSearchSQLiteMissingSourceRowException(rowType: String, rowId: String) :
    MedatarunException("Search projection update could not find source [$rowType] row [$rowId]")
