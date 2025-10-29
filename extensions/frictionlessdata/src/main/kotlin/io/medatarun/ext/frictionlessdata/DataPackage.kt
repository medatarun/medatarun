package io.medatarun.ext.frictionlessdata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data package
 *
 * - https://datapackage.org/standard/data-package
 * - https://datapackage.org/profiles/2.0/datapackage.json
 */
@Serializable
data class DataPackage(
    /**
     * Profile
     *
     * The profile of this descriptor.
     */
    @SerialName($$"$schema")
    val schema: String? = null,
    /**
     * Name
     *
     * An identifier string.
     *
     * "This is ideally a url-usable and human-readable name. Name `SHOULD` be invariant, meaning it `SHOULD NOT`
     * change when its parent descriptor is updated.
     */
    val name: String? = null,

    /**
     * ID
     *
     * A property reserved for globally unique identifiers. Examples of identifiers that are unique include UUIDs and DOIs.
     *
     * A common usage pattern for Data Packages is as a packaging format within the bounds of a system or platform.
     * In these cases, a unique identifier for a package is desired for common data handling workflows,
     * such as updating an existing package. While at the level of the specification, global uniqueness cannot be
     * validated, consumers using the `id` property `MUST` ensure identifiers are globally unique.
     */
    val id: String? = null,
    /**
     * Title
     *
     * A human-readable title.
     */
    val title: String? = null,

    /**
     * Description
     *
     * A text description. Markdown is encouraged.
     */
    val description: String? = null,

    /**
     * Home Page
     *
     * The home on the web that is related to this data package.
     */
    val homepage: String? = null,

    /**
     * Version
     *
     * A unique version number for this descriptor.
     */
    val version: String? = null,

    /**
     * Created
     *
     * The datetime on which this descriptor was created.
     *
     * The datetime must conform to the string formats for datetime as described in [RFC3339](https://tools.ietf.org/html/rfc3339#section-5.6)
     */
    val created: String? = null,
    /**
     * Contributors
     *
     * The contributors to this descriptor.
     *
     * Use of this property does not imply that the person was the original creator of, or a contributor to, the data
     * in the descriptor, but refers to the composition of the descriptor itself.
     */
    val contributors: List<DataPackageContributor> = emptyList(),
    /**
     * Keywords
     *
     * A list of keywords that describe this package.
     */
    val keywords: List<String> = emptyList(),

    /**
     * Image
     *
     * A image to represent this package.
     *
     * Examples: http://example.com/image.jpg or relative/to/image.jpg
     */
    val image: String? = null,

    /**
     * Licenses
     *
     * The license(s) under which this package is published.
     *
     * This property is not legally binding and does not guarantee that the package is licensed under the terms defined herein.
     */
    val licenses: List<DataPackageLicence> = emptyList(),

    /**
     * Resources
     *
     * An `array` of Data Resource objects, each compliant with the [Data Resource](/data-resource/) specification.
     */
    val resources: List<DataResource> = emptyList(),

    /**
     * Sources
     *
     * The raw sources for this resource.
     */
    val sources: List<DataPackageSource> = emptyList(),
)

/**
 * A contributor to this descriptor.
 */
@Serializable
data class DataPackageContributor(
    /**
     * Title
     *
     * A human-readable title.
     */
    val title: String? = null,
    /**
     * Path
     *
     * A fully qualified URL, or a POSIX file path.
     *
     * pattern: ^((?=[^./~])(?!file:)((?!\\/\\.\\.\\/)(?!\\\\)(?!:\\/\\/).)*|(http|ftp)s?:\\/\\/.*)$
     *
     * Implementations need to negotiate the type of path provided, and dereference the data accordingly.
     */
    val path: String? = null,

    /**
     * Email
     *
     * An email address.
     */
    val email: String? = null,
    val givenName: String? = null,
    val familyName: String? = null,
    /**
     * Organization
     *
     * An organizational affiliation for this contributor.
     */
    val organization: String? = null,

    val roles: List<String> = emptyList(),
)

/**
 * A license for this descriptor.
 */
@Serializable
data class DataPackageLicence(
    /**
     * Open Definition license identifier
     *
     * MUST be an Open Definition license identifier, see http://licenses.opendefinition.org/
     */
    val name: String? = null,
    /**
     * Path
     *
     * A fully qualified URL, or a POSIX file path.
     *
     * Pattern: ^((?=[^./~])(?!file:)((?!\\/\\.\\.\\/)(?!\\\\)(?!:\\/\\/).)*|(http|ftp)s?:\\/\\/.*)$
     *
     * Implementations need to negotiate the type of path provided, and dereference the data accordingly.
     *
     */
    val path: String? = null,
    /**
     * A human-readable title.
     */
    val title: String? = null,

    )

/**
 * Source
 *
 * A source file.
 */
@Serializable
data class DataPackageSource(
    /**
     * Title
     *
     * A human-readable title.
     */
    val title: String? = null,
    /**
     * Path
     *
     * A fully qualified URL, or a POSIX file path.
     *
     * Pattern: ^((?=[^./~])(?!file:)((?!\/\.\.\/)(?!\\)(?!:\/\/).)*|(http|ftp)s?:\/\/.*)$
     */
    val path: String? = null,
    /**
     * Email
     *
     * An email address.
     */
    val email: String? = null,

    val version: String? = null
)