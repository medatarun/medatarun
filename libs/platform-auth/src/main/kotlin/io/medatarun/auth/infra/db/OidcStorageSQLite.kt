package io.medatarun.auth.infra.db

import io.medatarun.auth.domain.oidc.OidcAuthorizeCode
import io.medatarun.auth.domain.oidc.OidcAuthorizeCtx
import io.medatarun.auth.domain.oidc.AuthRefreshToken
import io.medatarun.auth.domain.oidc.AuthRefreshTokenId
import io.medatarun.auth.ports.needs.OidcStorage
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.platform.db.exposed.IdTransformer
import io.medatarun.platform.db.exposed.instant
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.time.Instant

class OidcStorageSQLite(private val dbConnectionFactory: DbConnectionFactory) : OidcStorage {
    override fun saveAuthCtx(oidcAuthorizeCtx: OidcAuthorizeCtx) {
        dbConnectionFactory.withExposed {
            AuthCtxTable.insert { row ->
                row[authorizeCtxCodeColumn] = oidcAuthorizeCtx.authCtxCode
                row[clientIdColumn] = oidcAuthorizeCtx.clientId
                row[redirectUriColumn] = oidcAuthorizeCtx.redirectUri
                row[scopeColumn] = oidcAuthorizeCtx.scope
                row[stateColumn] = oidcAuthorizeCtx.state
                row[codeChallengeColumn] = oidcAuthorizeCtx.codeChallenge
                row[codeChallengeMethodColumn] = oidcAuthorizeCtx.codeChallengeMethod
                row[nonceColumn] = oidcAuthorizeCtx.nonce
                row[createdAtColumn] = oidcAuthorizeCtx.createdAt
                row[expiresAtColumn] = oidcAuthorizeCtx.expiresAt
            }
        }
    }

    override fun findAuthCtx(authCtxId: String): OidcAuthorizeCtx {
        return dbConnectionFactory.withExposed {
            val row = AuthCtxTable.selectAll()
                .where { AuthCtxTable.authorizeCtxCodeColumn eq authCtxId }
                .singleOrNull()
                ?: throw NoSuchElementException("auth_ctx not found: $authCtxId")
            readAuthCtx(row)
        }
    }

    override fun saveAuthCode(oidcAuthorizeCode: OidcAuthorizeCode) {
        dbConnectionFactory.withExposed {
            AuthCodeTable.insert { row ->
                row[codeColumn] = oidcAuthorizeCode.code
                row[clientIdColumn] = oidcAuthorizeCode.clientId
                row[redirectUriColumn] = oidcAuthorizeCode.redirectUri
                row[subjectColumn] = oidcAuthorizeCode.subject
                row[scopeColumn] = oidcAuthorizeCode.scope
                row[codeChallengeColumn] = oidcAuthorizeCode.codeChallenge
                row[codeChallengeMethodColumn] = oidcAuthorizeCode.codeChallengeMethod
                row[nonceColumn] = oidcAuthorizeCode.nonce
                row[authTimeColumn] = oidcAuthorizeCode.authTime
                row[expiresAtColumn] = oidcAuthorizeCode.expiresAt
            }
        }
    }

    override fun findAuthCode(authCode: String): OidcAuthorizeCode? {
        return dbConnectionFactory.withExposed {
            AuthCodeTable.selectAll()
                .where { AuthCodeTable.codeColumn eq authCode }
                .singleOrNull()
                ?.let { readAuthCode(it) }
        }
    }

    override fun deleteAuthCode(authCode: String) {
        dbConnectionFactory.withExposed {
            AuthCodeTable.deleteWhere { codeColumn eq authCode }
        }
    }

    override fun deleteAuthCtx(authorizeCtxCode: String) {
        dbConnectionFactory.withExposed {
            AuthCtxTable.deleteWhere { authorizeCtxCodeColumn eq authorizeCtxCode }
        }
    }

    override fun saveRefreshToken(refreshToken: AuthRefreshToken) {
        dbConnectionFactory.withExposed {
            RefreshTokenTable.insert { row ->
                row[idColumn] = refreshToken.id
                row[tokenHashColumn] = refreshToken.tokenHash
                row[clientIdColumn] = refreshToken.clientId
                row[subjectColumn] = refreshToken.subject
                row[scopeColumn] = refreshToken.scope
                row[authTimeColumn] = refreshToken.authTime
                row[expiresAtColumn] = refreshToken.expiresAt
                row[revokedAtColumn] = refreshToken.revokedAt
                row[replacedByIdColumn] = refreshToken.replacedById
                row[nonceColumn] = refreshToken.nonce
            }
        }
    }

    override fun findRefreshTokenByTokenHash(tokenHash: String): AuthRefreshToken? {
        return dbConnectionFactory.withExposed {
            RefreshTokenTable.selectAll()
                .where { RefreshTokenTable.tokenHashColumn eq tokenHash }
                .singleOrNull()
                ?.let { readRefreshToken(it) }
        }
    }

