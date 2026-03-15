package io.medatarun.model.domain

import io.medatarun.model.domain.ModelVersion.Companion.DESCRIPTION
import kotlin.text.MatchResult


/**
 * See [DESCRIPTION] for infos
 */
@JvmInline
value class ModelVersion(val value: String) : Comparable<ModelVersion> {
    private val match: MatchResult
        get() = MODEL_VERSION_REGEX.matchEntire(value) ?: throw ModelVersionInvalidFormatException()
    val major: Int
        get() = parseCoreIdentifier(1)
    val minor: Int
        get() = parseCoreIdentifier(2)
    val patch: Int
        get() = parseCoreIdentifier(3)
    val preRelease: String?
        get() = match.groups[4]?.value

    init {
        ensureValid()
    }

    override fun compareTo(other: ModelVersion): Int {
        val majorComparison = major.compareTo(other.major)
        if (majorComparison != 0) return majorComparison

        val minorComparison = minor.compareTo(other.minor)
        if (minorComparison != 0) return minorComparison

        val patchComparison = patch.compareTo(other.patch)
        if (patchComparison != 0) return patchComparison

        val currentPreRelease = parsePreReleaseIdentifiers()
        val otherPreRelease = other.parsePreReleaseIdentifiers()

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

    /**
     * Completes structural format errors detected during creation with the dedicated domain exceptions.
     */
    private fun ensureValid() {
        if (value.isBlank()) throw ModelVersionEmptyException()

        if (hasLeadingZero(rawCoreIdentifier(1)) || hasLeadingZero(rawCoreIdentifier(2)) || hasLeadingZero(rawCoreIdentifier(3))) {
            throw ModelVersionCoreLeadingZeroException()
        }

        val preReleaseStable = preRelease
        if (preReleaseStable != null) {
            validatePreReleaseIdentifiers(preReleaseStable)
        }
    }

    private fun rawCoreIdentifier(groupIndex: Int): String {
        return match.groups[groupIndex]?.value ?: throw ModelVersionInvalidFormatException()
    }

    private fun parseCoreIdentifier(groupIndex: Int): Int {
        val identifier = rawCoreIdentifier(groupIndex)
        return identifier.toIntOrNull() ?: throw ModelVersionInvalidFormatException()
    }

    private fun parsePreReleaseIdentifiers(): List<String> {
        val preReleaseStable = preRelease ?: return emptyList()
        return preReleaseStable.split(".")
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
