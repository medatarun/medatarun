package io.medatarun

import io.medatarun.cli.AppCLIRunner
import io.medatarun.httpserver.AppHttpServer
import io.medatarun.runtime.internal.AppRuntimeBuilder
import io.medatarun.runtime.internal.AppRuntimeConfigFactory
import org.slf4j.LoggerFactory

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main(args: Array<String>) {

    val config = AppRuntimeConfigFactory().create()
    val serverPort: Int = config.getProperty("medatarun.server.port", "8080").toInt()
    val serverHost: String = config.getProperty("medatarun.server.host", "0.0.0.0")

    if (args.isNotEmpty() && args[0] == "serve") {
        val runtime = AppRuntimeBuilder(config).build()
        AppHttpServer(runtime).start(
            host = serverHost,
            port = serverPort,
            wait = true
        )
    } else {
        val logger = LoggerFactory.getLogger("MAIN")
        logger.info("Connecting to $serverHost:$serverPort")
        val cliRunner = AppCLIRunner(args, defaultServerHost = serverHost, defaultServerPort = serverPort)
        cliRunner.handleCLI()
    }

}
