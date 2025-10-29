package io.medatarun.ext.frictionlessdata

import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

@Serializable
data class TableSchema(
    @Contextual
    val fields: List<TableSchemaField>,
    /**
     * A primary key is a field name or an array of field names, whose values `MUST` uniquely identify each row in the table.
     *
     * Field name in the `primaryKey` `MUST` be unique, and `MUST` match a field name in the associated table.
     * It is acceptable to have an array with a single value, indicating that the value of a single field is the
     * primary key.
     */
    val primaryKey: StringOrStringArray? = null,

    /**
     * Table Schema Foreign Key
     */
    val foreignKeys: List<TableSchemaForeignKey> = emptyList(),

    /**
     * Missing values
     *
     * Values that when encountered in the source, should be considered as `null`, 'not present', or 'blank' values.
     *
     * Many datasets arrive with missing data values, either because a value was not collected or it never existed.
     *
     * Missing values may be indicated simply by the value being empty in other cases a special value may have been
     * used e.g. `-`, `NaN`, `0`, `-9999` etc.
     *
     * The `missingValues` property provides a way to indicate that these values should be interpreted as equivalent to null.
     *
     * `missingValues` are strings rather than being the data type of the particular field. This allows for comparison
     * prior to casting and for fields to have missing value which are not of their type, for example a `number`
     * field to have missing values indicated by `-`.
     *
     * The default value of `missingValue` for a non-string type
     * field is the empty string `''`. For string type fields there is no default for `missingValue` (for string fields
     * the empty string `''` is a valid value and need not indicate null).
     */
    val missingValues: Set<String> = emptySet()
)

@Serializable
data class TableSchemaForeignKey(
    /**
     * Fields that make up the primary key.
     */
    val fields: Set<String>,
    val reference: TableSchemaForeignKeyReference
)

@Serializable
data class TableSchemaForeignKeyReference(
    val resource: String? = "",
    val fields: StringOrStringArray
)

@Serializable
abstract class TableSchemaField {



    /**
     * Type of the field
     */
    abstract val type: String

    /**
     * Name
     *
     * A name for this field.
     */
    abstract val name: String

    /**
     * Title
     *
     * A human-readable title.
     */
    abstract val title: String?

    /**
     * Description
     *
     * A text description. Markdown is encouraged.
     */
    abstract val description: String?

    /**
     * Example
     *
     * An example value for the field.
     */
    abstract val example: String?

    /**
     * Derivative function not in the spec
     */
    abstract fun isOptional(): Boolean
}

/**
 * Table Schema Field
 */
@Serializable
data class TableSchemaFieldString(
    override val name: String,
    override val title: String? = null,
    override val description: String? = null,
    override val example: String? = null,
    override val type: String = "string",

    /**
     * Format
     *
     * The format keyword options for `string` are `default`, `email`, `uri`, `binary`, and `uuid`.
     *
     * The following `format` options are supported:
     *
     * - **default**: any valid string.
     * - **email**: A valid email address.
     * - **uri**: A valid URI.
     * - **binary**: A base64 encoded string representing binary data.
     * - **uuid**: A string that is a uuid.
     */
    val format: TableSchemaFormat? = null,
    /**
     * Constraints
     *
     * The following constraints are supported for `string` fields.
     */
    val constraints: TableSchemaFieldStringConstraints? = null,
    /**
     * The RDF type for this field.
     */
    val rdfType: String? = null




) : TableSchemaField() {
    override fun isOptional(): Boolean {
        return !(constraints?.required ?: false)
    }
}

@Serializable
data class TableSchemaFieldStringConstraints(
    /**
     * Indicates whether a property must have a value for each instance.
     *
     * An empty string is considered to be a missing value.
     */
    val required: Boolean? = null,
    /**
     * When `true`, each value for the property `MUST` be unique.
     */
    val unique: Boolean? = null,
    /**
     * A regular expression pattern to test each value of the property against, where a truthy response indicates validity.
     */
    val pattern: String? = null,
    /**
     *
     */
    val enum: Set<String> = emptySet(),
    /**
     * An integer that specifies the minimum length of a value.
     */
    val minLength: Int? = null,
    /**
     * An integer that specifies the maximum length of a value.
     */
    val maxLength: Int? = null


)

