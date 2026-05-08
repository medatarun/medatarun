package io.medatarun.model.actions

import io.medatarun.lang.exceptions.MedatarunUserException

class ModelImportActionNotFoundException(location: String) : MedatarunUserException("Could not find importer for $location")