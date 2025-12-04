package io.medatarun

import io.medatarun.cli.AppCLIRunner
import io.medatarun.httpserver.AppHttpServer
import io.medatarun.runtime.internal.AppRuntimeBuilder

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main(args: Array<String>) {

    val runtime = AppRuntimeBuilder().build()


    if (args.isNotEmpty() && args[0] == "serve") {
        AppHttpServer(runtime).start(wait = true)
    } else {
        val cliRunner = AppCLIRunner(args, runtime)
        cliRunner.handleCLI()
    }

}
