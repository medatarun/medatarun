package io.medatarun.auth.bootstrap

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import io.medatarun.auth.bootstrap.AuthEmbeddedKeyRegistry.Companion.DEFAULT_KEYSTORE_PATH_NAME
import io.medatarun.auth.bootstrap.internal.AuthEmbeddedKeyRegistryImpl
import org.junit.jupiter.api.Test
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class AuthEmbeddedKeyRegistryTest {

    @Test
    fun `should register embedded keys`() {
        val fs = Jimfs.newFileSystem(Configuration.unix())
        val medatarunHomePath = fs.getPath("/opt/medatarun")
        val secretsPath = medatarunHomePath.resolve(DEFAULT_KEYSTORE_PATH_NAME)
        val service = AuthEmbeddedKeyRegistryImpl(secretsPath)
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
        val secondService = AuthEmbeddedKeyRegistryImpl(secretsPath)
        val keys2 = secondService.loadOrCreateKeys()

        assertEquals(keys.kid, keys2.kid, "kid should remain the same after reload")
        assertTrue(keys.privateKey.encoded.contentEquals(keys2.privateKey.encoded), "private key should be the same")
        assertTrue(keys.publicKey.encoded.contentEquals(keys2.publicKey.encoded), "public key should be the same")
    }
}