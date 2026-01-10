package io.medatarun.auth

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import io.medatarun.auth.internal.JwtSigninKeyRegistryImpl
import io.medatarun.auth.ports.exposed.JwtSigninKeyRegistry
import org.junit.jupiter.api.Test
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JwtSigninKeyRegistryTest {

    @Test
    fun `should register embedded keys`() {
        val fs = Jimfs.newFileSystem(Configuration.unix())
        val medatarunHomePath = fs.getPath("/opt/medatarun")
        val secretsPath = medatarunHomePath.resolve(JwtSigninKeyRegistry.Companion.DEFAULT_KEYSTORE_PATH_NAME)
        val service = JwtSigninKeyRegistryImpl(secretsPath)
        val keys = service.loadOrCreateKeys()

        // Files should be created
        val privPath = secretsPath.resolve("private.pem")
        val pubPath = secretsPath.resolve("public.pem")
        val kidPath = secretsPath.resolve("kid.txt")

        assertTrue(Files.exists(privPath), "private.pem should exist")
        assertTrue(Files.exists(pubPath), "public.pem should exist")
        assertTrue(Files.exists(kidPath), "kid.txt should exist")

        // PEM headers should be correct
        val privContent = Files.readString(privPath)
        val pubContent = Files.readString(pubPath)
        assertTrue(privContent.contains("-----BEGIN PRIVATE KEY-----"))
        assertTrue(privContent.contains("-----END PRIVATE KEY-----"))
        assertTrue(pubContent.contains("-----BEGIN PUBLIC KEY-----"))
        assertTrue(pubContent.contains("-----END PUBLIC KEY-----"))

        // Keys should be non-null and RSA
        assertNotNull(keys.privateKey)
        assertNotNull(keys.publicKey)
        assertEquals("RSA", keys.privateKey.algorithm)
        assertEquals("RSA", keys.publicKey.algorithm)
        assertTrue(keys.kid.isNotBlank(), "kid should not be blank")

        // Loading again should return the same material (idempotency)
        val secondService = JwtSigninKeyRegistryImpl(secretsPath)
        val keys2 = secondService.loadOrCreateKeys()

        assertEquals(keys.kid, keys2.kid, "kid should remain the same after reload")
        assertTrue(keys.privateKey.encoded.contentEquals(keys2.privateKey.encoded), "private key should be the same")
        assertTrue(keys.publicKey.encoded.contentEquals(keys2.publicKey.encoded), "public key should be the same")
    }
}