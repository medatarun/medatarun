import io.medatarun.lang.trimToNull
import org.junit.jupiter.api.Assertions.assertNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TrimToNullTest {
    @Test
    fun `null is null`() {
        val str: String? = null
        assertNull(str.trimToNull())
    }
    @Test
    fun `empty is null`() {
        val str: String? = ""
        assertNull(str.trimToNull())
    }
    @Test
    fun `blank is null`() {
        val str: String = "   "
        assertNull(str.trimToNull())
    }
    @Test
    fun `trimmed is not null`() {
        val str: String = "  a "
        assertNotNull(str.trimToNull())
        assertEquals("a", str.trimToNull())
    }
}