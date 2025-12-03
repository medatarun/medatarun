package io.medatarun.resources.actions

import io.medatarun.model.model.MedatarunException

class ModelImportActionNotFoundException(location: String) : MedatarunException("Could not find importer for $location")