package io.medatarun

import io.medatarun.cli.AppCLIRunner
import io.medatarun.httpserver.AppHttpServer
import io.medatarun.runtime.internal.AppRuntimeBuilder

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main(args: Array<String>) {

    val runtime = AppRuntimeBuilder().build()


    val serverPort: Int = 8080
    val serverHost: String = "0.0.0.0"

    if (args.isNotEmpty() && args[0] == "serve") {
        AppHttpServer(runtime).start(
            host = serverHost,
            port = serverPort,
            wait = true
        )
    } else {
        val cliRunner = AppCLIRunner(args, runtime, defaultServerHost = serverHost, defaultServerPort = serverPort)
        cliRunner.handleCLI()
    }

}
