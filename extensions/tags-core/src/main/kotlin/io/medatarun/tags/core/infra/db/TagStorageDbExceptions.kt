package io.medatarun.tags.core.infra.db

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.http.StatusCode

class TagEventConcurrentWriteException(
    expectedRevision: Int,
    conflictingRevision: Int
) : MedatarunException(
    "Cannot append a tag event at expected revision [$expectedRevision] because revision [$conflictingRevision] was written concurrently.",
    StatusCode.CONFLICT
)

class TagStorageEventInvalidScopeRefException(raw: String) :
    MedatarunException("Invalid tag scope ref payload [$raw]. Expected object with `type` and optional `id` for local scopes.")
