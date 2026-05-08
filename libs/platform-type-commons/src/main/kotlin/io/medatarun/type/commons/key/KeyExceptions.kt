package io.medatarun.type.commons.key

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.exceptions.MedatarunUserException
import io.medatarun.lang.http.StatusCode


class KeyInvalidFormatException :
    MedatarunUserException("Invalid key format", StatusCode.BAD_REQUEST)

class KeyEmptyException :
    MedatarunUserException("Invalid key format, a key can not be empty", StatusCode.BAD_REQUEST)

class KeyTooLongException(maxsize: Int) :
    MedatarunUserException("Key size can not exceed $maxsize characters", StatusCode.BAD_REQUEST)

class KeyStrictInvalidFormatException :
    MedatarunUserException("Invalid strict key format", StatusCode.BAD_REQUEST)

class KeyStrictEmptyException :
    MedatarunUserException("Invalid strict key format, a key can not be empty", StatusCode.BAD_REQUEST)

class KeyStrictTooLongException(maxsize: Int) :
    MedatarunUserException("Strict key size can not exceed $maxsize characters", StatusCode.BAD_REQUEST)
