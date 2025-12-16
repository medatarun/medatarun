package io.medatarun.model.actions

import io.medatarun.model.domain.MedatarunException

class ModelImportActionNotFoundException(location: String) : MedatarunException("Could not find importer for $location")