    override fun revokeRefreshToken(id: AuthRefreshTokenId, revokedAt: Instant, replacedById: AuthRefreshTokenId) {
        dbConnectionFactory.withExposed {
            RefreshTokenTable.update(where = { RefreshTokenTable.idColumn eq id }) { row ->
                row[revokedAtColumn] = revokedAt
                row[replacedByIdColumn] = replacedById
            }
        }
    }

    override fun purgeExpired(now: Instant) {
        dbConnectionFactory.withExposed {
            AuthCtxTable.deleteWhere { expiresAtColumn less now }
            AuthCodeTable.deleteWhere { expiresAtColumn less now }
        }
        dbConnectionFactory.withExposed {
            RefreshTokenTable.deleteWhere { expiresAtColumn less now }
        }
    }

    private fun readAuthCtx(row: ResultRow): OidcAuthorizeCtx {
        return OidcAuthorizeCtx(
            authCtxCode = row[AuthCtxTable.authorizeCtxCodeColumn],
            clientId = row[AuthCtxTable.clientIdColumn],
            redirectUri = row[AuthCtxTable.redirectUriColumn],
            scope = row[AuthCtxTable.scopeColumn],
            state = row[AuthCtxTable.stateColumn],
            codeChallenge = row[AuthCtxTable.codeChallengeColumn],
            codeChallengeMethod = row[AuthCtxTable.codeChallengeMethodColumn],
            nonce = row[AuthCtxTable.nonceColumn],
            createdAt = row[AuthCtxTable.createdAtColumn],
            expiresAt = row[AuthCtxTable.expiresAtColumn]
        )
    }

    private fun readAuthCode(row: ResultRow): OidcAuthorizeCode {
        return OidcAuthorizeCode(
            code = row[AuthCodeTable.codeColumn],
            clientId = row[AuthCodeTable.clientIdColumn],
            redirectUri = row[AuthCodeTable.redirectUriColumn],
            subject = row[AuthCodeTable.subjectColumn],
            scope = row[AuthCodeTable.scopeColumn],
            codeChallenge = row[AuthCodeTable.codeChallengeColumn],
            codeChallengeMethod = row[AuthCodeTable.codeChallengeMethodColumn],
            nonce = row[AuthCodeTable.nonceColumn],
            authTime = row[AuthCodeTable.authTimeColumn],
            expiresAt = row[AuthCodeTable.expiresAtColumn]
        )
    }

    private fun readRefreshToken(row: ResultRow): AuthRefreshToken {
        return AuthRefreshToken(
            id = row[RefreshTokenTable.idColumn],
            tokenHash = row[RefreshTokenTable.tokenHashColumn],
            clientId = row[RefreshTokenTable.clientIdColumn],
            subject = row[RefreshTokenTable.subjectColumn],
            scope = row[RefreshTokenTable.scopeColumn],
            authTime = row[RefreshTokenTable.authTimeColumn],
            expiresAt = row[RefreshTokenTable.expiresAtColumn],
            revokedAt = row[RefreshTokenTable.revokedAtColumn],
            replacedById = row[RefreshTokenTable.replacedByIdColumn],
            nonce = row[RefreshTokenTable.nonceColumn]
        )
    }


    companion object {
        private object AuthCtxTable : Table("auth_ctx") {
            val authorizeCtxCodeColumn = text("authorize_ctx_code")
            val clientIdColumn = text("client_id")
            val redirectUriColumn = text("redirect_uri")
            val scopeColumn = text("scope")
            val stateColumn = text("state").nullable()
            val codeChallengeColumn = text("code_challenge")
            val codeChallengeMethodColumn = text("code_challenge_method")
            val nonceColumn = text("nonce").nullable()
            val createdAtColumn = instant("created_at")
            val expiresAtColumn = instant("expires_at")
        }

        private object AuthCodeTable : Table("auth_code") {
            val codeColumn = text("code")
            val clientIdColumn = text("client_id")
            val redirectUriColumn = text("redirect_uri")
            val subjectColumn = text("subject")
            val scopeColumn = text("scope")
            val codeChallengeColumn = text("code_challenge")
            val codeChallengeMethodColumn = text("code_challenge_method")
            val nonceColumn = text("nonce").nullable()
            val authTimeColumn = instant("auth_time")
            val expiresAtColumn = instant("expires_at")
        }

        private object RefreshTokenTable : Table("auth_refresh_token") {
            val idColumn = javaUUID("id").transform(IdTransformer(::AuthRefreshTokenId))
            val tokenHashColumn = text("token_hash")
            val clientIdColumn = text("client_id")
            val subjectColumn = text("subject")
            val scopeColumn = text("scope")
            val authTimeColumn = instant("auth_time")
            val expiresAtColumn = instant("expires_at")
            val revokedAtColumn = instant("revoked_at").nullable()
            val replacedByIdColumn = javaUUID("replaced_by_id")
                .transform(IdTransformer(::AuthRefreshTokenId))
                .nullable()
            val nonceColumn = text("nonce").nullable()
        }

    }

}
