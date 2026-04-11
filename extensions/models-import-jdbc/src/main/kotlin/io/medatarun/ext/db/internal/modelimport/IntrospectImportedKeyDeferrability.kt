package io.medatarun.ext.db.internal.modelimport

internal enum class IntrospectImportedKeyDeferrability {
    importedKeyInitiallyDeferred,
    importedKeyInitiallyImmediate,
    importedKeyNotDeferrable
}
