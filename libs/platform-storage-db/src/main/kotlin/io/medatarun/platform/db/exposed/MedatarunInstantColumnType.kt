package io.medatarun.platform.db.exposed

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ColumnType
import org.jetbrains.exposed.v1.core.IDateColumnType
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.statements.api.RowApi
import org.jetbrains.exposed.v1.core.vendors.PostgreSQLDialect
import org.jetbrains.exposed.v1.core.vendors.SQLiteDialect
import org.jetbrains.exposed.v1.core.vendors.currentDialect
import java.sql.Timestamp
import java.time.Instant
import java.time.ZoneId
import kotlin.time.ExperimentalTime
import kotlin.time.Instant as KotlinInstant
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

private val SQLITE_TIMESTAMP_FORMAT: DateTimeFormat<LocalDateTime> by lazy { createMedatarunLocalDateTimeFormatter(3) }
private val DEFAULT_TIMESTAMP_FORMAT: DateTimeFormat<LocalDateTime> = LocalDateTime.Formats.ISO

/**
 * Stores an [Instant] while keeping the current Exposed behavior:
 * PostgreSQL stays unchanged, SQLite is serialized to text as `YYYY-MM-DD HH:mm:ss.SSS`.
 *
 * This is intentionally a local copy so Medatarun can later switch SQLite storage
 * strategy without affecting PostgreSQL handling.
 */
class MedatarunInstantColumnType : ColumnType<Instant>(), IDateColumnType {
    override val hasTimePart: Boolean = true

    override fun sqlType(): String = currentDialect.dataTypeProvider.timestampType()

    override fun nonNullValueToString(value: Instant): String {
        val localDateTime = toKotlinInstant(value).toLocalDateTime(TimeZone.currentSystemDefault())
        return when (currentDialect) {
            is SQLiteDialect -> "'${SQLITE_TIMESTAMP_FORMAT.format(localDateTime)}'"
            else -> "'${DEFAULT_TIMESTAMP_FORMAT.format(localDateTime)}'"
        }
    }

    @Suppress("MagicNumber")
    private fun instantValueFromDB(value: Any): Instant = when (value) {
        is Timestamp -> KotlinInstant.fromEpochSeconds(value.time / 1000, value.nanos).toJavaInstant()
        is String -> parseInstantFromString(value)
        is java.time.LocalDateTime -> {
            value.atZone(ZoneId.systemDefault())
                .toInstant()
        }
        else -> instantValueFromDB(value.toString())
    }

    private fun parseInstantFromString(value: String): Instant {
        return java.time.Instant.parse(value)
    }

    override fun valueFromDB(value: Any): Instant {
        return instantValueFromDB(value)
    }

    override fun readObject(rs: RowApi, index: Int): Any? {
        return rs.getObject(index, Timestamp::class.java, this)
    }

    override fun notNullValueToDB(value: Instant): Any {
        val localDateTime = toKotlinInstant(value).toLocalDateTime(TimeZone.currentSystemDefault())
        return when (currentDialect) {
            is SQLiteDialect -> SQLITE_TIMESTAMP_FORMAT.format(localDateTime)
            else -> localDateTime.toMedatarunSqlTimestamp()
        }
    }

    override fun nonNullValueAsDefaultString(value: Instant): String {
        val localDateTime = toKotlinInstant(value).toLocalDateTime(TimeZone.currentSystemDefault())
        return when (currentDialect) {
            is PostgreSQLDialect -> {
                val formatted = SQLITE_TIMESTAMP_FORMAT.format(localDateTime)
                "'${formatted.trimEnd('0').trimEnd('.')}'::timestamp without time zone"
            }
            else -> super.nonNullValueAsDefaultString(value)
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun toKotlinInstant(value: Instant): KotlinInstant {
        return value.toKotlinInstant()
    }
}

/**
 * Registers a timestamp column backed by [MedatarunInstantColumnType].
 */
fun Table.instant(name: String): Column<Instant> = registerColumn(name, MedatarunInstantColumnType())

private fun createMedatarunLocalDateTimeFormatter(fraction: Int): DateTimeFormat<LocalDateTime> {
    return LocalDateTime.Format {
        date(LocalDate.Formats.ISO)
        char(' ')
        hour()
        char(':')
        minute()
        char(':')
        second()
        if (fraction in 1..9) {
            char('.')
            secondFraction(fraction)
        }
    }
}

private fun LocalDateTime.toMedatarunSqlTimestamp(
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): Timestamp {
    val instant = toInstant(timeZone)
    return Timestamp(instant.toEpochMilliseconds())
        .apply { this.nanos = instant.nanosecondsOfSecond }
}
