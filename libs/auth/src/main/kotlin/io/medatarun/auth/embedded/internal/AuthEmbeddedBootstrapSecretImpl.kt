package io.medatarun.auth.embedded.internal

import io.medatarun.auth.embedded.AuthEmbeddedBootstrapSecret
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.security.SecureRandom
import java.util.*

/**
 * Creates a bootstrap secret, generated once only
 */
class AuthEmbeddedBootstrapSecretImpl(
    private val bootstrapDir: Path
): AuthEmbeddedBootstrapSecret {
    val secretPath = bootstrapDir.resolve("bootstrap.secret")
    val consumedPath = bootstrapDir.resolve("bootstrap.consumed.flag")


    override fun loadOrCreateBootstrapSecret(logOnce: (String) -> Unit): AuthEmbeddedBootstrapState {
        Files.createDirectories(bootstrapDir)

        val consumed = Files.exists(consumedPath)
        if (Files.exists(secretPath)) {
            val secret = Files.readString(secretPath).trim()
            if (!consumed) {
                logOnce(secret)
            }
            return AuthEmbeddedBootstrapState(secret, consumed)
        }

        val rnd = ByteArray(48)
        SecureRandom().nextBytes(rnd)
        val secret = Base64.getUrlEncoder().withoutPadding().encodeToString(rnd)

        Files.writeString(secretPath, secret, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        logOnce(secret)
        return AuthEmbeddedBootstrapState(secret, consumed)
    }

    override fun markBootstrapConsumed() {
        Files.writeString(consumedPath, "consumed", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    }

    override fun load(): AuthEmbeddedBootstrapState? {
        if (!Files.exists(secretPath)) return null
        val secret = Files.readString(secretPath)
        val consumed = Files.exists(consumedPath)
        return AuthEmbeddedBootstrapState(secret, consumed)

    }
}