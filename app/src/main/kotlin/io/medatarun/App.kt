package io.medatarun

import io.medatarun.actions.runtime.AppHttpServerServices
import io.medatarun.cli.AppCLIConfigProperty
import io.medatarun.cli.AppCLIRunner
import io.medatarun.cli.AppCLIUtils.configureCliLogging
import io.medatarun.cli.AppCLIUtils.isServerMode
import io.medatarun.httpserver.AppHttpServer
import io.medatarun.lang.strings.trimToNull
import io.medatarun.runtime.internal.AppRuntimeBuilder
import io.medatarun.runtime.internal.AppRuntimeConfigFactory
import org.slf4j.LoggerFactory

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main(args: Array<String>) {
    val isServerMode = isServerMode(args)

    if (isServerMode) {
        banner()
        val c = AppRuntimeConfigFactory(cli = false).create()
        val runtime = AppRuntimeBuilder(c).build()
        val server = AppHttpServer(c.publicBaseURL, AppHttpServerServices(runtime))
        Runtime.getRuntime().addShutdownHook(Thread { server.stop() })
        server.start(
            host = c.serverHost,
            port = c.serverPort,
            wait = true
        )

    } else {
        configureCliLogging()
        val c = AppRuntimeConfigFactory(cli = true).create()
        val authenticationToken = c.getProperty(AppCLIConfigProperty.AuthToken.key)?.trimToNull()
        val cliRunner = AppCLIRunner(
            args,
            publicBaseUrl = c.publicBaseURL,
            authenticationToken = authenticationToken
        )
        cliRunner.handleCLI()
    }

}



fun banner() {
    val logger = LoggerFactory.getLogger("SERVER")
    val banner = """
░█▄█░█▀▀░█▀▄░█▀█░▀█▀░█▀█░█▀▄░█░█░█▀█
░█░█░█▀▀░█░█░█▀█░░█░░█▀█░█▀▄░█░█░█░█
░▀░▀░▀▀▀░▀▀░░▀░▀░░▀░░▀░▀░▀░▀░▀▀▀░▀░▀    
pid=${ProcessHandle.current().pid()}
    """.trimIndent()
    for (string in banner.lines()) {
        logger.info(string)
    }

}