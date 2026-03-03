package io.medatarun.model.ports.needs

/**
 * Repository selection kept in the command API for compatibility.
 *
 * The current runtime exposes a single native SQL repository, so this selector is effectively ignored there.
 */
sealed interface RepositoryRef {
    /**
     * Indicates that the runtime should use its default repository.
     */
    object Auto : RepositoryRef

    /**
     * Indicates to use a repository identified by its name when the runtime supports it.
     */
    data class Id(val id: ModelRepositoryId) : RepositoryRef

    companion object {
        fun ModelRepositoryId.ref() = Id(this)
    }
}
