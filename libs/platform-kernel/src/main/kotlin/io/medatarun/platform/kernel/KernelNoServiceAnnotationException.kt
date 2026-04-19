package io.medatarun.platform.kernel

import io.medatarun.lang.exceptions.MedatarunException
import kotlin.reflect.KClass

class KernelNoServiceAnnotationException(clazz: KClass<*>): MedatarunException("Service $clazz registration refused must be annotated with @"+ Service::class.simpleName)
class KernelNoServiceImplAnnotationException(clazz: KClass<*>): MedatarunException("Service $clazz registration refused must be annotated with @"+ ServiceContributionPoint::class.simpleName)