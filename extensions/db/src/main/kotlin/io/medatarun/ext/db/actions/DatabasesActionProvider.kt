package io.medatarun.ext.db.actions

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.ext.db.internal.connection.DbConnectionRegistry
import io.medatarun.ext.db.internal.drivers.DbDriverManager
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlin.reflect.KClass

class DatabasesActionProvider(
    private val dbDriverManager: DbDriverManager,
    private val dbConnectionRegistry: DbConnectionRegistry
) : ActionProvider<DatabasesAction> {
    override val actionGroupKey: String = "databases"


    override fun findCommandClass(): KClass<DatabasesAction>? {
        return DatabasesAction::class
    }

    override fun dispatch(
        cmd: DatabasesAction,
        actionCtx: ActionCtx
    ): Any {
        return when (cmd) {
            is DatabasesAction.DatabaseDrivers -> {
                buildJsonObject {
                    putJsonArray("drivers") {
                        dbDriverManager.driverRegistry.listDrivers().forEach {
                            addJsonObject {
                                put("id", it.id)
                                put("name", it.name)
                                put("location", it.jarPath.fileName.toString())
                                put("className", it.className)
                            }
                        }
                    }
                }

            }

            is DatabasesAction.Datasources -> {
                buildJsonObject {
                    putJsonArray("datasources") {
                        dbConnectionRegistry.listConnections().forEach { connection ->
                            addJsonObject {
                                put("id", connection.name)
                                put("driver", connection.driver)
                                put("url", connection.url)
                                put("username", connection.username)
                                put("properties", buildJsonObject {
                                    connection.properties.entries.forEach {
                                        put(it.key, it.value)
                                    }
                                })
                            }
                        }
                    }
                }

            }
        }
    }
}