package io.medatarun.storage.eventsourcing.testkit

import io.medatarun.storage.eventsourcing.StorageCmd
import io.medatarun.storage.eventsourcing.StorageEventDescriptor
import io.medatarun.storage.eventsourcing.StorageEventEncoded
import io.medatarun.storage.eventsourcing.StorageEventJsonCodec
import io.medatarun.storage.eventsourcing.StorageEventUnknownContractException
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

abstract class StorageCmdJsonCodecTestBase<CMD : StorageCmd>(
    private val codec: StorageEventJsonCodec<CMD>,
    private val registeredContracts: Collection<StorageEventDescriptor<out CMD>>,
    private val upscale: (CMD) -> List<CMD> = { listOf(it) },
) {
    abstract val storageCmdRootClass: KClass<out StorageCmd>

    abstract fun testCases(): List<StorageCmdTestCase<CMD>>

    @Test
    fun `test cases covers all registered contracts`() {
        val cases = testCases()
        registeredContracts.forEach { desc ->
            assertTrue(
                cases.any { it.eventType == desc.eventType && it.eventVersion == desc.eventVersion },
                "Contract ${desc.eventType} v${desc.eventVersion} not covered by tests"
            )
        }
    }

    @Test
    fun `encode uses the expected event contract`() {
        for (testCase in testCases()) {
            val encoded = codec.encode(testCase.cmd)
            assertEquals(testCase.eventType, encoded.eventType, "Wrong event type for ${testCase.cmd::class.simpleName}")
            assertEquals(testCase.eventVersion, encoded.eventVersion, "Wrong event version for ${testCase.cmd::class.simpleName}")
            assertJsonEquals(testCase.json, encoded.payload, "Wrong payload for ${testCase.eventType} v${testCase.eventVersion}")
        }
    }

    @Test
    fun `decode reads the expected event contract`() {
        for (testCase in testCases()) {
            val decoded = codec.decode(StorageEventEncoded(testCase.eventType, testCase.eventVersion, testCase.json))
            assertEquals(testCase.cmd, decoded, "Wrong decoded command for ${testCase.eventType} v${testCase.eventVersion}")
        }
    }

    @Test
    fun `upscaled versions`() {
        for (testCase in testCases()) {
            assertEquals(testCase.upscaled, upscale(testCase.cmd), "Wrong upscaled for ${testCase.eventType} v${testCase.eventVersion}")
        }
    }

    @Test
    fun `decode unknown event contract throws dedicated exception`() {
        assertFailsWith<StorageEventUnknownContractException> {
            codec.decode(StorageEventEncoded("unknown_event", 1, "{}"))
        }
    }

    @Test
    fun `storage commands do not define default constructor values`() {
        val offenders = allLeafClasses(storageCmdRootClass).mapNotNull { cmdClass ->
            val constructor = cmdClass.primaryConstructor ?: return@mapNotNull null
            val optionalParams = constructor.parameters.filter { it.isOptional }.mapNotNull { it.name }
            if (optionalParams.isEmpty()) null
            else "${cmdClass.simpleName}: ${optionalParams.joinToString(", ")}"
        }
        assertTrue(
            actual = offenders.isEmpty(),
            message = "StorageCmd constructors must not define default values. Offenders: ${offenders.joinToString("; ")}"
        )
    }

    private fun allLeafClasses(kClass: KClass<*>): List<KClass<*>> =
        kClass.sealedSubclasses.flatMap { subclass ->
            if (subclass.sealedSubclasses.isEmpty()) listOf(subclass)
            else allLeafClasses(subclass)
        }

    private fun assertJsonEquals(expected: String, actual: String, message: String) {
        assertEquals(normalizeJson(expected), normalizeJson(actual), message)
    }

    private fun normalizeJson(value: String): String =
        Json.parseToJsonElement(value).toString()
}
