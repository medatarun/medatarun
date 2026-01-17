package io.medatarun.ext.frictionlessdata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Data Resource
 *
 * TODO Data Resource
 */
@Serializable
data class DataResource(
    /**
     * Profile
     *
     * The profile of this descriptor.
     */
    @SerialName($$"$schema")
    val dollarSchema: String? = null,
    /**
     * Name
     *
     * An identifier string.
     *
     * This is ideally a url-usable and human-readable name. Name `SHOULD` be invariant, meaning it `SHOULD NOT` change when its parent descriptor is updated.
     *
     */
    val name :String?=null,
    /**
     * Path
     *
     * A reference to the data for this resource, as either a path as a string, or an array of paths as strings. of valid URIs.
     *
     * Pattern : ^((?=[^./~])(?!file:)((?!\\/\\.\\.\\/)(?!\\\\)(?!:\\/\\/).)*|(http|ftp)s?:\\/\\/.*)$
     *
     * The dereferenced value of each referenced data source in `path` `MUST` be commensurate with a native, dereferenced representation of the data the resource describes. For example, in a *Tabular* Data Resource, this means that the dereferenced value of `path` `MUST` be an array.
     *
     * examples:
     * - "file.csv"
     * - ["http://example.com/file.csv", "file.csv]
     */
    val path: StringOrStringArray,
    /**
     *
     * Data
     *
     * Inline data for this resource.
     */
    val data: JsonElement? = null,

    val type: String = "table",

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
     * Sources
     *
     * The raw sources for this resource.
     */
    val sources: List<DataPackageSource> = emptyList(),
    /**
     * Licenses
     *
     * The license(s) under which the resource is published.
     *
     * This property is not legally binding and does not guarantee that the package is licensed under the terms defined herein.
     */
    val licences: List<DataPackageLicence> = emptyList(),
    /**
     * Format
     *
     * The file format of this resource.
     *
     * `csv`, `xls`, `json` are examples of common formats.
     */
    val format: String? = null,
    /**
     * Media Type
     *
     * The media type of this resource. Can be any valid media type listed with [IANA](https://www.iana.org/assignments/media-types/media-types.xhtml).
     */
    val mediatype: String? = null,
    /**
     *
     * Encoding
     *
     * The file encoding of this resource.
     */
    val encoding: String?=null,

    /**
     * Bytes
     *
     * The size of this resource in bytes.
     */
    val bytes: Integer? = null,
    /**
     * Hash
     *
     * The MD5 hash of this resource. Indicate other hashing algorithms with the {algorithm}:{hash} format.
     */
    val hash: String? = null,
    /**
     * Table Dialect
     *
     * The Table dialect descriptor.
     */
    val dialect: TableDialect? = null,
    /**
     * Table Schema
     *
     * A Table Schema for this resource, compliant with the [Table Schema](
     */
    val schema: StringOrTableSchema? = null,



)