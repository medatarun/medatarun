package io.medatarun.ext.db.domain

import io.medatarun.lang.exceptions.MedatarunUserException

class DbTableWithoutColumnsException(tableName: String) : MedatarunUserException("Table $tableName has no columns.")
class DbDriverManagerUnknownDatabaseException(name: String) : MedatarunUserException("Unsupported database $name")
class DbConnectionNotFoundException(name: String) : MedatarunUserException("Unknown connection named [$name]")
class DbImportTypeNotFoundException(table:String, type:String): MedatarunUserException("Could not import table $table, type $type could not be resolved.")
class DbImportCouldNotFindEntityForRelationship(tableName: String): MedatarunUserException("Could not create relationship based on foreign key. Entity mathing table name $tableName doesn't exist.")