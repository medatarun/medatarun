package io.medatarun.model.domain

import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class ModelVersionTest {
    @Test
    fun `valid model versions are accepted`() {
        val valid = listOf(
            "0.0.1",
            "1.2.3",
            "10.20.30",
            "1.2.3-alpha",
            "1.2.3-alpha.1",
            "1.2.3-0.3.7",
            "1.2.3-x.7.z-92",
            "1.2.3+build.1",
            "1.2.3+001",
            "1.2.3-alpha+build.1",
        )

        valid.forEach { version ->
            assertDoesNotThrow(version) { ModelVersion(version).validate() }
        }
    }

    @Test
    fun `empty model version is rejected`() {
        assertThrows<ModelVersionEmptyException> {
            ModelVersion("").validate()
        }
        assertThrows<ModelVersionEmptyException> {
            ModelVersion("   ").validate()
        }
    }

    @Test
    fun `invalid format model versions are rejected`() {
        val invalid = listOf(
            "1.2",
            "1.2.3.4",
            "v1.2.3",
            "1.2.3-",
            "1.2.3+build..1",
            "1.2.3+build&1",
            "1.2.3-!alpha"
        )

        invalid.forEach { version ->
            assertThrows<ModelVersionInvalidFormatException> {
                ModelVersion(version).validate()
            }
        }
    }

    @Test
    fun `core numeric identifiers with leading zeros are rejected`() {
        val invalid = listOf(
            "01.2.3",
            "1.02.3",
            "1.2.03"
        )

        invalid.forEach { version ->
            assertThrows<ModelVersionCoreLeadingZeroException> {
                ModelVersion(version).validate()
            }
        }
    }

    @Test
    fun `pre-release numeric identifiers with leading zeros are rejected`() {
        val invalid = listOf(
            "1.2.3-01",
            "1.2.3-1.02",
            "1.2.3-001.alpha"
        )

        invalid.forEach { version ->
            assertThrows<ModelVersionPreReleaseLeadingZeroException> {
                ModelVersion(version).validate()
            }
        }
    }
}
