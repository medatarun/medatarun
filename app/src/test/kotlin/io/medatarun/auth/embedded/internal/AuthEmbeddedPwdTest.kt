package io.medatarun.auth.embedded.internal


import io.medatarun.auth.embedded.internal.AuthEmbeddedPwd.PasswordPolicyFailReason
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.*

class AuthEmbeddedPwdTest {

    private val authEmbeddedPwd = AuthEmbeddedPwd()

    @Test
    fun `hashPassword should generate a string with three parts separated by colons`() {
        val password = "mySecretPassword"
        val hash = authEmbeddedPwd.hashPassword(password)
        
        val parts = hash.split(":")
        assertEquals(3, parts.size, "Hash should have 3 parts")
        assertEquals("310000", parts[0], "First part should be the number of iterations")
    }

    @Test
    fun `verifyPassword should return true for correct password`() {
        val password = "securePassword123"
        val hash = authEmbeddedPwd.hashPassword(password)
        
        assertTrue(authEmbeddedPwd.verifyPassword(hash, password), "Password verification should succeed")
    }

    @Test
    fun `verifyPassword should return false for incorrect password`() {
        val password = "securePassword123"
        val wrongPassword = "wrongPassword"
        val hash = authEmbeddedPwd.hashPassword(password)
        
        assertFalse(authEmbeddedPwd.verifyPassword(hash, wrongPassword), "Password verification should fail for wrong password")
    }

    @Test
    fun `verifyPassword should return false if hash is for different password even if they share some prefix`() {
        val password = "password"
        val candidate = "password123"
        val hash = authEmbeddedPwd.hashPassword(password)
        
        assertFalse(authEmbeddedPwd.verifyPassword(hash, candidate))
    }

    @Test
    fun `verifyPassword should throw exception for malformed stored hash`() {
        val malformedHash = "310000:not-a-base64"
        assertThrows<IllegalArgumentException> {
            authEmbeddedPwd.verifyPassword(malformedHash, "anyPassword")
        }
    }

    @Test
    fun `hashPassword should produce different hashes for the same password due to random salt`() {
        val password = "samePassword"
        val hash1 = authEmbeddedPwd.hashPassword(password)
        val hash2 = authEmbeddedPwd.hashPassword(password)
        
        assertNotEquals(hash1, hash2, "Hashes should be different due to salt")
    }

    @Test
    fun `checkPasswordPolicy should fail if password is too short`() {
        val result = authEmbeddedPwd.checkPasswordPolicy("Short1!", "user")
        assertFalse(result.ok)
        assertIs<AuthEmbeddedPwd.PasswordCheck.Fail>(result)
        assertEquals(PasswordPolicyFailReason.TOO_SHORT, result.reason)
    }

    @Test
    fun `checkPasswordPolicy should fail if password is only whitespace`() {
        val result = authEmbeddedPwd.checkPasswordPolicy("              ", "user")
        assertFalse(result.ok)
        assertIs<AuthEmbeddedPwd.PasswordCheck.Fail>(result)
        assertEquals(PasswordPolicyFailReason.WHITESPACES_ONLY, result.reason)
    }

    @Test
    fun `checkPasswordPolicy should fail if password is equal to username`() {
        val result = authEmbeddedPwd.checkPasswordPolicy("VerySecurePassword1!", "VerySecurePassword1!")
        assertFalse(result.ok)
        assertIs<AuthEmbeddedPwd.PasswordCheck.Fail>(result)
        assertEquals(PasswordPolicyFailReason.EQUALS_USERNAME, result.reason)
    }

    @Test
    fun `checkPasswordPolicy should fail if password has less than 3 character classes`() {
        // Only lowercase and uppercase (2 classes)
        val result1 = authEmbeddedPwd.checkPasswordPolicy("OnlyLettersLongEnough", "user")
        assertFalse(result1.ok)
        assertIs<AuthEmbeddedPwd.PasswordCheck.Fail>(result1)
        assertEquals(PasswordPolicyFailReason.MISSING_CHAR_CATEGORY, result1.reason)

        // Only lowercase and digits (2 classes)
        val result2 = authEmbeddedPwd.checkPasswordPolicy("onlylettersand12345", "user")
        assertFalse(result2.ok)
        assertIs<AuthEmbeddedPwd.PasswordCheck.Fail>(result2)
        assertEquals(PasswordPolicyFailReason.MISSING_CHAR_CATEGORY, result2.reason)
    }

    @Test
    fun `checkPasswordPolicy should succeed for valid passwords`() {
        // Lower, Upper, Digit
        assertTrue(authEmbeddedPwd.checkPasswordPolicy("ValidPassword123", "user").ok)
        
        // Lower, Upper, Symbol
        assertTrue(authEmbeddedPwd.checkPasswordPolicy("ValidPassword!!!", "user").ok)
        
        // Lower, Digit, Symbol
        assertTrue(authEmbeddedPwd.checkPasswordPolicy("validpassword123!!!", "user").ok)
        
        // Upper, Digit, Symbol
        assertTrue(authEmbeddedPwd.checkPasswordPolicy("VALIDPASSWORD123!!!", "user").ok)
    }


}