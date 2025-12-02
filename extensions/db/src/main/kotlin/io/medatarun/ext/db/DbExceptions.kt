package io.medatarun.ext.db

import io.medatarun.model.model.MedatarunException

class DbTableWithoutColumnsException(tableName: String) : Exception("Table $tableName has no columns.")
class DbDriverManagerUnknownDatabaseException(name: String) : MedatarunException("Unsupported database $name")