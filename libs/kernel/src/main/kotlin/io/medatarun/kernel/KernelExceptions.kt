package io.medatarun.kernel

import io.medatarun.lang.exceptions.MedatarunException
import java.nio.file.Path
import kotlin.reflect.KClass

class ExtensionRegistryDuplicateIdException(keys: Set<String>): MedatarunException("Multiple extensions with the same ID has been registered: ${keys.joinToString(" ")}")
class ContributionPointIdMismatch(e: ExtensionId, c: ContributionPointId): MedatarunException("Contribution point id [$c] declared by extension [$e] shall start with the name of the extension.")
class ContributionPointDuplicateId(e: ExtensionId, c: ContributionPointId): MedatarunException("Contribution point id [$c] declared by extension [$e] has the same id as another contribution point. Contribution point ids shall be unique.")
class ContributionPointDuplicateInterface(e: ExtensionId, c: ContributionPointId, api: KClass<*>, other: ContributionPointId): MedatarunException("Contribution point id [$c] declared by extension [$e] uses [$api] with is already the interface for contribution point $other. Two contribution points can not share the same interface.")
class ContributionRegistrationContributionPointNotFoundException(e: ExtensionId, api: KClass<*>): MedatarunException("Error during registrations of extension $e, no contribution point matches this interface: [$api].")
class ContributionPointNotFoundByApiException(api: KClass<*>): MedatarunException("No contribution point matches this interface: [$api].")
class ExtensionStoragePathNotDirectoryException(path: Path): MedatarunException("Extension storage path [$path] is not a directory.")