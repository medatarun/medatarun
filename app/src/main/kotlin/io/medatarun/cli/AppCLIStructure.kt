package io.medatarun.app.io.medatarun.cli

import io.medatarun.app.io.medatarun.runtime.AppRuntime
import io.medatarun.app.io.medatarun.runtime.getLogger


class ModelCLIResource(runtime: AppRuntime) {
    @Suppress("unused")
    fun create(id: String, name: String) {
        logger.cli("Création du modèle $id ($name)")
    }

    companion object {
        private val logger = getLogger(ModelCLIResource::class)
    }
}

class ModelConfigCLIResource(runtime: AppRuntime) {
    @Suppress("unused")
    fun show() {
        logger.cli("Affichage de la config du modèle")
    }
    companion object {
        private val logger = getLogger(ModelConfigCLIResource::class)
    }
}

class ModelDataCLIResource(runtime: AppRuntime) {
    @Suppress("unused")
    fun import(file: String) {
        logger.cli("Import du fichier $file")
    }
    companion object {
        private val logger = getLogger(ModelDataCLIResource::class)
    }
}

class AppCLIResources(private val runtime: AppRuntime) {
    @Suppress("unused")
    val model = ModelCLIResource(runtime)
    @Suppress("unused")
    val config = ModelConfigCLIResource(runtime)
    @Suppress("unused")
    val data = ModelDataCLIResource(runtime)
}
