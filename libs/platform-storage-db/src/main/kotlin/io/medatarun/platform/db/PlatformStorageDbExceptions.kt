package io.medatarun.platform.db

import io.medatarun.lang.exceptions.MedatarunTechnicalException

class DbMigrationRunnerUnknownVersionException(extension: String, version: Int) :
    MedatarunTechnicalException("Could not migrate database for extension $extension to version $version")

