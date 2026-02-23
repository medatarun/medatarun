package io.medatarun.tags.core.fixtures

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.type.commons.id.Id
import java.util.UUID

/**
 * Single test-only ID type reused by all sample domain objects used in tag assignment tests.
 * We intentionally keep a shared type to reduce fixture noise.
 */
@JvmInline
value class SampleId(override val value: UUID) : Id<SampleId> {
    companion object {
        fun sampleId(): SampleId {
            return SampleId(UuidUtils.generateV7())
        }
    }
}