package io.medatarun.storage.eventsourcing

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.exceptions.MedatarunTechnicalException
import kotlin.reflect.KClass

class StorageEventDuplicateContractException(registryName: String, eventType: String, eventVersion: Int) :
    MedatarunTechnicalException("Duplicate event contract [$eventType@$eventVersion] in $registryName event registry.")

class StorageEventCommandNotRegisteredException(registryName: String, kClass: KClass<*>) :
    MedatarunTechnicalException("StorageCmd class [$kClass] is not registered in the event registry $registryName.")

class StorageEventUnknownContractException(registryName: String, eventType: String, eventVersion: Int) :
    MedatarunTechnicalException("Unknown event contract [$eventType@$eventVersion] in event registry $registryName.")

class StorageEventMissingContractAnnotationException(className: String) :
    MedatarunTechnicalException("StorageCmd class [$className] is missing ${StorageEventContract::class}.")

class StorageEventContractOnNonDataClassException(className: String) :
    MedatarunTechnicalException("StorageCmd class [$className] declares ${StorageEventContract::class} but is not a data class.")

class StorageEventPayloadEncodeException(eventType: String, eventVersion: Int, cause: Throwable) :
    MedatarunTechnicalException("Could not encode storage event payload for [$eventType@$eventVersion]. Cause: ${cause.message}")

class StorageEventPayloadDecodeException(eventType: String, eventVersion: Int, cause: Throwable) :
    MedatarunTechnicalException("Could not decode storage event payload for [$eventType@$eventVersion]. Cause: ${cause.message}")
