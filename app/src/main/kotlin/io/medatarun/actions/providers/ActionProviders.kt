package io.medatarun.actions.providers

import io.medatarun.actions.providers.batch.BatchActionProvider
import io.medatarun.actions.providers.config.ConfigActionProvider
import io.medatarun.actions.providers.model.ModelActionProvider

class ActionProviders() {
    @Suppress("unused")
    val model = ModelActionProvider()

    @Suppress("unused")
    val config = ConfigActionProvider()

    @Suppress("unused")
    val batch = BatchActionProvider()
}