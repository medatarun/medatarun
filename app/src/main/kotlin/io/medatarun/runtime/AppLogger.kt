package io.medatarun.app.io.medatarun.runtime

import kotlin.reflect.KClass

interface AppLogger {
    fun cli(msg: String)
    fun debug(msg: String)
    fun info(msg: String)
    fun warn(msg: String)
    fun error(msg: String, throwable: Throwable? = null)
}

object LoggerConfig {
    var debug = false
    var info = true
    var warn = true
    var error = true
}

object Ansi {
    const val RESET = "\u001B[0m"
    const val RED = "\u001B[31m"
    const val YELLOW = "\u001B[33m"
    const val BLUE = "\u001B[34m"
    const val GREEN = "\u001B[32m"
}

fun getLogger(clazz: KClass<*>): AppLogger {
    return getLogger(clazz.simpleName?:"")
}
fun getLogger(name: String): AppLogger {
    return object : AppLogger {

        fun isDebugEnabled() = LoggerConfig.debug

        override fun cli(msg: String) {
            println(msg)
        }

        override fun debug(msg: String) {
            if (LoggerConfig.debug) output("DEBUG", msg, Ansi.BLUE)
        }

        override fun info(msg: String) {
            if (LoggerConfig.info) output("INFO ", msg, null)
        }

        override fun warn(msg: String) {
            if (LoggerConfig.warn) output("WARN ", msg, Ansi.YELLOW)
        }

        override fun error(msg: String, throwable: Throwable?) {
            if (LoggerConfig.error) output("ERROR", msg, Ansi.RED)
        }

        private fun output(level: String, message: String, color: String?) {
            val format = "[$level] [$name] $message"
            if (color == null) {
                println(format)
            } else {
                println("${color}${format}${Ansi.RESET}")
            }

        }
    }
}
