package io.medatarun.app.io.medatarun.runtime.internal

import io.medatarun.app.io.medatarun.runtime.AppRuntime

class AppRuntimeBuilder {
    fun build(): AppRuntime {
        return object: AppRuntime {}
    }
}