package io.medatarun.model.domain

import io.medatarun.model.domain.ModelVersion.Companion.DESCRIPTION


/**
 * See [DESCRIPTION] for infos
 */
@JvmInline
value class ModelVersion(val value: String) {
    val match
        get() = MODEL_VERSION_REGEX.matchEntire(value) ?: throw ModelVersionInvalidFormatException()
    val major
        get() = match.groups[1]?.value ?: throw ModelVersionInvalidFormatException()
    val minor
        get() = match.groups[2]?.value ?: throw ModelVersionInvalidFormatException()
    val patch
        get() = match.groups[3]?.value ?: throw ModelVersionInvalidFormatException()
    val preRelease
        get() = match.groups[4]?.value

    fun validate(): ModelVersion {
        if (value.isBlank()) throw ModelVersionEmptyException()

        if (hasLeadingZero(major) || hasLeadingZero(minor) || hasLeadingZero(patch)) {
            throw ModelVersionCoreLeadingZeroException()
        }

        val preReleaseStable = preRelease
        if (preReleaseStable != null) {
            validatePreReleaseIdentifiers(preReleaseStable)
        }

        return this
    }


    private fun validatePreReleaseIdentifiers(preRelease: String) {
        // SemVer: pre-release identifiers are dot-separated; numeric identifiers cannot have leading zeros.
        val identifiers = preRelease.split(".")
        identifiers.forEach { identifier ->
            if (identifier.isNotEmpty() && identifier.all { it.isDigit() } && hasLeadingZero(identifier)) {
                throw ModelVersionPreReleaseLeadingZeroException()
            }
        }
    }

    private fun hasLeadingZero(identifier: String): Boolean {
        return identifier.length > 1 && identifier.startsWith("0")
    }

    companion object {
        private val MODEL_VERSION_REGEX = Regex(
            "^([0-9]+)\\.([0-9]+)\\.([0-9]+)" +
                    "(?:-([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?" +
                    "(?:\\+([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?\$"
        )

        const val DESCRIPTION = """
ModelVersion follows Semantic Versioning (MAJOR.MINOR.PATCH).

Each part is a number, for example 1.2.3. The version must not be empty.

An optional pre-release can be added after -, using dot-separated identifiers, for example 1.2.3-alpha or 1.2.3-alpha.1.
Optional build metadata can be added after +, for example 1.2.3+build.1. Pre-release and build metadata can be combined.

Numeric identifiers (major, minor, patch, and numeric pre-release parts) must not contain leading zeros.
Pre-release and build identifiers may only contain letters, digits, and hyphens.

This format allows versions to be compared and ordered consistently over time.     
"""
    }
}