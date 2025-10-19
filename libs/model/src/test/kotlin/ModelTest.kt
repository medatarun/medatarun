import io.medatarun.model.model.ModelRuntimeDefault
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ModelTest {
    @Test
    fun testMessage() {
        val runtime = ModelRuntimeDefault()
        assertEquals(runtime.models.size, 0)
    }
}