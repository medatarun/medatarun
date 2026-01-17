package io.medatarun.ext.frictionlessdata

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

class DataPackageSerializer {



    object TableSchemaFieldSerializer : JsonContentPolymorphicSerializer<TableSchemaField>(TableSchemaField::class) {
        override fun selectDeserializer(element: JsonElement): KSerializer<out TableSchemaField> {
            val type = element.jsonObject["type"]?.jsonPrimitive?.content
            return when (type) {
                "string" -> TableSchemaFieldString.serializer()
                "number" -> TableSchemaFieldNumber.serializer()
                "integer" -> TableSchemaFieldInteger.serializer()
                "date" -> TableSchemaFieldDate.serializer()
                "datetime" -> TableSchemaFieldDateTime.serializer()
                "time" -> TableSchemaFieldTime.serializer()
                "year" -> TableSchemaFieldYear.serializer()
                "yearmonth" -> TableSchemaFieldYearMonth.serializer()
                "boolean" -> TableSchemaFieldBoolean.serializer()
                "object" -> TableSchemaFieldObject.serializer()
                "geopoint" -> TableSchemaFieldGeopoint.serializer()
                "geojson" -> TableSchemaFieldGeoJson.serializer()
                "array" -> TableSchemaFieldArray.serializer()
                "duration" -> TableSchemaFieldDuration.serializer()
                "any" -> TableSchemaFieldAny.serializer()
                else -> throw TableSchemaFieldTypeUnknown(element)
            }
        }
    }

    val jsonTableSchema = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            this.contextual(TableSchemaFieldSerializer)
            this.contextual(ListSerializer(TableSchemaFieldSerializer))
        }
    }
    val jsonDataPackage = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            this.contextual(TableSchemaFieldSerializer)
            this.contextual(ListSerializer(TableSchemaFieldSerializer))
        }
    }

    fun readTableSchema(str: String): TableSchema {
        return jsonTableSchema.decodeFromString<TableSchema>(str)
    }

    fun readDataPackage(str: String): DataPackage {
        return jsonDataPackage.decodeFromString<DataPackage>(str)
    }

}