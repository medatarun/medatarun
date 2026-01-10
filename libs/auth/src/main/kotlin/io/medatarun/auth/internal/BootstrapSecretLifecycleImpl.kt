package io.medatarun.auth.internal

import io.medatarun.auth.ports.exposed.BootstrapSecretLifecycle
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.security.SecureRandom
import java.util.*

/**
 * Creates a bootstrap secret, generated once only
 */
class BootstrapSecretLifecycleImpl(
    private val bootstrapDir: Path
): BootstrapSecretLifecycle {
    val secretPath = bootstrapDir.resolve("bootstrap.secret")
    val consumedPath = bootstrapDir.resolve("bootstrap.consumed.flag")


    override fun loadOrCreateBootstrapSecret(logOnce: (String) -> Unit): BootstrapSecretState {
        Files.createDirectories(bootstrapDir)

        val consumed = Files.exists(consumedPath)
        if (Files.exists(secretPath)) {
            val secret = Files.readString(secretPath).trim()
            if (!consumed) {
                logOnce(secret)
            }
            return BootstrapSecretState(secret, consumed)
        }

        val rnd = ByteArray(48)
        SecureRandom().nextBytes(rnd)
        val secret = Base64.getUrlEncoder().withoutPadding().encodeToString(rnd)

        Files.writeString(secretPath, secret, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        logOnce(secret)
        return BootstrapSecretState(secret, consumed)
    }

    override fun markBootstrapConsumed() {
        Files.writeString(consumedPath, "consumed", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    }

    override fun load(): BootstrapSecretState? {
        if (!Files.exists(secretPath)) return null
        val secret = Files.readString(secretPath)
        val consumed = Files.exists(consumedPath)
        return BootstrapSecretState(secret, consumed)

    }
}