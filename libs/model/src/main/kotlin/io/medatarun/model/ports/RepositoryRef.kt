package io.medatarun.model.ports

/**
 * Strategy to choose a repository when operations need to perform
 * (for example, when we need to create a model)
 */
sealed interface RepositoryRef {
    /**
     * Indicates that we must take the default repository.
     *
     * If that is ambiguous, the default repository will be chosen.
     *
     * It also means that if more repositories exists, and we don't specify in which one we want to store the data,
     * an exception will be raised
     */
    object Auto : RepositoryRef

    /**
     * Indicates to use a repository identified by its name.
     * If this repository can not be found an exception is raised.
     */
    data class Id(val id: ModelRepositoryId) : RepositoryRef
}
