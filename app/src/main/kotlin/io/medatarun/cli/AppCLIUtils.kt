package io.medatarun.cli

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.filter.LevelFilter
import ch.qos.logback.classic.filter.ThresholdFilter
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.spi.FilterReply
import org.slf4j.LoggerFactory

object AppCLIUtils {

    fun isServerMode(args: Array<String>): Boolean {
        return args.isNotEmpty() && args[0] == "serve"
    }

    fun configureCliLogging() {
        // CLI output is machine-friendly, so split INFO to stdout and WARN/ERROR to stderr.
        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        context.reset()

        val encoderError = PatternLayoutEncoder()
        encoderError.context = context
        encoderError.pattern = "%date %-5level %logger - %msg%n"
        encoderError.start()

        val encoderCli = PatternLayoutEncoder()
        encoderCli.context = context
        encoderCli.pattern = "%msg%n"
        encoderCli.start()

        val infoFilter = LevelFilter()
        infoFilter.context = context
        infoFilter.setLevel(Level.INFO)
        infoFilter.onMatch = FilterReply.ACCEPT
        infoFilter.onMismatch = FilterReply.DENY
        infoFilter.start()

        val warnThresholdFilter = ThresholdFilter()
        warnThresholdFilter.context = context
        warnThresholdFilter.setLevel("WARN")
        warnThresholdFilter.start()

        val consoleOutAppender = ConsoleAppender<ILoggingEvent>()
        consoleOutAppender.context = context
        consoleOutAppender.name = "STDOUT"
        consoleOutAppender.encoder = encoderCli
        consoleOutAppender.target = "System.out"
        consoleOutAppender.addFilter(infoFilter)
        consoleOutAppender.start()

        val consoleErrAppender = ConsoleAppender<ILoggingEvent>()
        consoleErrAppender.context = context
        consoleErrAppender.name = "STDERR"
        consoleErrAppender.encoder = encoderError
        consoleErrAppender.target = "System.err"
        consoleErrAppender.addFilter(warnThresholdFilter)
        consoleErrAppender.start()

        val rootLogger = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
        rootLogger.level = Level.INFO
        rootLogger.addAppender(consoleOutAppender)
        rootLogger.addAppender(consoleErrAppender)
    }

}
