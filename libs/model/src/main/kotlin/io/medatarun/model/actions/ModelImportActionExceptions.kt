package io.medatarun.model.actions

import io.medatarun.lang.exceptions.MedatarunException

class ModelImportActionNotFoundException(location: String) : MedatarunException("Could not find importer for $location")