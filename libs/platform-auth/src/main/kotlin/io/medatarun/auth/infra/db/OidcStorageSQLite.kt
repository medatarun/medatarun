package io.medatarun.auth.infra.db

import io.medatarun.auth.domain.oidc.OidcAuthorizeCode
import io.medatarun.auth.domain.oidc.OidcAuthorizeCtx
import io.medatarun.auth.ports.needs.OidcStorage
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.platform.db.DbSqlResources
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
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
                row[createdAtColumn] = InstantSql.toSql(oidcAuthorizeCtx.createdAt)
                row[expiresAtColumn] = InstantSql.toSql(oidcAuthorizeCtx.expiresAt)
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
                row[authTimeColumn] = InstantSql.toSql(oidcAuthorizeCode.authTime)
                row[expiresAtColumn] = InstantSql.toSql(oidcAuthorizeCode.expiresAt)
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

    override fun purgeExpired(now: Instant) {
        dbConnectionFactory.withExposed {
            val nowSql = InstantSql.toSql(now)
            AuthCtxTable.deleteWhere { expiresAtColumn less nowSql }
            AuthCodeTable.deleteWhere { expiresAtColumn less nowSql }
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
            createdAt = Instant.parse(row[AuthCtxTable.createdAtColumn]),
            expiresAt = Instant.parse(row[AuthCtxTable.expiresAtColumn])
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
            authTime = Instant.parse(row[AuthCodeTable.authTimeColumn]),
            expiresAt = Instant.parse(row[AuthCodeTable.expiresAtColumn])
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
            val createdAtColumn = text("created_at")
            val expiresAtColumn = text("expires_at")
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
            val authTimeColumn = text("auth_time")
            val expiresAtColumn = text("expires_at")
        }

    }

}
