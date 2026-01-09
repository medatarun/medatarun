package io.medatarun

import io.medatarun.cli.AppCLIRunner
import io.medatarun.cli.AppCLIUtils.configureCliLogging
import io.medatarun.cli.AppCLIUtils.isServerMode
import io.medatarun.httpserver.AppHttpServer
import io.medatarun.runtime.internal.AppRuntimeBuilder
import io.medatarun.runtime.internal.AppRuntimeConfig
import io.medatarun.runtime.internal.AppRuntimeConfigFactory
import org.slf4j.LoggerFactory
import java.net.URI

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
        val authenticationToken = c.config.getProperty("medatarun.auth.token")
        val cliRunner = AppCLIRunner(
            args,
            defaultServerHost = c.serverHost,
            defaultServerPort = c.serverPort,
            authenticationToken = authenticationToken
        )
        cliRunner.handleCLI()
    }

}

fun createConfig(cli: Boolean): BasicConfig {
    val config = AppRuntimeConfigFactory(cli).create()
    val serverPort: Int = config.getProperty("medatarun.server.port", "8080").toInt()
    val serverHost: String = config.getProperty("medatarun.server.host", "0.0.0.0")

    @Suppress("HttpUrlsUsage")
    val publicBaseUrl: String = config.getProperty("medatarun.public.base.url", "http://$serverHost:$serverPort/")
    return object : BasicConfig {
        override val config: AppRuntimeConfig = config
        override val serverHost: String = serverHost
        override val serverPort: Int = serverPort
        override val publicBaseUrl: URI = URI(publicBaseUrl)

    }
}

interface BasicConfig {
    val config: AppRuntimeConfig
    val serverHost: String
    val serverPort: Int
    val publicBaseUrl: URI
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