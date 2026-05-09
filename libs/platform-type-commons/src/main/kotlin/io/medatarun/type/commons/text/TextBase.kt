package io.medatarun.type.commons.text

/**
 * Base type for all localizable texts.
 * Wrapper around some texts.
 */
sealed interface TextBase {

    /**
     * Returns a default text, when we don't need localization
     */
    val name: String

}





