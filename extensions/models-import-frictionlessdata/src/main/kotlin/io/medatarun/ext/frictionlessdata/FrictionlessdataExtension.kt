package io.medatarun.ext.frictionlessdata

import io.medatarun.model.domain.ModelId
import io.medatarun.model.ports.needs.ModelImporter
import io.medatarun.platform.kernel.ExtensionId
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx
import io.medatarun.platform.kernel.MedatarunServiceCtx
import io.medatarun.tags.core.domain.*
import io.medatarun.tags.core.ports.needs.TagScopeManager

class FrictionlessdataExtension : MedatarunExtension {
    override val id: ExtensionId = "models-import-frictionlessdata"
    override fun init(ctx: MedatarunExtensionCtx) {

    }

    override fun initServices(ctx: MedatarunServiceCtx) {
        val tagCmds = ctx.getService(TagCmds::class)
        val tagQueries = ctx.getService(TagQueries::class)
        val tagImporter = FrictionlessTagImporterWithCmds(tagCmds, tagQueries)
        val importer = FrictionlessdataModelImporter(tagImporter)
        ctx.register(ModelImporter::class, importer)
    }
}

private class FrictionlessTagImporterWithCmds(
    private val tagCmds: TagCmds,
    private val tagQueries: TagQueries
) : FrictionlessTagImporter {

    override fun importModelScopeTags(modelId: ModelId, keywords: List<String>): List<TagId> {
        val scopeRef = TagScopeRef.Local(TagScopeType("model"), TagScopeId(modelId.value))
        return keywords
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .map { keyword ->
                val tagRef = TagRef.ByKey(scopeRef = scopeRef, groupKey = null, key = TagKey(keyword))
                val existing = tagQueries.findTagByRefOptional(tagRef)
                if (existing != null) {
                    return@map existing.id
                }
                tagCmds.dispatch(
                    TagCmd.TagFreeCreate(
                        scopeRef = scopeRef,
                        key = TagKey(keyword),
                        name = keyword,
                        description = null
                    )
                )
                return@map tagQueries.findTagByRef(tagRef).id
            }
    }
}
