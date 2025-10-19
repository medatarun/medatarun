package io.medatarun.runtime

import io.medatarun.model.model.ModelCmd
import io.medatarun.model.model.ModelQueries

interface AppRuntime {
    val modelCmd: ModelCmd
    val modelQueries: ModelQueries
}