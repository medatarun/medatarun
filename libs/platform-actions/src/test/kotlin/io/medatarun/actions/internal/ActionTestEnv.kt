package io.medatarun.actions.internal

import com.google.common.jimfs.Jimfs
import io.medatarun.actions.ActionsExtension
import io.medatarun.actions.adapters.ActionPlatform
import io.medatarun.actions.ports.needs.ActionPrincipalCtx
import io.medatarun.actions.ports.needs.ActionRequestCtx
import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.platform.kernel.MedatarunConfig
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.PlatformBuilder
import io.medatarun.platform.kernel.getService
import io.medatarun.security.AppPrincipal
import io.medatarun.security.SecurityExtension
import io.medatarun.types.TypeSystemExtension

class ActionTestEnv(extensions: List<MedatarunExtension>) {
    val runtime = PlatformBuilder(
        config = MedatarunConfig.createTempConfig(Jimfs.newFileSystem(), emptyMap()),
        extensions = listOf(
            TypeSystemExtension(),
            SecurityExtension(),
            ActionsExtension(),
        ).plus(extensions)
    ).buildAndStart()

    val actionCtx = TestActionCtx()

    val actionPlatform = runtime.services.getService<ActionPlatform>()

    class TestActionCtx : ActionRequestCtx {

        override val principal: ActionPrincipalCtx = TestActionPrincipalCtx(null)
        override val source: String = "test"
    }

    private class TestActionPrincipalCtx(private val providedPrincipal: AppPrincipal?) : ActionPrincipalCtx {
        override val principal: AppPrincipal?
            get() = providedPrincipal

        override fun ensureIsAdmin() {
            if (providedPrincipal == null || !providedPrincipal.isAdmin) {
                throw TestPrincipalNotAdminException()
            }
        }

        override fun ensureSignedIn(): AppPrincipal {
            val principal = providedPrincipal ?: throw TestPrincipalMissingException()
            return principal
        }
    }

    private class TestPrincipalNotAdminException : MedatarunException("Principal is not admin")
    private class TestPrincipalMissingException : MedatarunException("Principal is missing")
}
