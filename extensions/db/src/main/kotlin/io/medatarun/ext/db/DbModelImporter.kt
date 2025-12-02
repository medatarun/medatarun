package io.medatarun.ext.db

import io.medatarun.model.ModelImporter
import io.medatarun.model.infra.AttributeDefInMemory
import io.medatarun.model.infra.EntityDefInMemory
import io.medatarun.model.infra.ModelInMemory
import io.medatarun.model.infra.ModelTypeInMemory
import io.medatarun.model.model.*
import io.medatarun.model.ports.ResourceLocator
import org.slf4j.LoggerFactory
import java.net.URI
import java.sql.DatabaseMetaData
import java.sql.ResultSet


class DbModelImporter(val dbDriverManager: DbDriverManager) : ModelImporter {
    override fun accept(
        path: String,
        resourceLocator: ResourceLocator
    ): Boolean {
        return path.startsWith("jdbc:")
    }


    override fun toModel(
        path: String,
        resourceLocator: ResourceLocator
    ): Model {
        val tables: List<IntrospectTable> = introspect(path)
        val model = ModelInMemory(
            id = ModelId("imported-database"),
            name = LocalizedTextNotLocalized("Imported Database"),
            version = ModelVersion("0.0.0"),
            description = null,
            origin = ModelOrigin.Uri(URI(path)),
            types = tables.map { t -> t.columns.map { c -> c.typeName } }
                .flatten()
                .toSet()
                .map { ModelTypeInMemory(ModelTypeId(it), null, null) },
            entityDefs = tables.map { table ->
                EntityDefInMemory(
                    id = EntityDefId(table.tableName),
                    name = null,
                    attributes = table.columns.map {
                        AttributeDefInMemory(
                            id = AttributeDefId(it.columnName),
                            name = null,
                            description = it.remarks?.let(::LocalizedTextNotLocalized),
                            type = ModelTypeId(it.typeName),
                            optional = it.isNullable != false,
                        )
                    },
                    description = table.remarks?.let(::LocalizedTextNotLocalized),
                    identifierAttributeDefId = table.pkNameOrFirstColumn(),
                    origin = EntityOrigin.Uri(URI(path)),
                    documentationHome = null,
                    hashtags = emptyList()

                )
            },
            relationshipDefs = emptyList(), // TODO
            documentationHome = null,
            hashtags = emptyList(),
        )
        return model
    }

    private fun introspect(path: String): MutableList<IntrospectTable> {
        val extractedTables = mutableListOf<IntrospectTable>()
        dbDriverManager.getConnection(path).use { conn ->
            val meta: DatabaseMetaData = conn.metaData
            meta.getTables(null, null, "%", arrayOf("TABLE")).use { tables ->
                while (tables.next()) {
                    val dbTable = introspectTable(tables, meta)
                    logger.debug("IntrospectTable: {}", dbTable)
                    extractedTables.add(dbTable)
                }
            }
        }
        return extractedTables
    }

    /**
     * Extract table information
     *
     * See [DatabaseMetaData.getTables] documentation for available resultset columns
     */
    private fun introspectTable(
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
        meta.getPrimaryKeys(null, null, tableName).use { rs ->
            while (rs.next()) {
                primaryKeySet.add(introspectPrimaryKey(rs))
            }
        }

        val extractedColumns = mutableListOf<IntrospectTableColumn>()

        meta.getColumns(null, null, tableName, "%").use { cols ->
            while (cols.next()) {
                val dbTableColumn = introspectTableColumn(cols)
                extractedColumns.add(dbTableColumn)
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
            primaryKey = primaryKeySet
        )
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
        private val logger = LoggerFactory.getLogger(DbModelImporter::class.java)
    }
}
