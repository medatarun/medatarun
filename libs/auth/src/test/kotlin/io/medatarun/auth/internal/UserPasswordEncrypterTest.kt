package io.medatarun.auth.internal

import io.medatarun.auth.domain.user.Username
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.*

class UserPasswordEncrypterTest {

    private val userPasswordEncrypter = UserPasswordEncrypter()

    @Test
    fun `hashPassword should generate a string with three parts separated by colons`() {
        val password = "mySecretPassword"
        val hash = userPasswordEncrypter.hashPassword(password)

        val parts = hash.split(":")
        assertEquals(3, parts.size, "Hash should have 3 parts")
        assertEquals("310000", parts[0], "First part should be the number of iterations")
    }

    @Test
    fun `verifyPassword should return true for correct password`() {
        val password = "securePassword123"
        val hash = userPasswordEncrypter.hashPassword(password)

        assertTrue(userPasswordEncrypter.verifyPassword(hash, password), "Password verification should succeed")
    }

    @Test
    fun `verifyPassword should return false for incorrect password`() {
        val password = "securePassword123"
        val wrongPassword = "wrongPassword"
        val hash = userPasswordEncrypter.hashPassword(password)

        assertFalse(
            userPasswordEncrypter.verifyPassword(hash, wrongPassword),
            "Password verification should fail for wrong password"
        )
    }

    @Test
    fun `verifyPassword should return false if hash is for different password even if they share some prefix`() {
        val password = "password"
        val candidate = "password123"
        val hash = userPasswordEncrypter.hashPassword(password)

        assertFalse(userPasswordEncrypter.verifyPassword(hash, candidate))
    }

    @Test
    fun `verifyPassword should throw exception for malformed stored hash`() {
        val malformedHash = "310000:not-a-base64"
        assertThrows<IllegalArgumentException> {
            userPasswordEncrypter.verifyPassword(malformedHash, "anyPassword")
        }
    }

    @Test
    fun `hashPassword should produce different hashes for the same password due to random salt`() {
        val password = "samePassword"
        val hash1 = userPasswordEncrypter.hashPassword(password)
        val hash2 = userPasswordEncrypter.hashPassword(password)

        assertNotEquals(hash1, hash2, "Hashes should be different due to salt")
    }

    @Test
    fun `checkPasswordPolicy should fail if password is too short`() {
        val result = userPasswordEncrypter.checkPasswordPolicy("Short1!", Username("user"))
        assertFalse(result.ok)
        assertIs<UserPasswordEncrypter.PasswordCheck.Fail>(result)
        assertEquals(UserPasswordEncrypter.PasswordPolicyFailReason.TOO_SHORT, result.reason)
    }

    @Test
    fun `checkPasswordPolicy should fail if password is only whitespace`() {
        val result = userPasswordEncrypter.checkPasswordPolicy("              ", Username("user"))
        assertFalse(result.ok)
        assertIs<UserPasswordEncrypter.PasswordCheck.Fail>(result)
        assertEquals(UserPasswordEncrypter.PasswordPolicyFailReason.WHITESPACES_ONLY, result.reason)
    }

    @Test
    fun `checkPasswordPolicy should fail if password is equal to username`() {
        val result = userPasswordEncrypter.checkPasswordPolicy("VerySecurePassword1!", Username("VerySecurePassword1!"))
        assertFalse(result.ok)
        assertIs<UserPasswordEncrypter.PasswordCheck.Fail>(result)
        assertEquals(UserPasswordEncrypter.PasswordPolicyFailReason.EQUALS_USERNAME, result.reason)
    }

    @Test
    fun `checkPasswordPolicy should fail if password has less than 3 character classes`() {
        // Only lowercase and uppercase (2 classes)
        val result1 = userPasswordEncrypter.checkPasswordPolicy("OnlyLettersLongEnough", Username("user"))
        assertFalse(result1.ok)
        assertIs<UserPasswordEncrypter.PasswordCheck.Fail>(result1)
        assertEquals(UserPasswordEncrypter.PasswordPolicyFailReason.MISSING_CHAR_CATEGORY, result1.reason)

        // Only lowercase and digits (2 classes)
        val result2 = userPasswordEncrypter.checkPasswordPolicy("onlylettersand12345", Username("user"))
        assertFalse(result2.ok)
        assertIs<UserPasswordEncrypter.PasswordCheck.Fail>(result2)
        assertEquals(UserPasswordEncrypter.PasswordPolicyFailReason.MISSING_CHAR_CATEGORY, result2.reason)
    }

    @Test
    fun `checkPasswordPolicy should succeed for valid passwords`() {
        // Lower, Upper, Digit
        assertTrue(userPasswordEncrypter.checkPasswordPolicy("ValidPassword123", Username("user")).ok)

        // Lower, Upper, Symbol
        assertTrue(userPasswordEncrypter.checkPasswordPolicy("ValidPassword!!!", Username("user")).ok)

        // Lower, Digit, Symbol
        assertTrue(userPasswordEncrypter.checkPasswordPolicy("validpassword123!!!", Username("user")).ok)

        // Upper, Digit, Symbol
        assertTrue(userPasswordEncrypter.checkPasswordPolicy("VALIDPASSWORD123!!!", Username("user")).ok)
    }


}