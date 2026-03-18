package io.medatarun.platform.kernel

import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface PlatformRuntime {
    val config: MedatarunConfig
    val services: MedatarunServiceRegistry
    val extensions: ExtensionRegistry
}

internal class PlatformRuntimeImpl(
    override val extensions: ExtensionRegistry,
    override val services: MedatarunServiceRegistry,
    override val config: MedatarunConfig
) : PlatformRuntime {

    internal fun start() {
        val startListeners = extensions.findContributionsFlat(PlatformStartedListener::class)
        val ctx: PlatformStartedCtx = object : PlatformStartedCtx {
            override val services: MedatarunServiceRegistry =
                this@PlatformRuntimeImpl.services
            override val extensionRegistry: ExtensionRegistry =
                this@PlatformRuntimeImpl.extensions
        }
        for (s in startListeners) {
            logger.info("Platform started event for contribution: ${s.javaClass}")
            s.onPlatformStarted(ctx)
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(PlatformRuntime::class.java)
    }
}