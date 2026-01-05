package io.medatarun.cli

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.filter.ThresholdFilter
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import org.slf4j.LoggerFactory

object AppCLIUtils {

    fun isServerMode(args: Array<String>): Boolean {
        return args.isNotEmpty() && args[0] == "serve"
    }

    fun configureCliLogging() {
        // CLI output is machine-friendly, so keep stdout clean and route only errors to stderr.
        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        context.reset()

        val encoder = PatternLayoutEncoder()
        encoder.context = context
        encoder.pattern = "%date %-5level %logger - %msg%n"
        encoder.start()

        val thresholdFilter = ThresholdFilter()
        thresholdFilter.context = context
        thresholdFilter.setLevel("ERROR")
        thresholdFilter.start()

        val consoleAppender = ConsoleAppender<ILoggingEvent>()
        consoleAppender.context = context
        consoleAppender.name = "STDERR"
        consoleAppender.encoder = encoder
        consoleAppender.target = "System.err"
        consoleAppender.addFilter(thresholdFilter)
        consoleAppender.start()

        val rootLogger = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
        rootLogger.level = Level.ERROR
        rootLogger.addAppender(consoleAppender)
    }

}