package io.medatarun.ext.db.internal.modelimport

import io.medatarun.ext.db.internal.drivers.DbDriverManager
import io.medatarun.ext.db.model.DbDatasource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.DatabaseMetaData
import java.sql.ResultSet

class DbIntrospect(val dbDriverManager: DbDriverManager) {

    fun introspect(connection: DbDatasource): IntrospectResult {
        val extractedTables = mutableListOf<IntrospectTable>()
        dbDriverManager.getConnection(connection).use { conn ->
            val meta: DatabaseMetaData = conn.metaData

            meta.getTables(conn.catalog, conn.schema, "%", arrayOf("TABLE")).use { tables ->
                while (tables.next()) {
                    val dbTable = introspectTable(conn.catalog, conn.schema, tables, meta)
                    logger.debug("IntrospectTable: {}", dbTable)
                    extractedTables.add(dbTable)
                }
            }
        }
        return IntrospectResult(extractedTables)
    }

    /**
     * Extract table information
     *
     * See [DatabaseMetaData.getTables] documentation for available resultset columns
     */
    private fun introspectTable(
        catalog: String?,
        schema: String?,
        rs: ResultSet,
        meta: DatabaseMetaData
    ): IntrospectTable {
        val tableCat: String? = rs.getString("TABLE_CAT")
        val tableSchem: String? = rs.getString("TABLE_SCHEM")
        val tableName: String = rs.getString("TABLE_NAME")
        val tableType: String = rs.getString("TABLE_TYPE")
        val remarks = rs.getString("REMARKS")
        val typeCat: String? = rs.getString("TYPE_CAT")
        val typeSchem: String? = rs.getString("TYPE_SCHEM")
        val typeName: String? = rs.getString("TYPE_NAME")
        val selfReferencingColName: String? = rs.getString("SELF_REFERENCING_COL_NAME")
        val refGeneration: String? = rs.getString("REF_GENERATION")

        val primaryKeySet = mutableListOf<IntrospectPk>()
        meta.getPrimaryKeys(catalog, schema, tableName).use { rs ->
            while (rs.next()) {
                primaryKeySet.add(introspectPrimaryKey(rs))
            }
        }

        val extractedColumns = mutableListOf<IntrospectTableColumn>()
        meta.getColumns(catalog, schema, tableName, "%").use { cols ->
            while (cols.next()) {
                val dbTableColumn = introspectTableColumn(cols)
                extractedColumns.add(dbTableColumn)
            }
        }

        val extractedImportedKeys = mutableListOf<IntrospectImportedKey>()
        meta.getImportedKeys(catalog, schema, tableName).use { keys ->
            while (keys.next()) {
                val k = introspectFK(keys)
                extractedImportedKeys.add(k)
            }
        }

        return IntrospectTable(
            tableCat = tableCat,
            tableSchem = tableSchem,
            tableName = tableName,
            tableType = tableType,
            remarks = remarks,
            typeCat = typeCat,
            typeSchem = typeSchem,
            typeName = typeName,
            selfReferencingColName = selfReferencingColName,
            refGeneration = refGeneration,
            columns = extractedColumns,
            primaryKey = primaryKeySet,
            foreignKeys = extractedImportedKeys
        )
    }

