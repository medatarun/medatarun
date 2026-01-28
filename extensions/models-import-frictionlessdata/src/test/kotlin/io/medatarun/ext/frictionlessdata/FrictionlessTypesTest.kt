package io.medatarun.ext.frictionlessdata

import io.medatarun.model.domain.TypeKey
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test


class FrictionlessTypesTest {

    @Test
    fun `should find all supported frictionless types`() {
        val list = FrictionlessTypes().generateAll()
        // Supported names comes from the spec
        val supported = listOf("string", "number", "integer", "date", "datetime", "time", "year", "yearmonth", "boolean", "object", "geopoint", "geojson", "array", "duration", "any")
        supported.forEach { tested ->
            assertTrue(list.any { it.key == TypeKey(tested) }, "$tested should be in types returned")
        }
    }

    @Test
    fun `successive calls shall never return the same ids`() {
        val list1 = FrictionlessTypes().generateAll()
        val list2 = FrictionlessTypes().generateAll()
        list1.forEach { tested ->
            assertTrue(list2.none { it.id.value == tested.id.value})
        }


    }

}