package io.medatarun.actions.providers.model

import io.medatarun.model.domain.MedatarunException

class ModelImportActionNotFoundException(location: String) : MedatarunException("Could not find importer for $location")