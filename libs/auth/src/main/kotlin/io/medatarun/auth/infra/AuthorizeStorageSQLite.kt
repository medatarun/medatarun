package io.medatarun.auth.infra

import io.medatarun.auth.domain.AuthCode
import io.medatarun.auth.domain.AuthCtx
import io.medatarun.auth.ports.needs.AuthorizeStorage
import io.medatarun.auth.ports.needs.DbConnectionFactory
import org.intellij.lang.annotations.Language
import java.time.Instant

class AuthorizeStorageSQLite(private val dbConnectionFactory: DbConnectionFactory) : AuthorizeStorage {
    override fun saveAuthCtx(authCtx: AuthCtx) {
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
                ps.setString(1, authCtx.authCtxCode)
                ps.setString(2, authCtx.clientId)
                ps.setString(3, authCtx.redirectUri)
                ps.setString(4, authCtx.scope)
                ps.setString(5, authCtx.state)
                ps.setString(6, authCtx.codeChallenge)
                ps.setString(7, authCtx.codeChallengeMethod)
                ps.setString(8, authCtx.nonce)
                ps.setString(9, authCtx.createdAt.toString())
                ps.setString(10, authCtx.expiresAt.toString())
                ps.executeUpdate()
            }
        }
    }

    override fun findAuthCtx(authCtxId: String): AuthCtx {
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

                    return AuthCtx(
                        authCtxCode = rs.getString("authorize_ctx_code"),
                        clientId = rs.getString("client_id"),
                        redirectUri = rs.getString("redirect_uri"),
                        scope = rs.getString("scope"),
                        state = rs.getString("state"),
                        codeChallenge = rs.getString("code_challenge"),
                        codeChallengeMethod = rs.getString("code_challenge_method"),
                        nonce = rs.getString("nonce"),
                        createdAt = Instant.parse(rs.getString("created_at")),
                        expiresAt = Instant.parse(rs.getString("expires_at"))
                    )
                }
            }
        }
    }

    override fun saveAuthCode(authCode: AuthCode) {
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
                ps.setString(1, authCode.code)
                ps.setString(2, authCode.clientId)
                ps.setString(3, authCode.redirectUri)
                ps.setString(4, authCode.subject)
                ps.setString(5, authCode.scope)
                ps.setString(6, authCode.codeChallenge)
                ps.setString(7, authCode.codeChallengeMethod)
                ps.setString(8, authCode.nonce)
                ps.setString(9, authCode.authTime.toString())
                ps.setString(10, authCode.expiresAt.toString())
                ps.executeUpdate()
            }
        }
    }

    override fun findAuthCode(authCode: String): AuthCode? {
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

                    return AuthCode(
                        code = rs.getString("code"),
                        clientId = rs.getString("client_id"),
                        redirectUri = rs.getString("redirect_uri"),
                        subject = rs.getString("subject"),
                        scope = rs.getString("scope"),
                        codeChallenge = rs.getString("code_challenge"),
                        codeChallengeMethod = rs.getString("code_challenge_method"),
                        nonce = rs.getString("nonce"),
                        authTime = Instant.parse(rs.getString("auth_time")),
                        expiresAt = Instant.parse(rs.getString("expires_at"))
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
                    ps.setString(1, now.toString())
                    ps.executeUpdate()
                }

                conn.prepareStatement(purgeAuthCode).use { ps ->
                    ps.setString(1, now.toString())
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
        dbConnectionFactory.getConnection().use { it.createStatement().execute(SCHEMA) }
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
