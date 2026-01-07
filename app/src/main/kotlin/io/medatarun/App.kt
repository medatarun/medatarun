package io.medatarun

import io.medatarun.cli.AppCLIRunner
import io.medatarun.cli.AppCLIUtils.configureCliLogging
import io.medatarun.cli.AppCLIUtils.isServerMode
import io.medatarun.httpserver.AppHttpServer
import io.medatarun.runtime.internal.AppRuntimeBuilder
import io.medatarun.runtime.internal.AppRuntimeConfig
import io.medatarun.runtime.internal.AppRuntimeConfigFactory

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main(args: Array<String>) {
    val isServerMode = isServerMode(args)

    if (isServerMode) {
        val c = createConfig(cli = false)
        val runtime = AppRuntimeBuilder(c.config).build()
        AppHttpServer(runtime).start(
            host = c.serverHost,
            port = c.serverPort,
            wait = true
        )
    } else {
        configureCliLogging()
        val c = createConfig(cli = true)
        val authenticationToken = c.config.getProperty("medatarun.auth.token")
        val cliRunner = AppCLIRunner(args, defaultServerHost = c.serverHost, defaultServerPort = c.serverPort, authenticationToken=authenticationToken)
        cliRunner.handleCLI()
    }

}

fun createConfig(cli: Boolean): BasicConfig {
    val config = AppRuntimeConfigFactory(cli).create()
    val serverPort: Int = config.getProperty("medatarun.server.port", "8080").toInt()
    val serverHost: String = config.getProperty("medatarun.server.host", "0.0.0.0")
    return object : BasicConfig {
        override val config: AppRuntimeConfig = config
        override val serverHost: String = serverHost
        override val serverPort: Int = serverPort
    }
}

interface BasicConfig {
    val config: AppRuntimeConfig
    val serverHost: String
    val serverPort: Int
}
