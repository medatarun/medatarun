package io.medatarun.auth.ports.needs

import io.medatarun.auth.domain.User
import java.time.Instant

/**
 * UserService will fire various events when users are created, modified, disabled...
 */
interface UserServiceEvents {
    /**
     * Fires a [UserEvent]
     */
    fun fire(evt: UserEvent)
}

/**
 * Structure of a [UserEvent].
 */
sealed interface UserEvent

/**
 * User was marked disabled (operation is already done)
 */
data class UserEventDisabledChanged(val username: String, val date: Instant?) : UserEvent

/**
 * User full name changed (operation is already done)
 */
data class UserEventFullnameChanged(val username: String, val fullname: String) : UserEvent

/**
 * User has been created (operation is already done)
 */
data class UserEventCreated(val user: User) : UserEvent
