package io.medatarun.runtime

import io.medatarun.actions.adapters.ActionPlatform
import io.medatarun.platform.kernel.getService
import io.medatarun.runtime.AppRuntimeTest.AppRuntimeOSBridgeTest
import io.medatarun.runtime.internal.AppRuntimeBuilder
import io.medatarun.runtime.internal.AppRuntimeConfigFactory
import kotlin.test.Test
import kotlin.test.assertFalse

class ActionsFitnessTest {
    @Test
    fun `that app can be launched in server mode`() {
        val os = AppRuntimeOSBridgeTest()
        val config = AppRuntimeConfigFactory(cli = false, os).create()
        val runtime = AppRuntimeBuilder(config).build()
        val actionPlatform = runtime.services.getService<ActionPlatform>()
        actionPlatform.registry.findAllActions().forEach { a ->
            a.descriptor.parameters.forEach { p->
                assertFalse(p.title.isNullOrEmpty(), "Action [${a.descriptor.key}] parameter [${p.key}] has no title")
                assertFalse(p.description.isNullOrEmpty(), "Action [${a.descriptor.key}] parameter [${p.key}] has no description")
            }
        }

    }

}