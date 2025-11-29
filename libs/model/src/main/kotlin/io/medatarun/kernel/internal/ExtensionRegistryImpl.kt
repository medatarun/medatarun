package io.medatarun.kernel.internal

import io.medatarun.kernel.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlin.io.path.absolute
import kotlin.reflect.KClass

class ExtensionRegistryImpl(
    private val extensions: List<MedatarunExtension>,
    private val config: MedatarunConfig
) : ExtensionRegistry {

    private val contributionPoints: MutableMap<ExtensionId, ContributionPoint<*>> = mutableMapOf()
    private val contributionPointsByClass: MutableMap<KClass<*>, ContributionPoint<*>> = mutableMapOf()
    private val contributions: MutableMap<ContributionPointId, List<ContributionImpl<*>>> = mutableMapOf()


    fun init() {
        val counts = extensions.groupBy { it.id }.mapValues { it.value.size }.filter { it.value > 1 }
        if (counts.size > 1) {
            throw ExtensionRegistryDuplicateIdException(counts.keys)
        }

        extensions.forEach { extension ->
            extension.init(MedatarunExtensionCtxImpl(extension, config, ExtensionRegistrar()))
        }
    }

    inner class ExtensionRegistrar {

        fun internalRegisterContributionPoint(e: MedatarunExtension, c: ContributionPoint<*>) {
            if (!c.id.startsWith(e.id + ".")) throw ContributionPointIdMismatch(e.id, c.id)
            if (contributionPoints.containsKey(e.id)) throw ContributionPointDuplicateId(e.id, c.id)
            if (contributionPointsByClass.containsKey(c.api)) {
                val other = contributionPointsByClass[c.api]?.id ?: "unknown"
                throw ContributionPointDuplicateInterface(e.id, c.id, c.api, other)
            }
            contributionPoints[c.id] = c
            contributionPointsByClass[c.api] = c
        }

        fun <INTERFACE : Any, IMPL : INTERFACE> internalRegisterContribution(
            fromExtensionId: ExtensionId,
            api: KClass<INTERFACE>,
            instance: IMPL
        ) {
            val contributionPoint = contributionPointsByClass[api]
                ?: throw ContributionRegistrationContributionPointNotFoundException(fromExtensionId, api)
            val c = ContributionImpl(
                fromExtensionId = fromExtensionId,
                toExtensionId = contributionPoint.extensionId,
                toContributinoPointId = contributionPoint.id,
                instance = instance
            )
            // Add to map considering the key may have no value yet (append or create + append)
            contributions.compute(c.toContributinoPointId) { _, v -> (v ?: emptyList()) + c }
        }

    }

    override fun <CONTRIB : Any> findContributionsFlat(api: KClass<CONTRIB>): List<CONTRIB> {
        val c = contributionPointsByClass[api] ?: throw ContributionPointNotFoundByApiException(api)
        return (contributions[c.id] ?: emptyList()).map {
            @Suppress("UNCHECKED_CAST")
            it.instance as CONTRIB
        }
    }

    override fun inspectHumanReadable(): String {
        val report = StringBuilder()
        for (extension in extensions) {
            report.appendLine("📦 ${extension.id}")
            for (contributionPoint in contributionPoints.filter { it.value.extensionId == extension.id }.values) {
                report.appendLine("   🖇️  ${contributionPoint.id} ${contributionPoint.api.simpleName}")
                for (contrib in (contributions[contributionPoint.id] ?: emptyList())) {
                    report.appendLine("      - " + contrib.fromExtensionId + " - " + contrib.instance::class.simpleName)
                }
            }
            for (contributionList in contributions.values) {
                for (contrib in contributionList) {
                    if (contrib.fromExtensionId == extension.id) {
                        report.appendLine("   🔗 " + contrib.instance::class.simpleName + " -> " + contrib.toContributinoPointId)
                    }
                }
            }
        }
        return report.toString()
    }

    override fun inspectJson(): JsonObject {
        return buildJsonObject {
            put("projectdir", config.projectDir.absolute().toString())
            putJsonArray("extensions") {
                extensions.forEach { ext ->
                    addJsonObject {
                        put("id", ext.id)
                        putJsonArray("contributionPoints") {
                            for (contributionPoint in contributionPoints.filter { it.value.extensionId == ext.id }.values) {
                                addJsonObject {
                                    put("id", contributionPoint.id)
                                    put("interface", contributionPoint.api.simpleName)
                                }
                            }
                        }
                        putJsonArray("contributions") {
                            contributions.values.forEach { contributionList ->
                                for (contrib in contributionList.filter { it.fromExtensionId == ext.id }) {
                                    addJsonObject {
                                        put("toExtension", contrib.toExtensionId)
                                        put("toContributionPoint", contrib.toContributinoPointId)
                                        put("implementation", contrib.instance::class.simpleName)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
