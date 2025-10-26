package io.medatarun.resources

import io.medatarun.runtime.AppRuntime


class AppResources(private val runtime: AppRuntime) {
    @Suppress("unused")
    val model = ModelResource(runtime)

    @Suppress("unused")
    val config = ConfigResource(runtime)

    @Suppress("unused")
    val data = DataResource(runtime)
}
