package io.medatarun.ext.frictionlessdata

import io.medatarun.model.domain.TypeKey
import io.medatarun.model.infra.ModelTypeInMemory

@Suppress("PropertyName")
class FrictionlessTypes {

    val type_string = ModelTypeInMemory(id = TypeKey("string"), name = null, description = null)
    val type_number = ModelTypeInMemory(id = TypeKey("number"), name = null, description = null)
    val type_integer = ModelTypeInMemory(id = TypeKey("integer"), name = null, description = null)
    val type_date = ModelTypeInMemory(id = TypeKey("date"), name = null, description = null)
    val type_datetime = ModelTypeInMemory(id = TypeKey("datetime"), name = null, description = null)
    val type_time = ModelTypeInMemory(id = TypeKey("time"), name = null, description = null)
    val type_year = ModelTypeInMemory(id = TypeKey("year"), name = null, description = null)
    val type_yearmonth = ModelTypeInMemory(id = TypeKey("yearmonth"), name = null, description = null)
    val type_boolean = ModelTypeInMemory(id = TypeKey("boolean"), name = null, description = null)
    val type_object = ModelTypeInMemory(id = TypeKey("object"), name = null, description = null)
    val type_geopoint = ModelTypeInMemory(id = TypeKey("geopoint"), name = null, description = null)
    val type_geojson = ModelTypeInMemory(id = TypeKey("geojson"), name = null, description = null)
    val type_array = ModelTypeInMemory(id = TypeKey("array"), name = null, description = null)
    val type_duration = ModelTypeInMemory(id = TypeKey("duration"), name = null, description = null)
    val type_any = ModelTypeInMemory(id = TypeKey("any"), name = null, description = null)

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