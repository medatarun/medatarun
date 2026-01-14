package io.medatarun.auth.internal.users

import io.medatarun.auth.domain.user.PasswordClear
import io.medatarun.auth.domain.user.PasswordHash
import io.medatarun.auth.domain.user.Username
import java.security.SecureRandom
import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class UserPasswordEncrypter(
    private val passwordEncryptionIterations: Int = DEFAULT_ITERATIONS
) {

    fun hashPassword(password: PasswordClear): PasswordHash {
        return PasswordHash(hashPassword(password.value))
    }
    fun hashPassword(password: String): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)

        val spec = PBEKeySpec(
            password.toCharArray(),
            salt,
            passwordEncryptionIterations,
            KEY_LENGTH
        )

        val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = skf.generateSecret(spec).encoded

        return listOf(
            passwordEncryptionIterations.toString(),
            Base64.getEncoder().encodeToString(salt),
            Base64.getEncoder().encodeToString(hash)
        ).joinToString(":")
    }

    fun verifyPassword(stored: PasswordHash, candidate: PasswordClear): Boolean {
        return verifyPassword(stored.value, candidate.value)
    }
    fun verifyPassword(stored: String, candidate: String): Boolean {

        val parts = stored.split(":")
        require(parts.size == 3)

        val iterations = parts[0].toInt()
        val salt = Base64.getDecoder().decode(parts[1])
        val expectedHash = Base64.getDecoder().decode(parts[2])

        val spec = PBEKeySpec(
            candidate.toCharArray(),
            salt,
            iterations,
            expectedHash.size * 8
        )

        val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val computedHash = skf.generateSecret(spec).encoded

        return constantTimeEquals(expectedHash, computedHash)
    }
    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].toInt() xor b[i].toInt())
        }
        return result == 0
    }

    enum class PasswordClass {
        LOWER, UPPER, DIGIT, SYMBOL
    }

    sealed interface PasswordCheck {
        val ok: Boolean
        class OK(): PasswordCheck {
            override val ok: Boolean = true
        }
        data class Fail(val reason: PasswordPolicyFailReason): PasswordCheck {
            override val ok: Boolean = false
        }
    }

    enum class PasswordPolicyFailReason(val label: String) {
        TOO_SHORT("password must be at least 14 characters"),
        WHITESPACES_ONLY("password cannot be only whitespace"),
        EQUALS_USERNAME("password cannot be equal to username"),
        MISSING_CHAR_CATEGORY("password must contain at least 3 of: lowercase, uppercase, digit, symbol")
    }

    fun checkPasswordPolicy(password: PasswordClear, username: Username): PasswordCheck {
        return checkPasswordPolicy(password.value, username)
    }
    fun checkPasswordPolicy(password: String, username: Username): PasswordCheck {
        if (password.length < 14) {
            return PasswordCheck.Fail(PasswordPolicyFailReason.TOO_SHORT )
        }

        if (password.all { it.isWhitespace() }) {
            return PasswordCheck.Fail( PasswordPolicyFailReason.WHITESPACES_ONLY)
        }

        if (password.equals(username.value, ignoreCase = true)) {
            return PasswordCheck.Fail( PasswordPolicyFailReason.EQUALS_USERNAME)
        }

        val classes = mutableSetOf<PasswordClass>()

        for (c in password) {
            when {
                c.isLowerCase() -> classes += PasswordClass.LOWER
                c.isUpperCase() -> classes += PasswordClass.UPPER
                c.isDigit() -> classes += PasswordClass.DIGIT
                !c.isLetterOrDigit() -> classes += PasswordClass.SYMBOL
            }
        }

        if (classes.size < 3) {
            return PasswordCheck.Fail(

                PasswordPolicyFailReason.MISSING_CHAR_CATEGORY

            )
        }

        return PasswordCheck.OK()
    }

    companion object {
        const val DEFAULT_ITERATIONS = 310_000
        const val DEFAULT_ITERATIONS_FOR_TESTS = 10_000
        private const val KEY_LENGTH = 256  // bits
     }
}