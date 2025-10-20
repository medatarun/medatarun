package io.medatarun.app

import io.medatarun.cli.AppCLIResources
import io.medatarun.cli.AppCLIRunner
import io.medatarun.httpserver.RestApi
import io.medatarun.runtime.internal.AppRuntimeBuilder
import io.medatarun.utils.Printer
import kotlin.io.path.Path

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main(args: Array<String>) {
    val name = "Kotlin"
    //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
    // to see how IntelliJ IDEA suggests fixing it.
    val message = "Hello, " + name + "!"
    val printer = Printer(message)
    printer.printMessage()

    for (i in 1..5) {
        //TIP Press <shortcut actionId="Debug"/> to start debugging your code. We have set one <icon src="AllIcons.Debugger.Db_set_breakpoint"/> breakpoint
        // for you, but you can always add more by pressing <shortcut actionId="ToggleLineBreakpoint"/>.
        println("i = $i")
    }

    println(Path("package.json").toAbsolutePath().toString())

    val runtime = AppRuntimeBuilder().build()
    val cliResources = AppCLIResources(runtime)

    if (args[0] == "serve") {
        val rest = RestApi(runtime).start(wait = true)
    } else {
        val cliRunner = AppCLIRunner(args, cliResources)
        cliRunner.handleCLI()
    }





}
