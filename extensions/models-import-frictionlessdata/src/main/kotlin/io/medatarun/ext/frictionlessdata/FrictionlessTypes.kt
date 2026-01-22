package io.medatarun.ext.frictionlessdata

import io.medatarun.model.domain.TypeKey
import io.medatarun.model.infra.ModelTypeInMemory

@Suppress("PropertyName")
class FrictionlessTypes {

    val type_string = ModelTypeInMemory(key = TypeKey("string"), name = null, description = null)
    val type_number = ModelTypeInMemory(key = TypeKey("number"), name = null, description = null)
    val type_integer = ModelTypeInMemory(key = TypeKey("integer"), name = null, description = null)
    val type_date = ModelTypeInMemory(key = TypeKey("date"), name = null, description = null)
    val type_datetime = ModelTypeInMemory(key = TypeKey("datetime"), name = null, description = null)
    val type_time = ModelTypeInMemory(key = TypeKey("time"), name = null, description = null)
    val type_year = ModelTypeInMemory(key = TypeKey("year"), name = null, description = null)
    val type_yearmonth = ModelTypeInMemory(key = TypeKey("yearmonth"), name = null, description = null)
    val type_boolean = ModelTypeInMemory(key = TypeKey("boolean"), name = null, description = null)
    val type_object = ModelTypeInMemory(key = TypeKey("object"), name = null, description = null)
    val type_geopoint = ModelTypeInMemory(key = TypeKey("geopoint"), name = null, description = null)
    val type_geojson = ModelTypeInMemory(key = TypeKey("geojson"), name = null, description = null)
    val type_array = ModelTypeInMemory(key = TypeKey("array"), name = null, description = null)
    val type_duration = ModelTypeInMemory(key = TypeKey("duration"), name = null, description = null)
    val type_any = ModelTypeInMemory(key = TypeKey("any"), name = null, description = null)

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


    fun list(): List<ModelTypeInMemory> = all
}