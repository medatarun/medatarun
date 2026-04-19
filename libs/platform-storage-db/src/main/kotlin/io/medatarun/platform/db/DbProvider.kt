package io.medatarun.platform.db

import io.medatarun.platform.kernel.ServiceContributionPoint
import java.sql.Connection

interface DbProvider: ServiceContributionPoint {
    val dialect: DbDialect
    fun getConnection(): Connection

}
