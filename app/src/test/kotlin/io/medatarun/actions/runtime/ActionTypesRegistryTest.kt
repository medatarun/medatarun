package io.medatarun.actions.runtime

import io.medatarun.model.domain.MedatarunException
import io.medatarun.types.JsonTypeEquiv
import io.medatarun.types.TypeDescriptor
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant
import java.time.LocalDate
import kotlin.reflect.typeOf
import kotlin.test.assertEquals

@OptIn(ExperimentalStdlibApi::class)
class ActionTypesRegistryTest {

    private class CustomType(val value: String)

    private class UnknownType(val value: String)

    private class CustomTypeDescriptor : TypeDescriptor<CustomType> {
        override val target = CustomType::class
        override fun validate(value: CustomType): CustomType {
            return value
        }
        override val equivMultiplatorm: String = "CustomTypeAlias"
        override val equivJson: JsonTypeEquiv = JsonTypeEquiv.OBJECT
    }

    @JvmInline
    private value class PhoneNumber(val value: String)

    private class PhoneNumberDescriptor : TypeDescriptor<PhoneNumber> {
        override val target = PhoneNumber::class
        override fun validate(value: PhoneNumber): PhoneNumber {
            if (!value.value.matches(Regex("^\\+[0-9]{6,15}$"))) {
                throw InvalidPhoneNumberException(value.value)
            }
            return value
        }
        override val equivMultiplatorm: String = "PhoneNumber"
        override val equivJson: JsonTypeEquiv = JsonTypeEquiv.STRING
    }

    private class InvalidPhoneNumberException(number: String) : MedatarunException(
        "Invalid phone number format: $number"
    )

    @Test
    fun `toJsonType should use descriptor mapping when available`() {
        val registry = ActionTypesRegistry(listOf(CustomTypeDescriptor()))

        val result = registry.toJsonType(typeOf<CustomType>())

        assertEquals(JsonTypeEquiv.OBJECT, result)
    }

    @Test
    fun `toMultiplatformType should use descriptor mapping when available`() {
        val registry = ActionTypesRegistry(listOf(CustomTypeDescriptor()))

        val result = registry.toMultiplatformType(typeOf<CustomType>())

        assertEquals("CustomTypeAlias", result)
    }

    @Test
    fun `toJsonType should map known primitives and lists`() {
        val registry = ActionTypesRegistry(emptyList())

        assertEquals(JsonTypeEquiv.STRING, registry.toJsonType(typeOf<String>()))
        assertEquals(JsonTypeEquiv.BOOLEAN, registry.toJsonType(typeOf<Boolean>()))
        assertEquals(JsonTypeEquiv.NUMBER, registry.toJsonType(typeOf<Int>()))
        assertEquals(JsonTypeEquiv.NUMBER, registry.toJsonType(typeOf<BigInteger>()))
        assertEquals(JsonTypeEquiv.NUMBER, registry.toJsonType(typeOf<Double>()))
        assertEquals(JsonTypeEquiv.NUMBER, registry.toJsonType(typeOf<BigDecimal>()))
        assertEquals(JsonTypeEquiv.ARRAY, registry.toJsonType(typeOf<List<String>>()))
        assertEquals(JsonTypeEquiv.OBJECT, registry.toJsonType(typeOf<Map<String, Int>>()))
        assertEquals(JsonTypeEquiv.STRING, registry.toJsonType(typeOf<Instant>()))
        assertEquals(JsonTypeEquiv.STRING, registry.toJsonType(typeOf<LocalDate>()))
    }

    @Test
    fun `toMultiplatformType should map known primitives and lists`() {
        val registry = ActionTypesRegistry(emptyList())

        assertEquals("String", registry.toMultiplatformType(typeOf<String>()))
        assertEquals("Boolean", registry.toMultiplatformType(typeOf<Boolean>()))
        assertEquals("List<String>", registry.toMultiplatformType(typeOf<List<String>>()))
        assertEquals("Integer", registry.toMultiplatformType(typeOf<Int>()))
        assertEquals("Integer", registry.toMultiplatformType(typeOf<BigInteger>()))
        assertEquals("Decimal", registry.toMultiplatformType(typeOf<Double>()))
        assertEquals("Decimal", registry.toMultiplatformType(typeOf<BigDecimal>()))
        assertEquals("Map<String,Integer>", registry.toMultiplatformType(typeOf<Map<String, Int>>()))
        assertEquals("Instant", registry.toMultiplatformType(typeOf<Instant>()))
        assertEquals("LocalDate", registry.toMultiplatformType(typeOf<LocalDate>()))
    }

    @Test
    fun `toJsonType should throw for unknown types`() {
        val registry = ActionTypesRegistry(emptyList())

        assertThrows<UndefinedMultiplatformTypeException> {
            registry.toJsonType(typeOf<UnknownType>())
        }
    }

    @Test
    fun `toMultiplatformType should throw for unknown types`() {
        val registry = ActionTypesRegistry(emptyList())

        assertThrows<UndefinedMultiplatformTypeException> {
            registry.toMultiplatformType(typeOf<UnknownType>())
        }
    }

    @Test
    fun `findValidator should return noop when type is not registered`() {
        val registry = ActionTypesRegistry(emptyList())

        val validator = registry.findValidator(String::class)

        assertEquals("value", validator.validate("value"))
    }

    @Test
    fun `findValidator should delegate to descriptor validator`() {
        val registry = ActionTypesRegistry(listOf(PhoneNumberDescriptor()))

        val validator = registry.findValidator(PhoneNumber::class)

        assertThrows<InvalidPhoneNumberException> {
            validator.validate(PhoneNumber("12345"))
        }
    }
}
