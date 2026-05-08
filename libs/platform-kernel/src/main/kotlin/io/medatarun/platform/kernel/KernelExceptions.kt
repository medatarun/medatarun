package io.medatarun.platform.kernel

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.exceptions.MedatarunTechnicalException
import java.nio.file.Path
import kotlin.reflect.KClass

class ExtensionRegistryDuplicateIdException(keys: Set<String>): MedatarunTechnicalException("Multiple extensions with the same ID has been registered: ${keys.joinToString(" ")}")
class ContributionPointIdMismatch(e: ExtensionId, c: ContributionPointId): MedatarunTechnicalException("Contribution point id [$c] declared by extension [$e] shall start with the name of the extension.")
class ContributionPointDuplicateId(e: ExtensionId, c: ContributionPointId): MedatarunTechnicalException("Contribution point id [$c] declared by extension [$e] has the same id as another contribution point. Contribution point ids shall be unique.")
class ContributionPointDuplicateInterface(e: ExtensionId, c: ContributionPointId, api: KClass<*>, other: ContributionPointId): MedatarunTechnicalException("Contribution point id [$c] declared by extension [$e] uses [$api] with is already the interface for contribution point $other. Two contribution points can not share the same interface.")
class ContributionRegistrationContributionPointNotFoundException(e: ExtensionId, api: KClass<*>): MedatarunTechnicalException("Error during registrations of extension $e, no contribution point matches this interface: [$api].")
class ContributionPointNotFoundByApiException(api: KClass<*>): MedatarunTechnicalException("No contribution point matches this interface: [$api].")
class ExtensionStoragePathNotDirectoryException(path: Path): MedatarunTechnicalException("Extension storage path [$path] is not a directory.")
class ContributionAccessWhileNotInitializedException: MedatarunTechnicalException("You can not access extension contributions while the extension registry is beeing built. You are reading contributions too soon, so you will not get them all.")