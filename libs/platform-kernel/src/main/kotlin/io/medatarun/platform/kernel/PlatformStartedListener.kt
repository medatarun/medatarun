package io.medatarun.platform.kernel

interface PlatformStartedListener: ServiceContributionPoint {
    fun onPlatformStarted(ctx: PlatformStartedCtx)
}
