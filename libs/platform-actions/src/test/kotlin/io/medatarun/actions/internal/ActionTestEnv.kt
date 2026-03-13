package io.medatarun.actions.internal

import com.google.common.jimfs.Jimfs
import io.medatarun.actions.ActionsExtension
import io.medatarun.actions.adapters.ActionPlatform
import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionPrincipalCtx
import io.medatarun.actions.ports.needs.ActionRequest
import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.platform.kernel.*
import io.medatarun.security.AppPrincipal
import io.medatarun.security.SecurityExtension
import io.medatarun.types.TypeSystemExtension
import kotlin.reflect.KClass

class ActionTestEnv(extensions: List<MedatarunExtension>) {
    val runtime = PlatformBuilder(
        config = MedatarunConfig.createTempConfig(Jimfs.newFileSystem(), emptyMap()),
        extensions = listOf(
            TypeSystemExtension(),
            SecurityExtension(),
            ActionsExtension(),
        ).plus(extensions)
    ).buildAndStart()

    val actionCtx = TestActionCtx( runtime.services)

    val actionPlatform = runtime.services.getService<ActionPlatform>()

    class TestActionCtx(

        private val serviceRegistry: MedatarunServiceRegistry
    ) : ActionCtx {

        override fun dispatchAction(req: ActionRequest): Any? {
            throw TestActionCtxDispatchException()
        }

        override fun <T : Any> getService(type: KClass<T>): T {
            return serviceRegistry.getService(type)
        }

        override val principal: ActionPrincipalCtx = TestActionPrincipalCtx(null)
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

    private class TestActionCtxDispatchException : MedatarunException("dispatch not supported for tests")
    private class TestPrincipalNotAdminException : MedatarunException("Principal is not admin")
    private class TestPrincipalMissingException : MedatarunException("Principal is missing")
}