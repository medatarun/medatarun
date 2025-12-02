package io.medatarun.ext.db

import io.medatarun.kernel.MedatarunExtension
import io.medatarun.kernel.MedatarunExtensionCtx
import io.medatarun.model.ModelImporter
import org.slf4j.LoggerFactory

class DbExtension: MedatarunExtension {

    override val id = "db"
    override fun init(ctx: MedatarunExtensionCtx) {
        val path = ctx.resolveApplicationHomePath("jdbc-drivers")
        logger.debug("jdbc-drivers path: $path")
        val dbDriverManager = DbDriverManager(path)
        ctx.register(ModelImporter::class, DbModelImporter(dbDriverManager))
    }
    companion object {
        private val logger = LoggerFactory.getLogger(DbExtension::class.java)
    }
}