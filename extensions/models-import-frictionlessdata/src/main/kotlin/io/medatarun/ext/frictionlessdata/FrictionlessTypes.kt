package io.medatarun.ext.frictionlessdata

import io.medatarun.model.domain.TypeId
import io.medatarun.model.domain.TypeKey
import io.medatarun.model.infra.ModelTypeInMemory

@Suppress("PropertyName", "LocalVariableName")
class FrictionlessTypes {

    fun generateAll(): List<ModelTypeInMemory> {

        fun create(key: String): ModelTypeInMemory {
            return ModelTypeInMemory(id= TypeId.generate(), key = TypeKey(key), name = null, description = null)
        }

        val type_string = create("string")
        val type_number = create("number")
        val type_integer = create("integer")
        val type_date = create("date")
        val type_datetime = create("datetime")
        val type_time = create("time")
        val type_year = create("year")
        val type_yearmonth = create("yearmonth")
        val type_boolean = create("boolean")
        val type_object = create("object")
        val type_geopoint = create("geopoint")
        val type_geojson = create("geojson")
        val type_array = create("array")
        val type_duration = create("duration")
        val type_any = create("any")

        val all = listOf(
            type_string,
            type_number,
            type_integer,
            type_date,
            type_datetime,
            type_time,
            type_year,
            type_yearmonth,
            type_boolean,
            type_object,
            type_geopoint,
            type_geojson,
            type_array,
            type_duration,
            type_any,
        )

        return all
    }


}