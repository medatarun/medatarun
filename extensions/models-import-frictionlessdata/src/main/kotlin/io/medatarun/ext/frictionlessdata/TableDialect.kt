package io.medatarun.ext.frictionlessdata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TableDialect(
    /**
     * Profile
     *
     * The profile of this descriptor.
     */
    @SerialName($$"$schema")
    val schema: String? = null,

    /**
     * Header
     *
     * Specifies if the file includes a header row, always as the first row in the file.
     */
    val header: Boolean = true,

    val headerRows: List<Int> = emptyList(),

    val headerJoin: String = " ",

    val commentRows: List<Int> = emptyList(),

    /**
     * Comment Character
     *
     * Specifies that any row beginning with this one-character string, without preceeding whitespace, causes the entire line to be ignored.
     */
    val commentChar: String? = null,
    /**
     * Delimiter
     *
     * A character sequence to use as the field separator.
     */
    val delimiter: String = ",",
    /**
     * Line Terminator
     *
     * Specifies the character sequence that must be used to terminate rows.
     */
    val lineTerminator: String = "\r\n",
    /**
     * Quote Character
     *
     * Specifies a one-character string to use as the quoting character.
     */
    val quoteChar: String = "\"",

    /**
     * Double Quote
     *
     * Specifies the handling of quotes inside fields.
     *
     * If Double Quote is set to true, two consecutive quotes must be interpreted as one.
     *
     */
    val doubleQuote: Boolean = true,

    /**
     * Escape Character
     *
     * Specifies a one-character string to use as the escape character.
     */
    val escapeChar: String? = null,
    /**
     * Null Sequence
     *
     * Specifies the null sequence, for example, `\N`.
     */
    val nullSequence: String? = null,

    /**
     * Skip Initial Space
     *
     * Specifies the interpretation of whitespace immediately following a delimiter. If false, whitespace immediately after a delimiter should be treated as part of the subsequent field.
     *
     */
    val skipInitialSpace: Boolean = false,

    val property: String? = null,

    /**
     * Documentation not in the spec: can be "array" or "object"
     */
    val itemType: String? = null,

    val itemKeys: List<String> = emptyList(),
    val sheetNumber: Int? = null,
    val sheetName: String? = null,
    val table: String? = null,

)
