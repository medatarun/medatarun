package io.medatarun.type.commons.key

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.http.StatusCode


class KeyInvalidFormatException :
    MedatarunException("Invalid key format", StatusCode.BAD_REQUEST)

class KeyEmptyException :
    MedatarunException("Invalid key format, a key can not be empty", StatusCode.BAD_REQUEST)

class KeyTooLongException(maxsize: Int) :
    MedatarunException("Key size can not exceed $maxsize characters", StatusCode.BAD_REQUEST)