@Serializable
data class TableSchemaFieldNumber(
    override val name: String,
    override val title: String? = null,
    override val description: String? = null,
    override val example: String? = null,
    override val type: String = "number",
    /**
     * There are no format keyword options for `number`: only `default` is allowed.
     */
    val format: String = "default",
    /**
     * a boolean field with a default of `true`. If `true` the physical contents of this field must follow the formatting constraints already set out. If `false` the contents of this field may contain leading and/or trailing non-numeric characters (which implementors MUST therefore strip). The purpose of `bareNumber` is to allow publishers to publish numeric data that contains trailing characters such as percentages e.g. `95%` or leading characters such as currencies e.g. `€95` or `EUR 95`. Note that it is entirely up to implementors what, if anything, they do with stripped text.
     */
    val bareNumber: Boolean? = null,
    /**
     * A string whose value is used to represent a decimal point within the number. The default value is `.`.
     */
    val decimalChar: String = ".",
    /**
     * A string whose value is used to group digits within the number. The default value is `null`. A common value is `,` e.g. '100,000'.
     */
    val groupChar: String? = null,

    val constraints: TableSchemaFieldNumberConstraints? = null,

    /**
     * The RDF type for this field.
     */
    val rdfType: String? = null

) : TableSchemaField() {
    override fun isOptional(): Boolean {
        return !(constraints?.required ?: false)
    }
}


@Serializable
data class TableSchemaFieldNumberConstraints(
    /**
     * Indicates whether a property must have a value for each instance.
     *
     * An empty string is considered to be a missing value.
     */
    val required: Boolean? = null,
    /**
     * When `true`, each value for the property `MUST` be unique.
     */
    val unique: Boolean? = null,
    /**
     *
     */
    val enum: Set<StringOrNumber> = emptySet(),
    /**
     * An integer that specifies the minimum length of a value.
     */
    val minimum: StringOrNumber? = null,
    /**
     * An integer that specifies the maximum length of a value.
     */
    val maximum: StringOrNumber? = null

)


@Serializable
data class TableSchemaFieldInteger(
    override val name: String,
    override val title: String? = null,
    override val description: String? = null,
    override val example: String? = null,
    override val type: String = "integer",
    /**
     * There are no format keyword options for `integer`: only `default` is allowed.
     */
    val format: String = "default",
    /**
     * a boolean field with a default of `true`. If `true` the physical contents of this field must follow the formatting constraints already set out. If `false` the contents of this field may contain leading and/or trailing non-numeric characters (which implementors MUST therefore strip). The purpose of `bareNumber` is to allow publishers to publish numeric data that contains trailing characters such as percentages e.g. `95%` or leading characters such as currencies e.g. `€95` or `EUR 95`. Note that it is entirely up to implementors what, if anything, they do with stripped text.
     */
    val bareNumber: Boolean? = null,

    val constraints: TableSchemaFieldNumberConstraints? = null,

    /**
     * The RDF type for this field.
     */
    val rdfType: String? = null

) : TableSchemaField() {
    override fun isOptional(): Boolean {
        return !(constraints?.required ?: false)
    }
}


@Serializable
data class TableSchemaFieldDate(
    override val name: String,
    override val title: String? = null,
    override val description: String? = null,
    override val example: String? = null,
    override val type: String = "date",
    /**
     * The format keyword options for `date` are `default`, `any`, and `{PATTERN}`.
     *
     * The following `format` options are supported:
     *
     * - **default**: An ISO8601 format string of YYYY-MM-DD.
     * - **any**: Any parsable representation of a date. The implementing library can attempt to parse the datetime
     *   via a range of strategies.
     * - **{PATTERN}**: The value can be parsed according to `{PATTERN}`, which `MUST` follow the date formatting
     *   syntax of C / Python [strftime](http://strftime.org/).
     */
    val format: String = "default",

    val constraints: TableSchemaFieldDateConstraints? = null,

    /**
     * The RDF type for this field.
     */
    val rdfType: String? = null

) : TableSchemaField() {
    override fun isOptional(): Boolean {
        return !(constraints?.required ?: false)
    }
}


