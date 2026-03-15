package io.medatarun.model.domain

import io.medatarun.model.domain.ModelVersion.Companion.DESCRIPTION


/**
 * See [DESCRIPTION] for infos
 */
data class ModelVersion(val value: String) : Comparable<ModelVersion> {
    private val parsed: ParsedModelVersion = ParsedModelVersion.parse(value)
    val major: Int = parsed.major
    val minor: Int = parsed.minor
    val patch: Int = parsed.patch
    val preRelease: String? = parsed.preRelease

    override fun compareTo(other: ModelVersion): Int {
        val majorComparison = major.compareTo(other.major)
        if (majorComparison != 0) return majorComparison

        val minorComparison = minor.compareTo(other.minor)
        if (minorComparison != 0) return minorComparison

        val patchComparison = patch.compareTo(other.patch)
        if (patchComparison != 0) return patchComparison

        val currentPreRelease: List<String> = parsed.preReleaseIdentifiers
        val otherPreRelease: List<String> = other.parsed.preReleaseIdentifiers

        if (currentPreRelease.isEmpty() && otherPreRelease.isEmpty()) return 0
        if (currentPreRelease.isEmpty()) return 1
        if (otherPreRelease.isEmpty()) return -1

        var index = 0
        while (index < currentPreRelease.size && index < otherPreRelease.size) {
            val identifierComparison = comparePreReleaseIdentifier(
                currentPreRelease[index],
                otherPreRelease[index]
            )
            if (identifierComparison != 0) return identifierComparison
            index += 1
        }

        return currentPreRelease.size.compareTo(otherPreRelease.size)
    }

    private fun comparePreReleaseIdentifier(left: String, right: String): Int {
        val leftNumeric = left.all { it.isDigit() }
        val rightNumeric = right.all { it.isDigit() }

        if (leftNumeric && rightNumeric) {
            return left.toInt().compareTo(right.toInt())
        }

        if (leftNumeric) return -1
        if (rightNumeric) return 1

        return left.compareTo(right)
    }

    private data class ParsedModelVersion(
        val major: Int,
        val minor: Int,
        val patch: Int,
        val preRelease: String?,
        val preReleaseIdentifiers: List<String>
    ) {
        companion object {
            fun parse(value: String): ParsedModelVersion {
                if (value.isBlank()) throw ModelVersionEmptyException()

                val match = MODEL_VERSION_REGEX.matchEntire(value) ?: throw ModelVersionInvalidFormatException()

                val rawMajor = match.groups[1]?.value ?: throw ModelVersionInvalidFormatException()
                val rawMinor = match.groups[2]?.value ?: throw ModelVersionInvalidFormatException()
                val rawPatch = match.groups[3]?.value ?: throw ModelVersionInvalidFormatException()

                if (hasLeadingZero(rawMajor) || hasLeadingZero(rawMinor) || hasLeadingZero(rawPatch)) {
                    throw ModelVersionCoreLeadingZeroException()
                }

                val preRelease = match.groups[4]?.value
                val preReleaseIdentifiers = preRelease?.split(".").orEmpty()
                validatePreReleaseIdentifiers(preReleaseIdentifiers)

                return ParsedModelVersion(
                    major = rawMajor.toIntOrNull() ?: throw ModelVersionInvalidFormatException(),
                    minor = rawMinor.toIntOrNull() ?: throw ModelVersionInvalidFormatException(),
                    patch = rawPatch.toIntOrNull() ?: throw ModelVersionInvalidFormatException(),
                    preRelease = preRelease,
                    preReleaseIdentifiers = preReleaseIdentifiers
                )
            }

            private fun validatePreReleaseIdentifiers(preReleaseIdentifiers: List<String>) {
                preReleaseIdentifiers.forEach { identifier ->
                    if (identifier.all { it.isDigit() } && hasLeadingZero(identifier)) {
                        throw ModelVersionPreReleaseLeadingZeroException()
                    }
                }
            }

            private fun hasLeadingZero(identifier: String): Boolean {
                return identifier.length > 1 && identifier.startsWith("0")
            }
        }
    }

    companion object {
        private val MODEL_VERSION_REGEX = Regex(
            "^([0-9]+)\\.([0-9]+)\\.([0-9]+)" +
                    "(?:-([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?\$"
        )

        const val DESCRIPTION = """
ModelVersion follows Semantic Versioning (MAJOR.MINOR.PATCH).

Each part is a number, for example 1.2.3. The version must not be empty.

An optional pre-release can be added after -, using dot-separated identifiers, for example 1.2.3-alpha or 1.2.3-alpha.1.
Build metadata after + is not accepted.

Numeric identifiers (major, minor, patch, and numeric pre-release parts) must not contain leading zeros.
Pre-release identifiers may only contain letters, digits, and hyphens.

This format allows versions to be compared and ordered consistently over time.     
"""
    }
}
