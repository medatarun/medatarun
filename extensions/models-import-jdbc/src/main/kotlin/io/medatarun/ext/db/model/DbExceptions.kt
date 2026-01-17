package io.medatarun.ext.db.model

import io.medatarun.lang.exceptions.MedatarunException

class DbTableWithoutColumnsException(tableName: String) : Exception("Table $tableName has no columns.")
class DbDriverManagerUnknownDatabaseException(name: String) : MedatarunException("Unsupported database $name")
class DbConnectionNotFoundException(name: String) : MedatarunException("Unknown connection named [$name]")