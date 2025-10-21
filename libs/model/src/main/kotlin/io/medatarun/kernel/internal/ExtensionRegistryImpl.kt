package io.medatarun.kernel.internal

import io.medatarun.kernel.*
import java.nio.file.Path
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
            extension.init(object : MedatarunExtensionCtx {
                override fun getConfigProperty(key: String): String? {
                    return config.getProperty(key)
                }

                override fun getConfigProperty(key: String, defaultValue: String): String {
                    return config.getProperty(key, defaultValue)
                }

                override fun resolveProjectPath(relativePath: String): Path {
                    return config.projectDir.resolve(relativePath).toAbsolutePath()
                }

                override fun <CONTRIB : Any> registerContributionPoint(id: ContributionPointId, api: KClass<CONTRIB>) {
                    internalRegisterContributionPoint(extension, ContributionPoint(id, api, extension.id))
                }

                override fun <INTERFACE : Any, IMPL : INTERFACE> register(api: KClass<INTERFACE>, instance: IMPL) {
                    internalRegisterContribution(extension.id, api, instance)
                }

            })
        }
    }

    private fun internalRegisterContributionPoint(e: MedatarunExtension, c: ContributionPoint<*>) {
        if (!c.id.startsWith(e.id + ".")) throw ContributionPointIdMismatch(e.id, c.id)
        if (contributionPoints.containsKey(e.id)) throw ContributionPointDuplicateId(e.id, c.id)
        if (contributionPointsByClass.containsKey(c.api)) {
            val other = contributionPointsByClass[c.api]?.id ?: "unknown"
            throw ContributionPointDuplicateInterface(e.id, c.id, c.api, other)
        }
        contributionPoints[c.id] = c
        contributionPointsByClass[c.api] = c
    }

    private fun <INTERFACE : Any, IMPL : INTERFACE> internalRegisterContribution(
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
}
