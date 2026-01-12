package io.medatarun.auth.domain

import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class FullnameTest {

    @Test
    fun `valid fullnames are accepted`() {
        val valid = listOf(
            "Jean Dupont",
            "Anne-Marie O'Neill",
            "Jean P. Martin",
            "Nguyễn Văn A",
            "محمد علي",
            "Jean  Dupont",
            "Marie, Jeanne-Arc",
            "José Álvarez",
            "Łukasz Żółć",
            "François L'Écuyer",
            "Hassan Ali",
            "王伟",
            "山田 太郎",
            "김민수",
            "David ben Gurion",
            "Jean-Luc Picard",
            "Jean  Dupont",
        )

        valid.forEach { name ->
            assertDoesNotThrow(name) { Fullname(name).validate() }
        }
    }

    @Test
    fun `empty fullname is rejected`() {
        assertThrows<UserFullnameEmptyException> {
            Fullname("   ").validate()
        }
    }

    @Test
    fun `too long fullname is rejected`() {
        assertThrows<UserFullnameTooLongException> {
            Fullname("a".repeat(201)).validate()
        }
    }

    @Test
    fun `control characters are rejected`() {
        assertThrows<UserFullnameInvalidFormatException> {
            Fullname("Jean\nDupont").validate()
        }
    }

    @Test
    fun `unicode separators are rejected`() {
        assertThrows<UserFullnameInvalidFormatException> {
            Fullname("Jean\u00A0Dupont").validate()
        }
    }

    @Test
    fun `symbols and emojis are rejected`() {
        val invalid = listOf(
            "Jean😀Dupont",
            "@dm!n",
            "{Jean}",
            "Jean|Dupont"
        )

        invalid.forEach {
            assertThrows<UserFullnameInvalidFormatException>("testing $it") {
                Fullname(it).validate()
            }
        }
    }
}
