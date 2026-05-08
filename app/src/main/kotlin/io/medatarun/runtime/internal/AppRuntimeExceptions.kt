package io.medatarun.runtime.internal

import io.medatarun.lang.exceptions.MedatarunTechnicalException
import io.medatarun.runtime.internal.AppRuntimeConfigFactory.Companion.USER_DIR_PROPERTY


class RootDirNotFoundException :
    MedatarunTechnicalException("Could not guess the current user directory. Configure MEDATARUN_APPLICATION_DATA if needed.")

class ProjectDirApplicationDataDoesNotExistException(path: String, envVar: String) :
    MedatarunTechnicalException("The project directory '$path' specified via environment variable $envVar does not exist.")

class ProjectDirNotAdirectoryException(path: String) :
    MedatarunTechnicalException("Project directory found $path is not a directory.")

class MedatarunHomeDoesNotExistException(path: String) :
    MedatarunTechnicalException("MEDATARUN_HOME directory '$path' does not exist.")

class MedatarunHomeNotADirectoryException(path: String) :
    MedatarunTechnicalException("MEDATARUN_HOME directory '$path' is not a directory.")

class MedatarunUserDirUndefinedException :
    MedatarunTechnicalException("Property $USER_DIR_PROPERTY is not defined.")