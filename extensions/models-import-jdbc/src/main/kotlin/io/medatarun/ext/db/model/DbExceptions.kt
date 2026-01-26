package io.medatarun.ext.db.model

import io.medatarun.lang.exceptions.MedatarunException

class DbTableWithoutColumnsException(tableName: String) : Exception("Table $tableName has no columns.")
class DbDriverManagerUnknownDatabaseException(name: String) : MedatarunException("Unsupported database $name")
class DbConnectionNotFoundException(name: String) : MedatarunException("Unknown connection named [$name]")
class DbImportCouldNotFindAttributeFromPrimaryKeyException(name:String, pk:String): MedatarunException("Could not choose an identifier attribtute for entity $name, determined primary key $pk doesn't match list of found attributes.")
class DbImportTypeNotFoundException(table:String, type:String): MedatarunException("Could not import table $table, type $type could not be resolved.")
class DbImportCouldNotFindEntityForRelationship(tableName: String): MedatarunException("Could not create relationship based on foreign key. Entity mathing table name $tableName doesn't exist.")