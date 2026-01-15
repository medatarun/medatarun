package io.medatarun.auth.ports.needs

import io.medatarun.auth.domain.user.Fullname
import io.medatarun.auth.domain.user.User
import io.medatarun.auth.domain.user.Username
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
data class UserEventDisabledChanged(val username: Username, val date: Instant?) : UserEvent

/**
 * User full name changed (operation is already done)
 */
data class UserEventFullnameChanged(val username: Username, val fullname: Fullname) : UserEvent

/**
 * User has been created (operation is already done)
 */
data class UserEventCreated(val user: User) : UserEvent
