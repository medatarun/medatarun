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
        )

        valid.forEach { version ->
            assertDoesNotThrow(version) { ModelVersion(version) }
        }
    }

    @Test
    fun `empty model version is rejected`() {
        assertThrows<ModelVersionEmptyException> {
            ModelVersion("")
        }
        assertThrows<ModelVersionEmptyException> {
            ModelVersion("   ")
        }
    }

    @Test
    fun `invalid format model versions are rejected`() {
        val invalid = listOf(
            "1.2",
            "1.2.3.4",
            "v1.2.3",
            "1.2.3-",
            "1.2.3+build.1",
            "1.2.3+001",
            "1.2.3-alpha+build.1",
            "1.2.3+build..1",
            "1.2.3+build&1",
            "1.2.3-!alpha"
        )

        invalid.forEach { version ->
            assertThrows<ModelVersionInvalidFormatException> {
                ModelVersion(version)
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
                ModelVersion(version)
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
                ModelVersion(version)
            }
        }
    }

    @Test
    fun `stable release is greater than matching pre-release`() {
        val preRelease = ModelVersion("1.2.3-rc.1")
        val stable = ModelVersion("1.2.3")

        kotlin.test.assertTrue(stable > preRelease)
    }

    @Test
    fun `pre-releases are ordered using semver precedence`() {
        val versions = listOf(
            ModelVersion("1.2.3"),
            ModelVersion("1.2.3-beta.11"),
            ModelVersion("1.2.3-beta.2"),
            ModelVersion("1.2.3-beta"),
            ModelVersion("1.2.3-alpha.beta"),
            ModelVersion("1.2.3-alpha.1"),
            ModelVersion("1.2.3-alpha"),
        )

        val sorted = versions.sorted()

        kotlin.test.assertEquals(
            listOf(
                ModelVersion("1.2.3-alpha"),
                ModelVersion("1.2.3-alpha.1"),
                ModelVersion("1.2.3-alpha.beta"),
                ModelVersion("1.2.3-beta"),
                ModelVersion("1.2.3-beta.2"),
                ModelVersion("1.2.3-beta.11"),
                ModelVersion("1.2.3"),
            ),
            sorted
        )
    }

    @Test
    fun `major minor and patch are ordered before pre-release comparison`() {
        val versions = listOf(
            ModelVersion("2.0.0-alpha"),
            ModelVersion("1.10.0"),
            ModelVersion("1.2.4"),
            ModelVersion("1.2.3"),
        )

        val sorted = versions.sorted()

        kotlin.test.assertEquals(
            listOf(
                ModelVersion("1.2.3"),
                ModelVersion("1.2.4"),
                ModelVersion("1.10.0"),
                ModelVersion("2.0.0-alpha"),
            ),
            sorted
        )
    }
}
