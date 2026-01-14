package io.medatarun

import io.medatarun.cli.AppCLIConfigProperty
import io.medatarun.cli.AppCLIRunner
import io.medatarun.cli.AppCLIUtils.configureCliLogging
import io.medatarun.cli.AppCLIUtils.isServerMode
import io.medatarun.config.createConfig
import io.medatarun.httpserver.AppHttpServer
import io.medatarun.lang.strings.trimToNull
import io.medatarun.runtime.internal.AppRuntimeBuilder
import org.slf4j.LoggerFactory

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main(args: Array<String>) {
    val isServerMode = isServerMode(args)

    if (isServerMode) {
        banner()
        val c = createConfig(cli = false)
        val runtime = AppRuntimeBuilder(c.config).build()
        val server = AppHttpServer(runtime, c.publicBaseUrl)
        Runtime.getRuntime().addShutdownHook(Thread { server.stop() })
        server.start(
            host = c.serverHost,
            port = c.serverPort,
            wait = true
        )

    } else {
        configureCliLogging()
        val c = createConfig(cli = true)
        val authenticationToken = c.config.getProperty(AppCLIConfigProperty.AuthToken.key)?.trimToNull()
        val cliRunner = AppCLIRunner(
            args,
            publicBaseUrl = c.publicBaseUrl,
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