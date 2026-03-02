package io.medatarun.auth.infra

import io.medatarun.auth.domain.user.*
import io.medatarun.auth.ports.needs.UserStorage
import io.medatarun.platform.db.DbConnectionFactory
import org.intellij.lang.annotations.Language
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.time.Instant

class UserStorageSQLite(private val dbConnectionFactory: DbConnectionFactory) : UserStorage {

    fun initSchema() {
        dbConnectionFactory.withConnection { it.createStatement().execute(SCHEMA) }
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
        dbConnectionFactory.withExposed {
            UsersTable.insert { row ->
                row[idColumn] = id.value.toString()
                row[loginColumn] = login.value
                row[fullNameColumn] = fullname.value
                row[passwordHashColumn] = password.value
                row[adminColumn] = admin
                row[bootstrapColumn] = bootstrap
                row[disabledDateColumn] = disabledDate?.let { InstantSql.toSql(it) }
            }
        }
    }

    override fun findByLogin(login: Username): User? =
        dbConnectionFactory.withExposed {
            UsersTable.selectAll()
                .where { UsersTable.loginColumn eq login.value }
                .singleOrNull()
                ?.let { readUser(it) }
        }

    override fun updatePassword(login: Username, newPassword: PasswordHash) {
        dbConnectionFactory.withExposed {
            UsersTable.update(where = { UsersTable.loginColumn eq login.value }) { row ->
                row[passwordHashColumn] = newPassword.value
            }
        }
    }

    override fun disable(login: Username, at: Instant) {
        dbConnectionFactory.withExposed {
            UsersTable.update(where = { UsersTable.loginColumn eq login.value }) { row ->
                row[disabledDateColumn] = InstantSql.toSql(at)
            }
        }
    }

    override fun enable(login: Username) {
        dbConnectionFactory.withExposed {
            UsersTable.update(where = { UsersTable.loginColumn eq login.value }) { row ->
                row[disabledDateColumn] = null
            }
        }
    }

    override fun updateFullname(username: Username, fullname: Fullname) {
        dbConnectionFactory.withExposed {
            UsersTable.update(where = { UsersTable.loginColumn eq username.value }) { row ->
                row[fullNameColumn] = fullname.value
            }
        }
    }

    private fun readUser(row: ResultRow): User {
        return User(
            id = UserId.fromString(row[UsersTable.idColumn]),
            username = Username(row[UsersTable.loginColumn]),
            fullname = Fullname(row[UsersTable.fullNameColumn]),
            passwordHash = PasswordHash(row[UsersTable.passwordHashColumn]),
            admin = row[UsersTable.adminColumn],
            bootstrap = row[UsersTable.bootstrapColumn],
            disabledDate = row[UsersTable.disabledDateColumn]?.let { Instant.parse(it) }
        )
    }

    companion object {
        private object UsersTable : Table("users") {
            val idColumn = text("id")
            val loginColumn = text("login")
            val fullNameColumn = text("full_name")
            val passwordHashColumn = text("password_hash")
            val adminColumn = bool("admin")
            val bootstrapColumn = bool("bootstrap")
            val disabledDateColumn = text("disabled_date").nullable()
        }

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
