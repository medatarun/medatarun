package io.medatarun.cli

import io.medatarun.resources.ConfigResource
import io.medatarun.resources.DataResource
import io.medatarun.resources.ModelResource
import io.medatarun.runtime.AppRuntime


class AppCLIResources(private val runtime: AppRuntime) {
    @Suppress("unused")
    val model = ModelResource(runtime)

    @Suppress("unused")
    val config = ConfigResource(runtime)

    @Suppress("unused")
    val data = DataResource(runtime)
}
