package io.medatarun.runtime.internal

import io.medatarun.lang.exceptions.MedatarunException


class RootDirNotFoundException:
    MedatarunException("Could not guess the current user directory. Configure MEDATARUN_APPLICATION_DATA if needed.")

class ProjectDirApplicationDataDoesNotExistException(path: String, envVar: String) :
    MedatarunException("The project directory '$path' specified via environment variable $envVar does not exist.")

class ProjectDirNotAdirectoryException(path: String) :
        MedatarunException("Project directory found $path is not a directory.")

class MedatarunHomeDoesNotExistException(path: String) : MedatarunException("MEDATARUN_HOME directory '$path' does not exist.")
class MedatarunHomeNotADirectoryException(path: String) : MedatarunException("MEDATARUN_HOME directory '$path' is not a directory.")