package io.medatarun.ext.modeljson

import io.medatarun.model.model.MedatarunException

class ModelJsonRepositoryNotFoundException(key: String, path: String) :
    MedatarunException("Configuration error: $key specifies path '$path' that does not point to a valid existing directory.")