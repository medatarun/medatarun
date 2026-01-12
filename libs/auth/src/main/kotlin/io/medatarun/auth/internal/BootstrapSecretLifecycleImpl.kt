package io.medatarun.auth.internal

import io.medatarun.auth.domain.BootstrapSecretPrefilledToShortException
import io.medatarun.auth.ports.exposed.BootstrapSecretLifecycle
import io.medatarun.auth.ports.exposed.BootstrapSecretLifecycle.Companion.SECRET_MIN_SIZE
import io.medatarun.auth.ports.exposed.BootstrapSecretLifecycle.Companion.SECRET_SIZE
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.security.SecureRandom
import java.util.*

/**
 * Creates a bootstrap secret, generated once only
 */
class BootstrapSecretLifecycleImpl(
    private val bootstrapDir: Path,
    private val prefilledSecret: String?,
): BootstrapSecretLifecycle {
    val secretPath = bootstrapDir.resolve("bootstrap.secret")
    val consumedPath = bootstrapDir.resolve("bootstrap.consumed.flag")

    init {
        if (prefilledSecret!=null) {
            if (prefilledSecret.length < SECRET_MIN_SIZE) {
                throw BootstrapSecretPrefilledToShortException()
            }
        }
    }


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

        val secret = if (prefilledSecret != null) {
            prefilledSecret
        } else {
            val rnd = ByteArray(SECRET_SIZE)
            SecureRandom().nextBytes(rnd)
            Base64.getUrlEncoder().withoutPadding().encodeToString(rnd)
        }


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

    companion object {

    }
}