@Serializable
class TableSchemaFieldDateConstraints(
    /**
     * Indicates whether a property must have a value for each instance.
     *
     * An empty string is considered to be a missing value.
     */
    val required: Boolean? = null,
    /**
     * When `true`, each value for the property `MUST` be unique.
     */
    val unique: Boolean? = null,
    /**
     *
     */
    val enum: Set<String> = emptySet(),
    /**
     * An integer that specifies the minimum length of a value.
     */
    val minimum: String? = null,
    /**
     * An integer that specifies the maximum length of a value.
     */
    val maximum: String? = null
)


@Serializable
data class TableSchemaFieldTime(
    override val name: String,
    override val title: String? = null,
    override val description: String? = null,
    override val example: String? = null,
    override val type: String = "time",
    /**
     * The following `format` options are supported:
     * - **default**: An ISO8601 format string for time.
     * - **any**: Any parsable representation of a date. The implementing library can attempt to parse the
     *   datetime via a range of strategies.
     * - **{PATTERN}**: The value can be parsed according to `{PATTERN}`, which `MUST` follow the date formatting
     *   syntax of C / Python [strftime](http://strftime.org/).
     */
    val format: String = "default",


    val constraints: TableSchemaFieldDateConstraints? = null,

    /**
     * The RDF type for this field.
     */
    val rdfType: String? = null

) : TableSchemaField() {
    override fun isOptional(): Boolean {
        return !(constraints?.required ?: false)
    }
}

@Serializable
data class TableSchemaFieldDateTime(
    override val name: String,
    override val title: String? = null,
    override val description: String? = null,
    override val example: String? = null,
    override val type: String = "datetime",
    /**
     *
     * The format keyword options for `datetime` are `default`, `any`, and `{PATTERN}`.
     *
     * The following `format` options are supported
     * - **default**: An ISO8601 format string for datetime.
     * - **any**: Any parsable representation of a date. The implementing library can attempt to parse the
     *   datetime via a range of strategies.
     * - **{PATTERN}**: The value can be parsed according to `{PATTERN}`, which `MUST` follow the date formatting
     *   syntax of C / Python [strftime](http://strftime.org/).
     */
    val format: String = "default",


    val constraints: TableSchemaFieldDateConstraints? = null,

    /**
     * The RDF type for this field.
     */
    val rdfType: String? = null

) : TableSchemaField() {
    override fun isOptional(): Boolean {
        return !(constraints?.required ?: false)
    }
}


/**
 * A calendar year, being an integer with 4 digits. Equivalent to [gYear in XML Schema](https://www.w3.org/TR/xmlschema-2/#gYear)
 */
@Serializable
data class TableSchemaFieldYear(
    override val name: String,
    override val title: String? = null,
    override val description: String? = null,
    override val example: String? = null,
    override val type: String = "year",
    /**
     * There are no format keyword options for `year`: only `default` is allowed.
     */
    val format: String = "default",


    val constraints: TableSchemaFieldNumberConstraints? = null,

    /**
     * The RDF type for this field.
     */
    val rdfType: String? = null

) : TableSchemaField() {
    override fun isOptional(): Boolean {
        return !(constraints?.required ?: false)
    }
}

/**
 * A calendar year, being an integer with 4 digits. Equivalent to [gYear in XML Schema](https://www.w3.org/TR/xmlschema-2/#gYear)
 */
@Serializable
data class TableSchemaFieldYearMonth(
    override val name: String,
    override val title: String? = null,
    override val description: String? = null,
    override val example: String? = null,
    override val type: String = "yearmonth",
    /**
     * There are no format keyword options for `year`: only `default` is allowed.
     */
    val format: String = "default",
    /**
     * The following constraints are supported for `yearmonth` fields.
     */
    val constraints: TableSchemaFieldDateConstraints? = null,

    /**
     * The RDF type for this field.
     */
    val rdfType: String? = null

) : TableSchemaField() {
    override fun isOptional(): Boolean {
        return !(constraints?.required ?: false)
    }
}

/**
 * The field contains boolean (true/false) data.
 */
