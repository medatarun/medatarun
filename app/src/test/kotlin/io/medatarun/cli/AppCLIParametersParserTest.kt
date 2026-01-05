package io.medatarun.cli

import io.medatarun.actions.runtime.ActionParamJsonType
import io.medatarun.actions.runtime.ActionParamJsonType.STRING
import io.medatarun.httpserver.cli.CliActionDto
import io.medatarun.httpserver.cli.CliActionParamDto
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppCLIParametersParserTest {

    val service = AppCLIParametersParser()


    @Test
    fun `action without parameters raise no error`() {
        val json = service.parseParameters(
            arrayOf(),
            createAction()
        )
        assertTrue { json.isEmpty() }
    }

    @Test
    fun `given unknown parameters raise error`() {
        assertThrows<CliParameterUnknownException> {
            service.parseParameters(
                arrayOf("--name=machin", "--type=bidule"),
                createAction(createActionParam("name", STRING))
            )
        }
    }

    @Test
    fun `given missing parameter raise error`() {
        assertThrows<CliParameterMissingException> {
            service.parseParameters(
                arrayOf("--name=machin"),
                createAction(createActionParam("name", STRING), createActionParam("type", STRING))
            )
        }
    }

    @Test
    fun `given missing optional parameter then ok`() {
        val json = service.parseParameters(
            arrayOf("--name=machin"),
            createAction(createActionParam("name", STRING), createActionParam("type", STRING, optional = true))
        )
        assertEquals(JsonPrimitive("machin"), json["name"])
        assertTrue(json.containsKey("type"))
        assertEquals(JsonNull, json["type"])
    }

    @Test
    fun `string parameters rendered as Json strings`() {
        val json = service.parseParameters(
            arrayOf("--name=my name", "--key=my key"),
            createAction(createActionParam("name", STRING), createActionParam("key", STRING))
        )
        assertEquals(JsonPrimitive("my name"), json["name"])
        assertEquals(JsonPrimitive("my key"), json["key"])
    }


    companion object {

        fun createAction(vararg params: CliActionParamDto): CliActionDto {
            return CliActionDto(
                actionGroupKey = "dummy",
                actionKey = "dummy",
                title = null,
                description = null,
                parameters = params.asList(),
            )
        }

        fun createActionParam(
            key: String,
            jsonType: ActionParamJsonType,
            optional: Boolean = false
        ): CliActionParamDto {
            return CliActionParamDto(
                key = key,
                optional = optional,
                title = null,
                description = null,
                order = 0,
                multiplatformType = "String",
                jsonType = jsonType.code
            )
        }

    }
}
