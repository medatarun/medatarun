package io.medatarun

import io.medatarun.cli.AppCLIRunner
import io.medatarun.httpserver.AppHttpServer
import io.medatarun.runtime.internal.AppRuntimeBuilder

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main(args: Array<String>) {




    val serverPort: Int = 8080
    val serverHost: String = "0.0.0.0"

    if (args.isNotEmpty() && args[0] == "serve") {
        val runtime = AppRuntimeBuilder().build()
        AppHttpServer(runtime).start(
            host = serverHost,
            port = serverPort,
            wait = true
        )
    } else {
        val cliRunner = AppCLIRunner(args, defaultServerHost = serverHost, defaultServerPort = serverPort)
        cliRunner.handleCLI()
    }

}
