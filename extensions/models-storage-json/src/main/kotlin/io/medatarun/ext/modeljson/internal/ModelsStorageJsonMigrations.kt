package io.medatarun.ext.modeljson.internal

import io.medatarun.ext.modeljson.migrations.Migration_1_1
import io.medatarun.platform.kernel.PlatformStartedCtx
import io.medatarun.platform.kernel.PlatformStartedListener
import kotlinx.serialization.json.Json

internal class ModelsStorageJsonMigrations(private val files: ModelsStorageJsonFiles, val prettyPrint: Boolean) :
    PlatformStartedListener {

    private val json = Json { prettyPrint = this@ModelsStorageJsonMigrations.prettyPrint }


    override fun onPlatformStarted(ctx: PlatformStartedCtx) {

        // First migration, if model has no id, load and save model to have ids everywhere

        Migration_1_1(files, json).start()
    }


}