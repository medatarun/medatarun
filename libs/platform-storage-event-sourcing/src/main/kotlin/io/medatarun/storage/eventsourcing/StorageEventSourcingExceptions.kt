package io.medatarun.storage.eventsourcing

import io.medatarun.lang.exceptions.MedatarunException
import kotlin.reflect.KClass

class StorageEventDuplicateContractException(registryName: String, eventType: String, eventVersion: Int) :
    MedatarunException("Duplicate event contract [$eventType@$eventVersion] in $registryName event registry.")

class StorageEventCommandNotRegisteredException(registryName: String, kClass: KClass<*>) :
    MedatarunException("StorageCmd class [$kClass] is not registered in the event registry $registryName.")

class StorageEventUnknownContractException(registryName: String, eventType: String, eventVersion: Int) :
    MedatarunException("Unknown event contract [$eventType@$eventVersion] in event registry $registryName.")

class StorageEventMissingContractAnnotationException(className: String) :
    MedatarunException("StorageCmd class [$className] is missing ${StorageEventContract::class}.")

class StorageEventContractOnNonDataClassException(className: String) :
    MedatarunException("StorageCmd class [$className] declares ${StorageEventContract::class} but is not a data class.")

class StorageEventPayloadEncodeException(eventType: String, eventVersion: Int, cause: Throwable) :
    MedatarunException("Could not encode storage event payload for [$eventType@$eventVersion]. Cause: ${cause.message}")

class StorageEventPayloadDecodeException(eventType: String, eventVersion: Int, cause: Throwable) :
    MedatarunException("Could not decode storage event payload for [$eventType@$eventVersion]. Cause: ${cause.message}")
