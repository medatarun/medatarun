package io.medatarun.ext.frictionlessdata

import io.medatarun.model.infra.ModelTypeInMemory
import io.medatarun.model.model.ModelTypeId

@Suppress("PropertyName")
class FrictionlessTypes {

    val type_string = ModelTypeInMemory(id = ModelTypeId("string"), name = null, description = null)
    val type_number = ModelTypeInMemory(id = ModelTypeId("number"), name = null, description = null)
    val type_integer = ModelTypeInMemory(id = ModelTypeId("integer"), name = null, description = null)
    val type_date = ModelTypeInMemory(id = ModelTypeId("date"), name = null, description = null)
    val type_datetime = ModelTypeInMemory(id = ModelTypeId("datetime"), name = null, description = null)
    val type_time = ModelTypeInMemory(id = ModelTypeId("time"), name = null, description = null)
    val type_year = ModelTypeInMemory(id = ModelTypeId("year"), name = null, description = null)
    val type_yearmonth = ModelTypeInMemory(id = ModelTypeId("yearmonth"), name = null, description = null)
    val type_boolean = ModelTypeInMemory(id = ModelTypeId("boolean"), name = null, description = null)
    val type_object = ModelTypeInMemory(id = ModelTypeId("object"), name = null, description = null)
    val type_geopoint = ModelTypeInMemory(id = ModelTypeId("geopoint"), name = null, description = null)
    val type_geojson = ModelTypeInMemory(id = ModelTypeId("geojson"), name = null, description = null)
    val type_array = ModelTypeInMemory(id = ModelTypeId("array"), name = null, description = null)
    val type_duration = ModelTypeInMemory(id = ModelTypeId("duration"), name = null, description = null)
    val type_any = ModelTypeInMemory(id = ModelTypeId("any"), name = null, description = null)

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