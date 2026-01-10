package io.medatarun.auth

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import io.medatarun.auth.domain.BootstrapSecretPrefilledToShortException
import io.medatarun.auth.internal.BootstrapSecretLifecycleImpl
import io.medatarun.auth.ports.exposed.BootstrapSecretLifecycle
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.nio.file.Files
import kotlin.test.*

class BootstrapSecretLifecycleTest {

    @Test
    fun `should use secret and make it usable only once`() {
        testSuite(null)
    }

    @Test
    fun `should generate secret and make it usable only once`() {
        testSuite("THIS IS MY SECRET 0123456789")
    }

    @Test
    fun `prefilled secret shall respect min size`() {
        assertThrows<BootstrapSecretPrefilledToShortException> {
            testSuite("secret bidon")
        }
    }

    fun testSuite(cfgBootstrapSecret: String?) {

        val fs = Jimfs.newFileSystem(Configuration.unix())
        val medatarunHomePath = fs.getPath("/opt/medatarun")
        val secretsPath = medatarunHomePath.resolve(BootstrapSecretLifecycle.DEFAULT_BOOTSTRAP_SECRET_PATH_NAME)
        val service = BootstrapSecretLifecycleImpl(secretsPath, cfgBootstrapSecret)
        var bootstrapSecretLog: String? = null

        val secretPath = secretsPath.resolve("bootstrap.secret")
        val consumedFlagPath = secretsPath.resolve("bootstrap.consumed.flag")

        // Before start
        // ------------

        // Before 1st start startup there is nothing
        val beforeStartState = service.load()
        assertNull(beforeStartState)

        // files state
        assertTrue(!Files.exists(secretPath), "bootstrap.secret should not exist yet")
        assertTrue(!Files.exists(consumedFlagPath), "consumed flag should not exist yet")

        // 1st call to loadOrCreate
        // ------------------------

        val state1 = service.loadOrCreateBootstrapSecret { secret -> bootstrapSecretLog = secret }
        if (cfgBootstrapSecret != null) {
            // If we gave a secret environment variable, it shall be used
            assertEquals(cfgBootstrapSecret, bootstrapSecretLog, "Secret environment variable shall be used")
        }

        // secret created and logged once
        assertNotNull(bootstrapSecretLog, "secret should be logged on first creation")
        assertEquals(bootstrapSecretLog, state1.secret, "returned secret should match logged value")
        assertTrue(state1.secret.isNotBlank(), "secret must not be blank")
        assertEquals(false, state1.consumed, "secret should not be consumed initially")

        // files state
        assertTrue(Files.exists(secretPath), "bootstrap.secret should exist")
        assertTrue(!Files.exists(consumedFlagPath), "consumed flag should not exist yet")

        // Just load after first creation, should not be consumed
        val loadAfterFirstCreate = service.load()
        assertNotNull(loadAfterFirstCreate, "load shall return the secret")
        assertEquals(bootstrapSecretLog, loadAfterFirstCreate.secret, "returned secret should match logged value")
        assertFalse(loadAfterFirstCreate.consumed, "consumed flag should be false after creation")

        // 2nd call to loadOrCreate (still not consumed)
        // ---------------------------------------------

        // Second start, try to run loadOrCreate: same secret, no log callback
        var bootstrapSecretLog2: String? = null
        val state2 = service.loadOrCreateBootstrapSecret { secret -> bootstrapSecretLog2 = secret }
        assertNotNull(bootstrapSecretLog2, "callback should be called until secret consumed")
        assertEquals(bootstrapSecretLog2, state1.secret, "returned secret should match logged value")
        assertEquals(state1.secret, state2.secret, "secret must be stable across reloads")
        assertEquals(false, state2.consumed, "still not consumed before marking")

        // Mark consumed and reload
        // ------------------------

        service.markBootstrapConsumed()
        assertTrue(Files.exists(consumedFlagPath), "consumed flag file should exist after marking")

        // Be sure you can still load it with marked consumed
        // Test before the next "loadOrCreate" to test if there is no cached value in memory
        val load3 = service.load()
        assertNotNull(load3, "secret should still be loadable and exist")
        assertEquals(bootstrapSecretLog, load3.secret, "secret should still be the same")
        assertTrue(load3.consumed, "secret should be marked as consumed the same")

        // Reload
        var bootstrapSecretLog3: String? = null
        val state3 = service.loadOrCreateBootstrapSecret { secret -> bootstrapSecretLog3 = secret }
        assertNull(bootstrapSecretLog3, "callback should not be called after secret consumed")
        assertEquals(state2.secret, state3.secret, "secret must remain the same after consumption")
        assertEquals(true, state3.consumed, "consumed flag should be true after marking")

        // Test we still load can after reload
        val load4 = service.load()
        assertNotNull(load4, "secret should still be loadable and exist")
        assertEquals(bootstrapSecretLog, load4.secret, "secret should still be the same")
        assertTrue(load4.consumed, "secret should be marked as consumed the same")
    }
}