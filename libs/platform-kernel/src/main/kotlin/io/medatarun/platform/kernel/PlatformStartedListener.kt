package io.medatarun.platform.kernel

interface PlatformStartedListener {
    fun onPlatformStarted(ctx: PlatformStartedCtx)
}
