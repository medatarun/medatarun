package io.medatarun.platform.kernel

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PlatformRuntime(
    private val extensionPlatform: ExtensionPlatform,
    private val services: MedatarunServiceRegistry
) {
    fun start() {
        val startListeners = extensionPlatform.extensionRegistry.findContributionsFlat(PlatformStartedListener::class)
        val ctx: PlatformStartedCtx = object : PlatformStartedCtx {
            override val services: MedatarunServiceRegistry =
                this@PlatformRuntime.services
            override val extensionRegistry: ExtensionRegistry =
                extensionPlatform.extensionRegistry
        }
        for (s in startListeners) {
            logger.info("Starting contribution: ${s.javaClass.simpleName}")
            s.onPlatformStarted(ctx)
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(PlatformRuntime::class.java)
    }
}