package io.medatarun.auth.infra

import io.medatarun.auth.domain.oidc.OidcAuthorizeCode
import io.medatarun.auth.domain.oidc.OidcAuthorizeCtx
import io.medatarun.auth.ports.needs.OidcStorage
import org.intellij.lang.annotations.Language
import java.time.Instant

class OidcStorageSQLite(private val dbConnectionFactory: DbConnectionFactory) : OidcStorage {
    override fun saveAuthCtx(oidcAuthorizeCtx: OidcAuthorizeCtx) {
        val sql = """
            INSERT INTO auth_ctx (
                authorize_ctx_code,
                client_id,
                redirect_uri,
                scope,
                state,
                code_challenge,
                code_challenge_method,
                nonce,
                created_at,
                expires_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        dbConnectionFactory.getConnection().use { conn ->
            conn.prepareStatement(sql).use { ps ->
                ps.setString(1, oidcAuthorizeCtx.authCtxCode)
                ps.setString(2, oidcAuthorizeCtx.clientId)
                ps.setString(3, oidcAuthorizeCtx.redirectUri)
                ps.setString(4, oidcAuthorizeCtx.scope)
                ps.setString(5, oidcAuthorizeCtx.state)
                ps.setString(6, oidcAuthorizeCtx.codeChallenge)
                ps.setString(7, oidcAuthorizeCtx.codeChallengeMethod)
                ps.setString(8, oidcAuthorizeCtx.nonce)
                ps.setString(9, InstantSql.toSql(oidcAuthorizeCtx.createdAt))
                ps.setString(10, InstantSql.toSql(oidcAuthorizeCtx.expiresAt))
                ps.executeUpdate()
            }
        }
    }

    override fun findAuthCtx(authCtxId: String): OidcAuthorizeCtx {
        val sql = """
            SELECT *
            FROM auth_ctx
            WHERE authorize_ctx_code = ?
        """.trimIndent()

        dbConnectionFactory.getConnection().use { conn ->
            conn.prepareStatement(sql).use { ps ->
                ps.setString(1, authCtxId)
                ps.executeQuery().use { rs ->
                    if (!rs.next()) {
                        throw NoSuchElementException("auth_ctx not found: $authCtxId")
                    }

                    return OidcAuthorizeCtx(
                        authCtxCode = rs.getString("authorize_ctx_code"),
                        clientId = rs.getString("client_id"),
                        redirectUri = rs.getString("redirect_uri"),
                        scope = rs.getString("scope"),
                        state = rs.getString("state"),
                        codeChallenge = rs.getString("code_challenge"),
                        codeChallengeMethod = rs.getString("code_challenge_method"),
                        nonce = rs.getString("nonce"),
                        createdAt = InstantSql.fromSqlRequired(rs, "created_at"),
                        expiresAt = InstantSql.fromSqlRequired(rs, "expires_at")
                    )
                }
            }
        }
    }

    override fun saveAuthCode(oidcAuthorizeCode: OidcAuthorizeCode) {
        val sql = """
            INSERT INTO auth_code (
                code,
                client_id,
                redirect_uri,
                subject,
                scope,
                code_challenge,
                code_challenge_method,
                nonce,
                auth_time,
                expires_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        dbConnectionFactory.getConnection().use { conn ->
            conn.prepareStatement(sql).use { ps ->
                ps.setString(1, oidcAuthorizeCode.code)
                ps.setString(2, oidcAuthorizeCode.clientId)
                ps.setString(3, oidcAuthorizeCode.redirectUri)
                ps.setString(4, oidcAuthorizeCode.subject)
                ps.setString(5, oidcAuthorizeCode.scope)
                ps.setString(6, oidcAuthorizeCode.codeChallenge)
                ps.setString(7, oidcAuthorizeCode.codeChallengeMethod)
                ps.setString(8, oidcAuthorizeCode.nonce)
                ps.setString(9, InstantSql.toSql(oidcAuthorizeCode.authTime))
                ps.setString(10, InstantSql.toSql(oidcAuthorizeCode.expiresAt))
                ps.executeUpdate()
            }
        }
    }

    override fun findAuthCode(authCode: String): OidcAuthorizeCode? {
        val sql = """
            SELECT *
            FROM auth_code
            WHERE code = ?
        """.trimIndent()

        dbConnectionFactory.getConnection().use { conn ->
            conn.prepareStatement(sql).use { ps ->
                ps.setString(1, authCode)
                ps.executeQuery().use { rs ->
                    if (!rs.next()) return null

                    return OidcAuthorizeCode(
                        code = rs.getString("code"),
                        clientId = rs.getString("client_id"),
                        redirectUri = rs.getString("redirect_uri"),
                        subject = rs.getString("subject"),
                        scope = rs.getString("scope"),
                        codeChallenge = rs.getString("code_challenge"),
                        codeChallengeMethod = rs.getString("code_challenge_method"),
                        nonce = rs.getString("nonce"),
                        authTime = InstantSql.fromSqlRequired(rs, "auth_time"),
                        expiresAt = InstantSql.fromSqlRequired(rs, "expires_at")
                    )
                }
            }
        }
    }

    override fun deleteAuthCode(authCode: String) {
        val sql = "DELETE FROM auth_code WHERE code = ?"

        dbConnectionFactory.getConnection().use { conn ->
            conn.prepareStatement(sql).use { ps ->
                ps.setString(1, authCode)
                ps.executeUpdate()
            }
        }
    }

    override fun deleteAuthCtx(authorizeCtxCode: String) {
        val sql = "DELETE FROM auth_ctx WHERE authorize_ctx_code = ?"

        dbConnectionFactory.getConnection().use { conn ->
            conn.prepareStatement(sql).use { ps ->
                ps.setString(1, authorizeCtxCode)
                ps.executeUpdate()
            }
        }
    }

    override fun purgeExpired(now: Instant) {
        val purgeAuthCtx = "DELETE FROM auth_ctx WHERE expires_at < ?"
        val purgeAuthCode = "DELETE FROM auth_code WHERE expires_at < ?"

        dbConnectionFactory.getConnection().use { conn ->
            conn.autoCommit = false
            try {
                conn.prepareStatement(purgeAuthCtx).use { ps ->
                    ps.setString(1, InstantSql.toSql(now))
                    ps.executeUpdate()
                }

                conn.prepareStatement(purgeAuthCode).use { ps ->
                    ps.setString(1, InstantSql.toSql(now))
                    ps.executeUpdate()
                }

                conn.commit()
            } catch (e: Exception) {
                conn.rollback()
                throw e
            } finally {
                conn.autoCommit = true
            }
        }
    }

    init {
        dbConnectionFactory.getConnection().use { connection ->
            SCHEMA.split(";")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .forEach { stmt ->
                    connection.createStatement().execute(stmt)
                }
        }
    }


    companion object {
        @Language("SQLite")
        private const val SCHEMA = """
CREATE TABLE IF NOT EXISTS auth_ctx (
    authorize_ctx_code TEXT PRIMARY KEY,
    client_id TEXT NOT NULL,
    redirect_uri TEXT NOT NULL,
    scope TEXT NOT NULL,
    state TEXT,
    code_challenge TEXT NOT NULL,
    code_challenge_method TEXT NOT NULL,
    nonce TEXT,
    created_at TEXT NOT NULL,
    expires_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS auth_code (
    code TEXT PRIMARY KEY,
    client_id TEXT NOT NULL,
    redirect_uri TEXT NOT NULL,
    subject TEXT NOT NULL,
    scope TEXT NOT NULL,
    code_challenge TEXT NOT NULL,
    code_challenge_method TEXT NOT NULL,
    nonce TEXT,
    auth_time TEXT NOT NULL,
    expires_at TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_auth_ctx_expires_at ON auth_ctx(expires_at);
CREATE INDEX IF NOT EXISTS idx_auth_code_expires_at ON auth_code(expires_at);
"""
    }

}
