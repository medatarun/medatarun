package io.medatarun.auth.embedded

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import io.medatarun.auth.embedded.internal.AuthEmbeddedBootstrapSecretImpl
import org.junit.jupiter.api.Test
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthEmbeddedBootstrapSecretTest {
    @Test
    fun `should generate secret and make it usable only once`() {
        val fs = Jimfs.newFileSystem(Configuration.unix())
        val medatarunHomePath = fs.getPath("/opt/medatarun")
        val secretsPath = medatarunHomePath.resolve(AuthEmbeddedBootstrapSecret.DEFAULT_BOOTSTRAP_SECRET_PATH_NAME)
        val service = AuthEmbeddedBootstrapSecretImpl(secretsPath)
        var bootstrapSecretLog: String? = null
        val state1 = service.loadOrCreateBootstrapSecret { secret -> bootstrapSecretLog = secret }

        // secret created and logged once
        assertNotNull(bootstrapSecretLog, "secret should be logged on first creation")
        assertEquals(bootstrapSecretLog, state1.secret, "returned secret should match logged value")
        assertTrue(state1.secret.isNotBlank(), "secret must not be blank")
        assertEquals(false, state1.consumed, "secret should not be consumed initially")

        // files state
        val secretPath = secretsPath.resolve("bootstrap.secret")
        val consumedFlagPath = secretsPath.resolve("bootstrap.consumed.flag")
        assertTrue(Files.exists(secretPath), "bootstrap.secret should exist")
        assertTrue(!Files.exists(consumedFlagPath), "consumed flag should not exist yet")

        // Second load: same secret, no log callback
        var bootstrapSecretLog2: String? = null
        val state2 = service.loadOrCreateBootstrapSecret { secret -> bootstrapSecretLog2 = secret }
        assertNull(bootstrapSecretLog2, "callback must not be called if secret already exists")
        assertEquals(state1.secret, state2.secret, "secret must be stable across reloads")
        assertEquals(false, state2.consumed, "still not consumed before marking")

        // Mark consumed and reload
        service.markBootstrapConsumed()
        assertTrue(Files.exists(consumedFlagPath), "consumed flag file should exist after marking")

        val state3 = service.loadOrCreateBootstrapSecret { /* should not be called */ }
        assertEquals(state2.secret, state3.secret, "secret must remain the same after consumption")
        assertEquals(true, state3.consumed, "consumed flag should be true after marking")
    }
}