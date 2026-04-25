package io.medatarun.auth.internal.users

import io.medatarun.auth.domain.ActorNotFoundException
import io.medatarun.auth.domain.actor.Actor
import io.medatarun.auth.domain.role.RoleRef
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
                actorService.actorDisable(actor.id, evt.date)
            }
        }
    }

    private fun updateFromUser(evt: UserEventCreated, actor: Actor) {
        val adminRole = actorService.findSpecialAdminRole()
        if (evt.user.admin) {
            if (!actorService.actorHasRole(actor.id, adminRole.id)) {
                actorService.actorAddRole(actor.id, RoleRef.ById(adminRole.id))
            }
        } else if (!evt.user.admin) {
            if (actorService.actorHasRole(actor.id, adminRole.id)) {
                actorService.actorDeleteRole(actor.id, RoleRef.ById(adminRole.id))
            }
        }
        actorService.updateFullname(actor.id, evt.user.fullname.value)
        actorService.actorDisable(actor.id, evt.user.disabledDate)
    }

    private fun createFromUser(evt: UserEventCreated) {
        val admin = evt.user.admin
        val actor = actorService.create(
            issuer = internalIssuer,
            subject = evt.user.username.value,
            fullname = evt.user.fullname.value,
            email = null,
            disabled = evt.user.disabledDate
        )
        if (admin) {
            val adminRole = actorService.findSpecialAdminRole()
            actorService.actorAddRole(actor.id, RoleRef.ById(adminRole.id))
        } else {
            actorService.actorAddAutoAssignRoleIfExists(actor.id)
        }
    }
}
