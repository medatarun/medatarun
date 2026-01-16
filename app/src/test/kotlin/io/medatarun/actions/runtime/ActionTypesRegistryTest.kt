package io.medatarun.actions.runtime

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonEquiv
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant
import java.time.LocalDate
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.test.assertEquals

class ActionTypesRegistryTest {

    @Suppress("unused")
    private class CustomType(val value: String)

    @Suppress("unused")
    private class UnknownType(val value: String)

    private class CustomTypeDescriptor : TypeDescriptor<CustomType> {
        override val target = CustomType::class
        override fun validate(value: CustomType): CustomType {
            return value
        }
        override val equivMultiplatorm: String = "CustomTypeAlias"
        override val equivJson: TypeJsonEquiv = TypeJsonEquiv.OBJECT
    }


    @JvmInline
    @Suppress("SameParameterValue")
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
        override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    }

    private class InvalidPhoneNumberException(number: String) : MedatarunException(
        "Invalid phone number format: $number"
    )

    @Test
    fun `toJsonType should use descriptor mapping when available`() {
        val registry = ActionTypesRegistry(listOf(CustomTypeDescriptor()))

        val result = registry.toJsonType(CustomType::class.createType())

        assertEquals(TypeJsonEquiv.OBJECT, result)
    }

    @Test
    fun `toMultiplatformType should use descriptor mapping when available`() {
        val registry = ActionTypesRegistry(listOf(CustomTypeDescriptor()))

        val result = registry.toMultiplatformType(CustomType::class.createType())

        assertEquals("CustomTypeAlias", result)
    }

    @Test
    fun `toJsonType should map known primitives and lists`() {
        val registry = ActionTypesRegistry(emptyList())

        assertEquals(TypeJsonEquiv.STRING, registry.toJsonType(String::class.createType()))
        assertEquals(TypeJsonEquiv.BOOLEAN, registry.toJsonType(Boolean::class.createType()))
        assertEquals(TypeJsonEquiv.NUMBER, registry.toJsonType(Int::class.createType()))
        assertEquals(TypeJsonEquiv.NUMBER, registry.toJsonType(BigInteger::class.createType()))
        assertEquals(TypeJsonEquiv.NUMBER, registry.toJsonType(Double::class.createType()))
        assertEquals(TypeJsonEquiv.NUMBER, registry.toJsonType(BigDecimal::class.createType()))
        assertEquals(TypeJsonEquiv.ARRAY, registry.toJsonType(listType(String::class.createType())))
        assertEquals(TypeJsonEquiv.OBJECT, registry.toJsonType(mapType(String::class.createType(), Int::class.createType())))
        assertEquals(TypeJsonEquiv.STRING, registry.toJsonType(Instant::class.createType()))
        assertEquals(TypeJsonEquiv.STRING, registry.toJsonType(LocalDate::class.createType()))
    }

    @Test
    fun `toMultiplatformType should map known primitives and lists`() {
        val registry = ActionTypesRegistry(emptyList())

        assertEquals("String", registry.toMultiplatformType(String::class.createType()))
        assertEquals("Boolean", registry.toMultiplatformType(Boolean::class.createType()))
        assertEquals("List<String>", registry.toMultiplatformType(listType(String::class.createType())))
        assertEquals("Integer", registry.toMultiplatformType(Int::class.createType()))
        assertEquals("Integer", registry.toMultiplatformType(BigInteger::class.createType()))
        assertEquals("Decimal", registry.toMultiplatformType(Double::class.createType()))
        assertEquals("Decimal", registry.toMultiplatformType(BigDecimal::class.createType()))
        assertEquals("Map<String,Integer>", registry.toMultiplatformType(mapType(String::class.createType(), Int::class.createType())))
        assertEquals("Instant", registry.toMultiplatformType(Instant::class.createType()))
        assertEquals("LocalDate", registry.toMultiplatformType(LocalDate::class.createType()))
    }

    @Test
    fun `toJsonType should throw for unknown types`() {
        val registry = ActionTypesRegistry(emptyList())

        assertThrows<UndefinedMultiplatformTypeException> {
            registry.toJsonType(UnknownType::class.createType())
        }
    }

    @Test
    fun `toMultiplatformType should throw for unknown types`() {
        val registry = ActionTypesRegistry(emptyList())

        assertThrows<UndefinedMultiplatformTypeException> {
            registry.toMultiplatformType(UnknownType::class.createType())
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

    private fun listType(elementType: KType): KType {
        return List::class.createType(listOf(KTypeProjection.invariant(elementType)))
    }

    private fun mapType(keyType: KType, valueType: KType): KType {
        val arguments = listOf(
            KTypeProjection.invariant(keyType),
            KTypeProjection.invariant(valueType)
        )
        return Map::class.createType(arguments)
    }
}
