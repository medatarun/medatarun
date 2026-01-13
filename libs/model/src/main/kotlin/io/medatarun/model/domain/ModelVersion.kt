package io.medatarun.model.domain

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
    }
}