@Serializable
data class TableSchemaFieldBoolean(
    override val name: String,
    override val title: String? = null,
    override val description: String? = null,
    override val example: String? = null,
    override val type: String = "boolean",
    /**
     * There are no format keyword options for `boolean`: only `default` is allowed.
     */
    val format: String = "default",
    val trueValues: Set<String> = setOf("true", "True", "TRUE", "1"),
    val falseValues: Set<String> = setOf("false", "False", "FALSE", "0"),
    /**
     * The following constraints are supported for `yearmonth` fields.
     */
    val constraints: TableSchemaFieldBooleanConstraints? = null,

    /**
     * The RDF type for this field.
     */
    val rdfType: String? = null

) : TableSchemaField() {
    override fun isOptional(): Boolean {
        return !(constraints?.required ?: false)
    }
}

@Serializable
data class TableSchemaFieldBooleanConstraints(
    val required: Boolean? = null,
    val enum: Set<Boolean> = emptySet(),
)


/**
 * The field contains data which can be parsed as a valid JSON object.
 */
@Serializable
data class TableSchemaFieldObject(
    override val name: String,
    override val title: String? = null,
    override val description: String? = null,
    override val example: String? = null,
    override val type: String = "object",
    /**
     * There are no format keyword options for `object`: only `default` is allowed.
     */
    val format: String = "default",
    /**
     * The following constraints are supported for `yearmonth` fields.
     */
    val constraints: TableSchemaFieldObjectConstraints? = null,

    /**
     * The RDF type for this field.
     */
    val rdfType: String? = null

) : TableSchemaField() {
    override fun isOptional(): Boolean {
        return !(constraints?.required ?: false)
    }
}

@Serializable
data class TableSchemaFieldObjectConstraints(
    val required: Boolean? = null,
    val unique: Boolean? = null,
    // TODO it should be StringOrObject but can not guess why and how objects are allowed
    val enum: Set<JsonElement> = emptySet(),
)

/**
 *
 * GeoPoint Field
 *
 * The field contains data describing a geographic point.
 */
@Serializable
data class TableSchemaFieldGeopoint(
    override val name: String,
    override val title: String? = null,
    override val description: String? = null,
    override val example: String? = null,
    override val type: String = "geopoint",
    /**
     * The format keyword options for `geopoint` are `default`,`array`, and `object`.
     *
     * The following `format` options are supported:
     *
     * - **default**: A string of the pattern 'lon, lat', where `lon` is the longitude and `lat` is the latitude.
     * - **array**: An array of exactly two items, where each item is either a number, or a string parsable as
     *   a number, and the first item is `lon` and the second item is `lat`.
     * - **object**: A JSON object with exactly two keys, `lat` and `lon`
     */
    val format: String = "default",
    /**
     * The following constraints are supported for `yearmonth` fields.
     */
    val constraints: TableSchemaFieldGeopointConstraints? = null,

    /**
     * The RDF type for this field.
     */
    val rdfType: String? = null

) : TableSchemaField() {
    override fun isOptional(): Boolean {
        return !(constraints?.required ?: false)
    }
}

@Serializable
data class TableSchemaFieldGeopointConstraints(
    val required: Boolean? = null,
    val unique: Boolean? = null,
    val enum: Set<JsonElement> = emptySet(),
)


/**
 * GeoJSON Field
 *
 * The field contains a JSON object according to GeoJSON or TopoJSON
 */
@Serializable
data class TableSchemaFieldGeoJson(
    override val name: String,
    override val title: String? = null,
    override val description: String? = null,
    override val example: String? = null,
    override val type: String = "geojson",
    /**
     * The format keyword options for `geojson` are `default` and `topojson`.
     *
     * The following `format` options are supported:
     * - **default**: A geojson object as per the [GeoJSON spec](http://geojson.org/).
     * - **topojson**: A topojson object as per the [TopoJSON spec](https://github.com/topojson/topojson-specification/blob/master/README.md)
     */
    val format: String = "default",
    /**
     * The following constraints are supported for `yearmonth` fields.
     */
    val constraints: TableSchemaFieldGeoJsonConstraints? = null,

    /**
     * The RDF type for this field.
     */
    val rdfType: String? = null

) : TableSchemaField() {
    override fun isOptional(): Boolean {
        return !(constraints?.required ?: false)
    }
}


