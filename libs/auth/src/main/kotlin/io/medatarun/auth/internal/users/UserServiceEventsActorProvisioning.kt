package io.medatarun.auth.internal.users

import io.medatarun.auth.domain.ActorNotFoundException
import io.medatarun.auth.domain.ActorRole.Companion.ADMIN
import io.medatarun.auth.domain.actor.Actor
import io.medatarun.auth.ports.exposed.ActorService
import io.medatarun.auth.ports.needs.*

/**
 * Keeps the actor registry in sync with internal users.
 * This avoids relying on JWTs to provision internal actors.
 */
class UserServiceEventsActorProvisioning(
    private val actorService: ActorService,
    private val internalIssuer: String
) : UserServiceEvents {

    override fun fire(evt: UserEvent) {
        when (evt) {
            is UserEventCreated -> {
                val actor = actorService.findByIssuerAndSubjectOptional(internalIssuer, evt.user.username.value)
                if (actor != null) {
                    updateFromUser(evt, actor)
                } else {
                    createFromUser(evt)
                }
            }

            is UserEventFullnameChanged -> {
                val actor = actorService.findByIssuerAndSubjectOptional(internalIssuer, evt.username.value)
                    ?: throw ActorNotFoundException()
                actorService.updateFullname(actor.id, evt.fullname.value)
            }

            is UserEventDisabledChanged -> {
                val actor = actorService.findByIssuerAndSubjectOptional(internalIssuer, evt.username.value)
                    ?: throw ActorNotFoundException()
                actorService.disable(actor.id, evt.date)
            }
        }
    }

    private fun updateFromUser(evt: UserEventCreated, actor: Actor) {
        if (evt.user.admin && !actor.roles.contains(ADMIN)) {
            actorService.setRoles(actor.id, actor.roles + ADMIN)
        } else if (!evt.user.admin && actor.roles.contains(ADMIN)) {
            actorService.setRoles(actor.id, actor.roles.filter { it != ADMIN })
        }
        actorService.updateFullname(actor.id, evt.user.fullname.value)
        actorService.disable(actor.id, evt.user.disabledDate)
    }

    private fun createFromUser(evt: UserEventCreated) {
        actorService.create(
            issuer = internalIssuer,
            subject = evt.user.username.value,
            fullname = evt.user.fullname.value,
            roles = if (evt.user.admin) listOf(ADMIN) else emptyList(),
            email = null,
            disabled = evt.user.disabledDate
        )
    }
}
