package io.medatarun.auth.infra.db

import io.medatarun.auth.domain.user.*
import io.medatarun.auth.ports.needs.UserStorage
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.platform.db.exposed.instant
import org.jetbrains.exposed.v1.core.ColumnTransformer
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.time.Instant
import java.util.UUID

class UserStorageSQLite(private val dbConnectionFactory: DbConnectionFactory) : UserStorage {

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
                row[idColumn] = id
                row[loginColumn] = login.value
                row[fullNameColumn] = fullname.value
                row[passwordHashColumn] = password.value
                row[adminColumn] = admin
                row[bootstrapColumn] = bootstrap
                row[disabledDateColumn] = disabledDate
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
                row[disabledDateColumn] = at
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

    override fun findAll(): List<User> {
        return dbConnectionFactory.withExposed {
            UsersTable.selectAll().orderBy(UsersTable.fullNameColumn to SortOrder.ASC).map { readUser(it) }
        }
    }

    private fun readUser(row: ResultRow): User {
        return User(
            id = row[UsersTable.idColumn],
            username = Username(row[UsersTable.loginColumn]),
            fullname = Fullname(row[UsersTable.fullNameColumn]),
            passwordHash = PasswordHash(row[UsersTable.passwordHashColumn]),
            admin = row[UsersTable.adminColumn],
            bootstrap = row[UsersTable.bootstrapColumn],
            disabledDate = row[UsersTable.disabledDateColumn]
        )
    }

    companion object {
        private object UsersTable : Table("users") {
            val idColumn = javaUUID("id").transform(UserIdColumnTransformer())
            val loginColumn = text("login")
            val fullNameColumn = text("full_name")
            val passwordHashColumn = text("password_hash")
            val adminColumn = bool("admin")
            val bootstrapColumn = bool("bootstrap")
            val disabledDateColumn = instant("disabled_date").nullable()
        }

        private class UserIdColumnTransformer : ColumnTransformer<UUID, UserId> {
            override fun unwrap(value: UserId): UUID {
                return value.value
            }

            override fun wrap(value: UUID): UserId {
                return UserId(value)
            }
        }

    }

}
