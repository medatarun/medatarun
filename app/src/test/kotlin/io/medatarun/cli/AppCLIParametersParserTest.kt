package io.medatarun.cli

import io.medatarun.httpserver.cli.CliActionDto
import io.medatarun.httpserver.cli.CliActionParamDto
import io.medatarun.types.JsonTypeEquiv
import io.medatarun.types.JsonTypeEquiv.*
import kotlinx.serialization.json.*
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

    @Test
    fun `invalid parameter format without equals raises error`() {
        assertThrows<CliParameterFormatException> {
            service.parseParameters(
                arrayOf("--name"),
                createAction(createActionParam("name", STRING))
            )
        }
    }

    @Test
    fun `invalid parameter format with blank key raises error`() {
        assertThrows<CliParameterFormatException> {
            service.parseParameters(
                arrayOf("--=value"),
                createAction(createActionParam("name", STRING))
            )
        }
    }

    @Test
    fun `boolean parameters rendered as Json booleans`() {
        val json = service.parseParameters(
            arrayOf("--flag=true", "--disabled=false"),
            createAction(createActionParam("flag", BOOLEAN), createActionParam("disabled", BOOLEAN))
        )
        assertEquals(JsonPrimitive(true), json["flag"])
        assertEquals(JsonPrimitive(false), json["disabled"])
    }

    @Test
    fun `invalid boolean parameters raise error`() {
        assertThrows<CliParameterBooleanValueException> {
            service.parseParameters(
                arrayOf("--flag=yes"),
                createAction(createActionParam("flag", BOOLEAN))
            )
        }
    }

    @Test
    fun `number parameters rendered as Json numbers`() {
        val json = service.parseParameters(
            arrayOf("--amount=42", "--ratio=3.14"),
            createAction(createActionParam("amount", NUMBER), createActionParam("ratio", NUMBER))
        )
        assertEquals(JsonPrimitive(42.0), json["amount"])
        assertEquals(JsonPrimitive(3.14), json["ratio"])
    }

    @Test
    fun `invalid number parameters raise error`() {
        assertThrows<CliParameterNumberValueException> {
            service.parseParameters(
                arrayOf("--amount=4a2"),
                createAction(createActionParam("amount", NUMBER))
            )
        }
    }

    @Test
    fun `object parameters rendered as Json objects`() {
        val json = service.parseParameters(
            arrayOf("--payload={\"name\":\"ana\",\"count\":2}"),
            createAction(createActionParam("payload", OBJECT))
        )
        val payload = json["payload"] as JsonObject
        val expected = Json.parseToJsonElement("{\"name\":\"ana\",\"count\":2}") as JsonObject
        assertEquals(expected, payload)
    }

    @Test
    fun `object parameters reject arrays`() {
        assertThrows<CliParameterObjectValueException> {
            service.parseParameters(
                arrayOf("--payload=[1,2]"),
                createAction(createActionParam("payload", OBJECT))
            )
        }
    }

    @Test
    fun `array parameters rendered as Json arrays`() {
        val json = service.parseParameters(
            arrayOf("--items=[\"a\",1,true]"),
            createAction(createActionParam("items", ARRAY))
        )
        val items = json["items"] as JsonArray
        val expected = Json.parseToJsonElement("[\"a\",1,true]") as JsonArray
        assertEquals(expected, items)
    }

    @Test
    fun `array parameters reject objects`() {
        assertThrows<CliParameterArrayValueException> {
            service.parseParameters(
                arrayOf("--items={\"a\":1}"),
                createAction(createActionParam("items", ARRAY))
            )
        }
    }

    @Test
    fun `invalid json raises json parse error`() {
        assertThrows<CliParameterJsonParseException> {
            service.parseParameters(
                arrayOf("--payload={\"name\":"),
                createAction(createActionParam("payload", OBJECT))
            )
        }
    }

    @Test
    fun `arguments without dashes are ignored and trigger missing required`() {
        assertThrows<CliParameterMissingException> {
            service.parseParameters(
                arrayOf("name=machin"),
                createAction(createActionParam("name", STRING))
            )
        }
    }

    @Test
    fun `duplicate parameter uses last value`() {
        val json = service.parseParameters(
            arrayOf("--name=first", "--name=second"),
            createAction(createActionParam("name", STRING))
        )
        assertEquals(JsonPrimitive("second"), json["name"])
    }

    @Test
    fun `empty string value kept for string parameter`() {
        val json = service.parseParameters(
            arrayOf("--name="),
            createAction(createActionParam("name", STRING))
        )
        assertEquals(JsonPrimitive(""), json["name"])
    }

    @Test
    fun `empty value for boolean raises error`() {
        assertThrows<CliParameterBooleanValueException> {
            service.parseParameters(
                arrayOf("--flag="),
                createAction(createActionParam("flag", BOOLEAN))
            )
        }
    }

    @Test
    fun `empty value for number raises error`() {
        assertThrows<CliParameterNumberValueException> {
            service.parseParameters(
                arrayOf("--amount="),
                createAction(createActionParam("amount", NUMBER))
            )
        }
    }

    @Test
    fun `empty value for object raises json parse error`() {
        assertThrows<CliParameterJsonParseException> {
            service.parseParameters(
                arrayOf("--payload="),
                createAction(createActionParam("payload", OBJECT))
            )
        }
    }

    @Test
    fun `empty value for array raises json parse error`() {
        assertThrows<CliParameterJsonParseException> {
            service.parseParameters(
                arrayOf("--items="),
                createAction(createActionParam("items", ARRAY))
            )
        }
    }

    @Test
    fun `object parameters reject primitives`() {
        assertThrows<CliParameterObjectValueException> {
            service.parseParameters(
                arrayOf("--payload=true"),
                createAction(createActionParam("payload", OBJECT))
            )
        }
    }

    @Test
    fun `array parameters reject primitives`() {
        assertThrows<CliParameterArrayValueException> {
            service.parseParameters(
                arrayOf("--items=123"),
                createAction(createActionParam("items", ARRAY))
            )
        }
    }

    @Test
    fun `number parameters reject NaN and Infinity`() {
        assertThrows<CliParameterNumberValueException> {
            service.parseParameters(
                arrayOf("--amount=NaN"),
                createAction(createActionParam("amount", NUMBER))
            )
        }
        assertThrows<CliParameterNumberValueException> {
            service.parseParameters(
                arrayOf("--limit=Infinity"),
                createAction(createActionParam("limit", NUMBER))
            )
        }
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
            jsonType: JsonTypeEquiv,
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
