package io.medatarun.platform.kernel.internal

import io.medatarun.platform.kernel.*
import kotlinx.serialization.json.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.io.path.absolute
import kotlin.reflect.KClass

class ExtensionRegistryImpl(
    private val extensions: List<MedatarunExtension>,
    private val config: MedatarunConfig,
    private val services: MedatarunServiceRegistry
) : ExtensionRegistry {

    private val contributionPoints: MutableMap<ExtensionId, ContributionPoint<*>> = mutableMapOf()
    private val contributionPointsByClass: MutableMap<KClass<*>, ContributionPoint<*>> = mutableMapOf()
    private val contributions: MutableMap<ContributionPointId, List<ContributionImpl<*>>> = mutableMapOf()

    private var initialized: Boolean = false

    fun init() {
        val counts = extensions.groupBy { it.id }.mapValues { it.value.size }.filter { it.value > 1 }
        if (counts.isNotEmpty()) {
            throw ExtensionRegistryDuplicateIdException(counts.keys)
        }

        extensions.forEach { extension ->
            logger.info("Registering contributions for {}", extension.id)
            extension.initContributions(
                MedatarunExtensionCtxImpl(
                    extension = extension,
                    registrar = ExtensionRegistrar(),
                    cfg = MedatarunExtensionCtxConfigImpl(extension, config),
                    services = services
                )
            )
        }
        initialized = true
    }

    inner class ExtensionRegistrar {

        fun internalRegisterContributionPoint(e: MedatarunExtension, c: ContributionPoint<*>) {
            if (!c.id.startsWith(e.id + ".")) throw ContributionPointIdMismatch(e.id, c.id)
            if (contributionPoints.containsKey(c.id)) throw ContributionPointDuplicateId(e.id, c.id)
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
            // Add to the map considering the key may have no value yet (append or create + append)
            contributions.compute(c.toContributinoPointId) { _, v -> (v ?: emptyList()) + c }
        }

    }

    private fun ensureInitialized() {
        if (!initialized) throw ContributionAccessWhileNotInitializedException()
    }

    override fun <CONTRIB : Any> findContributionsFlat(api: KClass<CONTRIB>): List<CONTRIB> {
        ensureInitialized()
        val c = contributionPointsByClass[api] ?: throw ContributionPointNotFoundByApiException(api)
        return (contributions[c.id] ?: emptyList()).map {
            @Suppress("UNCHECKED_CAST")
            it.instance as CONTRIB
        }
    }

    override fun inspectHumanReadable(): String {
        ensureInitialized()
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
        ensureInitialized()
        return buildJsonObject {
            put("homeDirectory", config.applicationHomeDir.absolute().toString())
            put("applicationDataDirectory", config.projectDir.absolute().toString())
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
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ExtensionRegistryImpl::class.java)
    }
}
