package io.medatarun.auth.domain

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.exceptions.MedatarunTechnicalException
import io.medatarun.lang.exceptions.MedatarunUserException
import io.medatarun.lang.http.StatusCode

class JwtMissingIssuerException :
    MedatarunUserException("JWT missing issuer.", StatusCode.UNAUTHORIZED)

class JwtMissingAlgException :
    MedatarunUserException("JWT missing algorithm.", StatusCode.UNAUTHORIZED)

class JwtUnknownIssuerException(issuer: String) :
    MedatarunUserException("JWT issuer [$issuer] is not recognized.", StatusCode.UNAUTHORIZED)

class JwtUnsupportedAlgException(alg: String) :
    MedatarunUserException("JWT algorithm [$alg] is not supported.", StatusCode.UNAUTHORIZED)

class JwtMissingKidException :
    MedatarunUserException("JWT header missing kid.", StatusCode.UNAUTHORIZED)

class JwtUnknownKidException(kid: String, issuer: String) :
    MedatarunUserException("JWT kid [$kid] not found for issuer [$issuer].", StatusCode.UNAUTHORIZED)

class JwtJwksFetchException(issuer: String, jwksUri: String) :
    MedatarunUserException("Could not fetch JWKS for issuer [$issuer] at [$jwksUri].", StatusCode.UNAUTHORIZED)

class JwtJwksUnknownExternalProvider(issuer: String) :
    MedatarunUserException("Unknown JWK external provider for issuer [$issuer].", StatusCode.UNAUTHORIZED)

class JwtUnsupportedKeyTypeException :
    MedatarunUserException("Unsupported JWKS key type.", StatusCode.UNAUTHORIZED)

class JwtMalformedTokenException :
    MedatarunUserException("JWT format is invalid.", StatusCode.UNAUTHORIZED)

class ExternalOidcProviderMissingConfigException(providerName: String, key: String) :
    MedatarunTechnicalException("Missing configuration [$key] for external OIDC provider [$providerName].")

class ExternalOidcProviderIssuerConflictException(issuer: String) :
    MedatarunTechnicalException("External OIDC issuer [$issuer] conflicts with internal issuer.")

class ExternalOidcProviderCacheDurationInvalidException(value: String) :
    MedatarunTechnicalException("Invalid JWKS cache duration [$value]. Expected a long (seconds).", StatusCode.BAD_REQUEST)