@Serializable
data class TableSchemaFieldGeoJsonConstraints(
    val required: Boolean? = null,
    val unique: Boolean? = null,
    val enum: Set<JsonElement> = emptySet(),
    /**
     * An integer that specifies the minimum length of a value.
     */
    val minLength: Int? = null,
    /**
     * An integer that specifies the maximum length of a value.
     */
    val maxLength: Int? = null,
)

/**
 * Array field
 *
 * The field contains data which can be parsed as a valid JSON array.
 */
@Serializable
data class TableSchemaFieldArray(
    override val name: String,
    override val title: String? = null,
    override val description: String? = null,
    override val example: String? = null,
    override val type: String = "array",
    /**
     * There are no format keyword options for `array`: only `default` is allowed.
     */
    val format: String = "default",
    /**
     * The following constraints are supported for `yearmonth` fields.
     */
    val constraints: TableSchemaFieldArrayConstraints? = null,

    /**
     * The RDF type for this field.
     */
    val rdfType: String? = null

) : TableSchemaField() {
    override fun isOptional(): Boolean {
        return !(constraints?.required ?: false)
    }
}


@Serializable
data class TableSchemaFieldArrayConstraints(
    val required: Boolean? = null,
    val unique: Boolean? = null,
    // TODO should be StringOrArray only not everything
    val enum: Set<JsonElement> = emptySet(),
    /**
     * An integer that specifies the minimum length of a value.
     */
    val minLength: Int? = null,
    /**
     * An integer that specifies the maximum length of a value.
     */
    val maxLength: Int? = null,
)


/**
 * Duration Field
 *
 * The field contains a duration of time.
 *
 * The lexical representation for duration is the [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601#Durations) extended
 * format `PnYnMnDTnHnMnS`, where `nY` represents the number of years, `nM` the number of months, `nD` the number of
 * days, 'T' is the date/time separator, `nH` the number of hours, `nM` the number of minutes and `nS` the number of
 * seconds. The number of seconds can include decimal digits to arbitrary precision. Date and time elements including
 * their designator may be omitted if their value is zero, and lower order elements may also be omitted for reduced
 * precision. Here we follow the definition of [XML Schema duration datatype](http://www.w3.org/TR/xmlschema-2/#duration)
 * directly and that definition is implicitly inlined here.
 */
@Serializable
data class TableSchemaFieldDuration(
    override val name: String,
    override val title: String? = null,
    override val description: String? = null,
    override val example: String? = null,
    override val type: String = "duration",
    /**
     * There are no format keyword options for `array`: only `default` is allowed.
     */
    val format: String = "default",
    /**
     * The following constraints are supported for `yearmonth` fields.
     */
    val constraints: TableSchemaFieldDurationConstraints? = null,

    /**
     * The RDF type for this field.
     */
    val rdfType: String? = null

) : TableSchemaField() {
    override fun isOptional(): Boolean {
        return !(constraints?.required ?: false)
    }
}


@Serializable
data class TableSchemaFieldDurationConstraints(
    val required: Boolean? = null,
    val unique: Boolean? = null,
    // TODO should be StringOrArray only not everything
    val enum: Set<JsonElement> = emptySet(),
    val minimum: String? = null,
    val maximum: String? = null,
)


/**
 * Any Field
 *
 * Any value is accepted, including values that are not captured by the type/format/constraint
 * requirements of the specification.
 */
@Serializable
data class TableSchemaFieldAny(
    override val name: String,
    override val title: String? = null,
    override val description: String? = null,
    override val example: String? = null,
    override val type: String = "any",

    /**
     * The following constraints are supported for `yearmonth` fields.
     */
    val constraints: TableSchemaFieldAnyConstraints? = null,

    /**
     * The RDF type for this field.
     */
    val rdfType: String? = null

) : TableSchemaField() {
    override fun isOptional(): Boolean {
        return !(constraints?.required ?: false)
    }
}


@Serializable
data class TableSchemaFieldAnyConstraints(
    val required: Boolean? = null,
    val unique: Boolean? = null,
    val enum: Set<JsonElement> = emptySet(),
)

