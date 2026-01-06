package io.medatarun.auth.bootstrap

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import io.medatarun.auth.bootstrap.AuthEmbeddedKeyRegistry.Companion.defaultKeystorePath
import io.medatarun.auth.bootstrap.internal.AuthEmbeddedKeyRegistryImpl
import org.junit.jupiter.api.Test


class AuthEmbeddedKeyRegistryTest {

    @Test
    fun `should register embedded keys`() {
        val fs = Jimfs.newFileSystem(Configuration.unix())
        val medatarunHomePath = fs.getPath("/opt/medatarun")
        val secretsPath = medatarunHomePath.resolve(defaultKeystorePath)
        val service = AuthEmbeddedKeyRegistryImpl(secretsPath)
        val keys = service.loadOrCreateKeys()

    }
}