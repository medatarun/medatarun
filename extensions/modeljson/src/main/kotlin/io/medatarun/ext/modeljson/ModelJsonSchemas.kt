package io.medatarun.ext.modeljson

object ModelJsonSchemas {
    fun current(): String = locations.first() + "/" + initial

    val locations = listOf(
        "https://raw.githubusercontent.com/medatarun/medatarun/main/schemas"
    )
    val initial = "medatarun-model-1.0.json"
}