    private fun introspectFK(key: ResultSet): IntrospectImportedKey {
        val k = IntrospectImportedKey(
            pkTableCat = key.getString("PKTABLE_CAT"),
            pkTableSchem = key.getString("PKTABLE_SCHEM"),
            pkTableName = key.getString("PKTABLE_NAME"),
            pkColumnName = key.getString("PKCOLUMN_NAME"),
            fkTableCat = key.getString("FKTABLE_CAT"),
            fkTableSchem = key.getString("FKTABLE_SCHEM"),
            fkTableName = key.getString("FKTABLE_NAME"),
            fkColumnName = key.getString("FKCOLUMN_NAME"),
            keySeq = key.getShort("KEY_SEQ"),
            updateRule = when (key.getInt("UPDATE_RULE")) {
                DatabaseMetaData.importedKeyNoAction -> IntrospectImportedKeyUpdateRule.importedKeyNoAction
                DatabaseMetaData.importedKeyCascade -> IntrospectImportedKeyUpdateRule.importedKeyCascade
                DatabaseMetaData.importedKeySetNull -> IntrospectImportedKeyUpdateRule.importedKeySetNull
                DatabaseMetaData.importedKeySetDefault -> IntrospectImportedKeyUpdateRule.importedKeySetDefault
                DatabaseMetaData.importedKeyRestrict -> IntrospectImportedKeyUpdateRule.importedKeyNoAction
                else -> IntrospectImportedKeyUpdateRule.importedKeyNoAction
            },
            deleteRule = when (key.getInt("DELETE_RULE")) {
                DatabaseMetaData.importedKeyNoAction -> IntrospectImportedKeyDeleteRule.importedKeyNoAction
                DatabaseMetaData.importedKeyCascade -> IntrospectImportedKeyDeleteRule.importedKeyCascade
                DatabaseMetaData.importedKeySetNull -> IntrospectImportedKeyDeleteRule.importedKeySetNull
                DatabaseMetaData.importedKeyRestrict -> IntrospectImportedKeyDeleteRule.importedKeyNoAction
                DatabaseMetaData.importedKeySetDefault -> IntrospectImportedKeyDeleteRule.importedKeySetDefault
                else -> IntrospectImportedKeyDeleteRule.importedKeyNoAction
            },
            fkName = key.getString("FK_NAME"),
            pkName = key.getString("PK_NAME"),
            deferrability = when (key.getInt("DEFERRABILITY")) {
                DatabaseMetaData.importedKeyInitiallyDeferred -> IntrospectImportedKeyDeferrability.importedKeyInitiallyDeferred
                DatabaseMetaData.importedKeyInitiallyImmediate -> IntrospectImportedKeyDeferrability.importedKeyInitiallyImmediate
                DatabaseMetaData.importedKeyNotDeferrable -> IntrospectImportedKeyDeferrability.importedKeyNotDeferrable
                else -> IntrospectImportedKeyDeferrability.importedKeyInitiallyDeferred

            }
        )
        return k
    }

    /**
     * Extract primary key information
     * See [DatabaseMetaData.getPrimaryKeys] documentation for resultset's columns
     */

    private fun introspectPrimaryKey(rs: ResultSet): IntrospectPk {
        return IntrospectPk(
            tableName = rs.getString("TABLE_NAME"),
            columnName = rs.getString("COLUMN_NAME"),
            keySeq = rs.getShort("KEY_SEQ"),
            pkName = rs.getString("PK_NAME"),
        )
    }

    /**
     * Extract column informations
     * See [DatabaseMetaData.getColumns] documentation for available columns
     */
    private fun introspectTableColumn(rs: ResultSet): IntrospectTableColumn {

        val columnName: String = rs.getString("COLUMN_NAME")
        val typeName: String = rs.getString("TYPE_NAME")
        val remarks: String? = rs.getString("REMARKS")
        val columnSize: Int = rs.getInt("COLUMN_SIZE")
        val isNullableInt: Int = rs.getInt("NULLABLE")
        val decimalDigits: Int = rs.getInt("DECIMAL_DIGITS")
        val numPrecRadix: Int = rs.getInt("NUM_PREC_RADIX")
        val columnDef: String? = rs.getString("COLUMN_DEF")
        val charOctetLength: String? = rs.getString("CHAR_OCTET_LENGTH")
        val ordinalPosition: Int = rs.getInt("ORDINAL_POSITION")
        val scopeCatalog: String? = rs.getString("SCOPE_CATALOG")
        val scopeSchema: String? = rs.getString("SCOPE_SCHEMA")
        val scopeTable: String? = rs.getString("SCOPE_TABLE")
        val isAutoIncrementStr: String = rs.getString("IS_AUTOINCREMENT")
        val isAutoIncrement: Boolean? = when (isAutoIncrementStr) {
            "YES" -> true
            "NO" -> false
            else -> null
        }
        val isGeneratedColumnStr: String = rs.getString("IS_GENERATEDCOLUMN")
        val isGeneratedColumn: Boolean? = when (isGeneratedColumnStr) {
            "YES" -> true
            "NO" -> false
            else -> null
        }

        val isNullable: Boolean? = when (isNullableInt) {
            DatabaseMetaData.columnNoNulls -> false
            DatabaseMetaData.columnNullable -> true
            else -> null
        }

        val introspectTableColumn = IntrospectTableColumn(
            columnName = columnName,
            typeName = typeName,
            remarks = remarks,
            columnSize = columnSize,
            isNullableInt = isNullableInt,
            decimalDigits = decimalDigits,
            numPrecRadix = numPrecRadix,
            columnDef = columnDef,
            charOctetLength = charOctetLength,
            ordinalPosition = ordinalPosition,
            scopeCatalog = scopeCatalog,
            scopeSchema = scopeSchema,
            scopeTable = scopeTable,
            isAutoIncrement = isAutoIncrement,
            isGeneratedColumn = isGeneratedColumn,
            isNullable = isNullable,
        )

        logger.debug("Found column {}", introspectTableColumn)

        return introspectTableColumn
    }
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(DbIntrospect::class.java)
    }
}