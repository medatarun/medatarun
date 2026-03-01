package io.medatarun.tags.core

import io.medatarun.lang.exceptions.MedatarunException

/**
 * Test-only exception used in fixtures and test harness to avoid leaking IllegalStateException in test code.
 */
class TagTestIllegalStateException(message: String) : MedatarunException(message)
