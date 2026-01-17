package io.medatarun.ext.db.internal.modelimport

enum class IntrospectImportedKeyDeferrability {
    importedKeyInitiallyDeferred,
    importedKeyInitiallyImmediate,
    importedKeyNotDeferrable
}
