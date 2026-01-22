package io.medatarun.ext.modeljson

@Suppress("MayBeConstant")
object ModelJsonSchemas {
    fun current(): String = locations.first() + "/" + v_1_1

    val locations = listOf(
        "https://raw.githubusercontent.com/medatarun/medatarun/main/schemas"
    )
    val v_1_0 = "medatarun-model-1.0.json"
    val v_1_1 = "medatarun-model-1.0.json"
}