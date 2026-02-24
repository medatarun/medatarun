package io.medatarun.platform.kernel

import com.google.common.jimfs.Jimfs
import io.medatarun.platform.kernel.MedatarunConfig.Companion.createTempConfig
import java.nio.file.Path
import javax.security.auth.login.AppConfigurationEntry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class PlatformKernelTest {
    @Test
    fun `platform builds`() {
        PlatformBuilder(config, listOf(ExtensionRecipe(), ExtensionVehicle())).buildAndStart()
    }

    @Test
    fun `events fired and received`() {

        val p = PlatformBuilder(config, listOf(ExtensionRecipe(), ExtensionVehicle())).buildAndStart()
        val vehicle = p.services.getService<ExtensionVehicle.VehicleService>()
        val recipe = p.services.getService<ExtensionRecipe.RecipeService>()
        assertTrue(vehicle.driving.isEmpty())
        recipe.sendRecipe("burger")
        assertEquals(listOf("burger"), vehicle.driving)

    }


    class ExtensionRecipe : MedatarunExtension {
        override val id: ExtensionId = "recipe"
        override fun init(ctx: MedatarunExtensionCtx) {

        }

        override fun initServices(ctx: MedatarunServiceCtx) {
            val e = ctx.getService(EventSystem::class)
            val recipeSentEvt = e.createNotifier(RecipeSent::class)
            val recipeService = RecipeService(recipeSentEvt)
            ctx.register(RecipeService::class, recipeService)
        }

        class RecipeService(val evt: EventNotifier<RecipeSent>) {
            fun sendRecipe(name: String) {
                evt.fire(RecipeSent(name))
            }
        }

        data class RecipeSent(val name: String) : Event
    }

    class ExtensionVehicle : MedatarunExtension {
        override val id: ExtensionId = "vehicle"
        override fun initServices(ctx: MedatarunServiceCtx) {
            val e = ctx.getService(EventSystem::class)
            val s = VehicleService()
            e.registerObserver(ExtensionRecipe.RecipeSent::class, object : EventObserver<ExtensionRecipe.RecipeSent> {
                override fun onEvent(evt: ExtensionRecipe.RecipeSent) {
                    s.drive(evt.name)
                }
            })
            ctx.register(VehicleService::class, s)

        }

        override fun init(ctx: MedatarunExtensionCtx) {

        }

        class VehicleService {
            val driving = mutableListOf<String>()
            fun drive(name: String) {
                driving.add(name)
            }
        }
    }
    val config = createTempConfig(Jimfs.newFileSystem(), emptyMap())
}