package io.medatarun.auth.infra

import io.medatarun.auth.domain.user.*
import io.medatarun.auth.ports.needs.UserStorage
import org.intellij.lang.annotations.Language
import java.time.Instant

class UserStorageSQLite(private val dbConnectionFactory: DbConnectionFactory) : UserStorage {

    init {
        dbConnectionFactory.getConnection().use { it.createStatement().execute(SCHEMA) }
    }


    override fun insert(
        id: UserId,
        login: Username,
        fullname: Fullname,
        password: PasswordHash,
        admin: Boolean,
        bootstrap: Boolean,
        disabledDate: Instant?
    ) {


        dbConnectionFactory.getConnection().use { c ->
            c.prepareStatement(
                """
                INSERT INTO users(id, login, full_name, password_hash, admin, bootstrap, disabled_date)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """
            ).use { ps ->
                ps.setString(1, id.value.toString())
                ps.setString(2, login.value)
                ps.setString(3, fullname.value)
                ps.setString(4, password.value)
                ps.setInt(5, if (admin) 1 else 0)
                ps.setInt(6, if (bootstrap) 1 else 0)
                ps.setString(7, disabledDate?.let { InstantSql.toSql(it) })
                ps.executeUpdate()
            }
        }
    }

    override fun findByLogin(login: Username): User? =
        dbConnectionFactory.getConnection().use { c ->
            c.prepareStatement(
                "SELECT id, login, full_name, password_hash, admin, bootstrap, disabled_date FROM users WHERE login = ?"
            ).use { ps ->
                ps.setString(1, login.value)
                val rs = ps.executeQuery()
                if (!rs.next()) return null

                User(
                    id = UserId.fromString(rs.getString("id")),
                    username = Username(rs.getString("login")),
                    fullname = Fullname(rs.getString("full_name")),
                    passwordHash = PasswordHash(rs.getString("password_hash")),
                    admin = rs.getInt("admin") == 1,
                    bootstrap = rs.getInt("bootstrap") == 1,
                    disabledDate = InstantSql.fromSqlOptional(rs, "disabled_date")
                )
            }
        }

    override fun updatePassword(login: Username, newPassword: PasswordHash) {


        dbConnectionFactory.getConnection().use { c ->
            c.prepareStatement(
                "UPDATE users SET password_hash = ? WHERE login = ?"
            ).use { ps ->
                ps.setString(1, newPassword.value)
                ps.setString(2, login.value)
                ps.executeUpdate()
            }
        }
    }

    override fun disable(login: Username, at: Instant) {
        dbConnectionFactory.getConnection().use { c ->
            c.prepareStatement(
                "UPDATE users SET disabled_date = ? WHERE login = ?"
            ).use { ps ->
                ps.setString(1, InstantSql.toSql(at))
                ps.setString(2, login.value)
                ps.executeUpdate()
            }
        }
    }

    override fun enable(login: Username) {
        dbConnectionFactory.getConnection().use { c ->
            c.prepareStatement(
                "UPDATE users SET disabled_date = NULL WHERE login = ?"
            ).use { ps ->
                ps.setString(1, login.value)
                ps.executeUpdate()
            }
        }
    }

    override fun updateFullname(username: Username, fullname: Fullname) {
        dbConnectionFactory.getConnection().use { c ->
            c.prepareStatement("UPDATE users SET full_name = ? WHERE login = ?").use { ps ->
                ps.setString(1, fullname.value)
                ps.setString(2, username.value)
                ps.executeUpdate()
            }
        }
    }

    companion object {
        @Language("SQLite")
        private const val SCHEMA = """
CREATE TABLE IF NOT EXISTS users (
  id TEXT PRIMARY KEY UNIQUE,
  login TEXT NOT NULL UNIQUE,
  full_name TEXT NOT NULL,
  password_hash TEXT NOT NULL,
  admin INTEGER NOT NULL,
  bootstrap INTEGER NOT NULL,
  disabled_date TEXT
);
"""

    }

}
