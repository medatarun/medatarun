package io.medatarun.platform.db

import io.medatarun.lang.exceptions.MedatarunException

class DbMigrationRunnerUnknownVersionException(extension: String, version: Int) :
    MedatarunException("Could not migrate database for extension $extension to version $version")

class DbMigrationUnknownDialectException(productName: String) :
    MedatarunException("Unsupported JDBC database product name [$productName] for database migrations")