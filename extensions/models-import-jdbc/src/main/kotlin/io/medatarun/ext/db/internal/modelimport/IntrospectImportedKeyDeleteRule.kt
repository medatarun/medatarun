package io.medatarun.ext.db.internal.modelimport

internal enum class IntrospectImportedKeyDeleteRule { importedKeyNoAction, importedKeyCascade, importedKeySetNull, importedKeySetDefault }