package io.medatarun.auth.domain

import io.medatarun.lang.http.StatusCode
import io.medatarun.model.domain.MedatarunException

class JwtMissingIssuerException :
    MedatarunException("JWT missing issuer.", StatusCode.UNAUTHORIZED)

class JwtMissingAudienceException :
    MedatarunException("JWT missing audience.", StatusCode.UNAUTHORIZED)

class JwtMissingAlgException :
    MedatarunException("JWT missing algorithm.", StatusCode.UNAUTHORIZED)

class JwtUnknownIssuerException(issuer: String) :
    MedatarunException("JWT issuer [$issuer] is not recognized.", StatusCode.UNAUTHORIZED)

class JwtUnsupportedAlgException(alg: String) :
    MedatarunException("JWT algorithm [$alg] is not supported.", StatusCode.UNAUTHORIZED)

class JwtMissingKidException :
    MedatarunException("JWT header missing kid.", StatusCode.UNAUTHORIZED)

class JwtUnknownKidException(kid: String, issuer: String) :
    MedatarunException("JWT kid [$kid] not found for issuer [$issuer].", StatusCode.UNAUTHORIZED)

class JwtJwksFetchException(issuer: String, jwksUri: String) :
    MedatarunException("Could not fetch JWKS for issuer [$issuer] at [$jwksUri].", StatusCode.UNAUTHORIZED)

class JwtJwksParseException(issuer: String) :
    MedatarunException("Invalid JWKS payload for issuer [$issuer].", StatusCode.UNAUTHORIZED)

class JwtUnsupportedKeyTypeException :
    MedatarunException("Unsupported JWKS key type.", StatusCode.UNAUTHORIZED)

class JwtMalformedTokenException :
    MedatarunException("JWT format is invalid.", StatusCode.UNAUTHORIZED)

class JwtSignatureInvalidException :
    MedatarunException("JWT signature or claims are invalid.", StatusCode.UNAUTHORIZED)

class JwtAudienceMismatchException(expected: List<String>) :
    MedatarunException("JWT audience does not match expected values: $expected.", StatusCode.UNAUTHORIZED)

class ExternalOidcProviderMissingConfigException(providerName: String, key: String) :
    MedatarunException("Missing configuration [$key] for external OIDC provider [$providerName].")

class ExternalOidcProviderIssuerConflictException(issuer: String) :
    MedatarunException("External OIDC issuer [$issuer] conflicts with internal issuer.")

class ExternalOidcProviderCacheDurationInvalidException(value: String) :
    MedatarunException("Invalid JWKS cache duration [$value]. Expected a long (seconds).", StatusCode.BAD_REQUEST)
