package io.medatarun.model.domain

import io.medatarun.model.infra.ModelRepositoryInMemory
import io.medatarun.model.infra.ModelStoragesComposite
import io.medatarun.model.internal.ModelAuditor
import io.medatarun.model.internal.ModelCmdsImpl
import io.medatarun.model.internal.ModelQueriesImpl
import io.medatarun.model.internal.ModelValidationImpl
import io.medatarun.model.ports.exposed.ModelCmd
import io.medatarun.model.ports.exposed.ModelCmds
import io.medatarun.model.ports.exposed.ModelQueries
import org.junit.platform.commons.logging.LoggerFactory

class ModelTestRuntime private constructor (
    val repositories: List<ModelRepositoryInMemory>
) {
    val storages = ModelStoragesComposite(repositories, ModelValidationImpl())
    val cmd: ModelCmds = ModelCmdsImpl(storages, object : ModelAuditor {
        override fun onCmdProcessed(cmd: ModelCmd) {
            logger.info { "onCmdProcessed: $cmd" }
        }
    })
    val queries: ModelQueries = ModelQueriesImpl(storages)

    companion object {
        private val logger = LoggerFactory.getLogger(ModelTestRuntime::class.java)
        fun createRuntime(
            repositories: List<ModelRepositoryInMemory> = listOf(ModelRepositoryInMemory("repo"))
        ): ModelTestRuntime {
            return ModelTestRuntime(
                repositories = repositories
            )
        }
    }
}