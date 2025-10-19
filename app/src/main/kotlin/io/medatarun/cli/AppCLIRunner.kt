package io.medatarun.app.io.medatarun.cli

import io.medatarun.app.io.medatarun.runtime.getLogger
import kotlin.collections.contains
import kotlin.collections.get
import kotlin.reflect.KParameter
import kotlin.reflect.full.functions
import kotlin.reflect.full.memberProperties


class AppCLIRunner(private val args: Array<String>, private val sys: AppCLIResources) {

    companion object {
        val logger = getLogger(AppCLIRunner::class)
    }

    init {
        logger.debug("Called with arguments : ${args.joinToString(" ")}")
    }

    fun handleCLI() {
        if (args.size < 2) {
            logger.error("Usage : app <resource> <function> [--param valeur]")
            return
        }

        val resourceName = args[0]
        val functionName = args[1]
        val params = mutableMapOf<String, String>()
        var i = 2
        while (i < args.size) {
            val arg = args[i]
            if (arg.startsWith("--")) {
                val split = arg.removePrefix("--").split("=", limit = 2)
                val key = split[0]
                val value = if (split.size == 2) split[1]
                else args.getOrNull(i + 1)?.takeIf { !it.startsWith("--") }
                if (value != null) {
                    params[key] = value
                    if (split.size == 1) i++ // saute la valeur séparée
                }
            }
            i++
        }

        val resourceProperty = AppCLIResources::class.memberProperties
            .find { it.name == resourceName } ?: run {
            logger.error("Ressource inconnue : $resourceName")
            return
        }

        val resourceInstance = resourceProperty.getter.call(sys)
        val fn = resourceInstance!!::class.functions.find { it.name == functionName } ?: run {
            logger.error("Fonction inconnue : $functionName")
            return
        }

        // Vérification des paramètres requis
        val missing = fn.parameters
            .filter { it.kind == KParameter.Kind.VALUE && !it.isOptional && it.name !in params.keys }
            .mapNotNull { it.name }

        if (missing.isNotEmpty()) {
            logger.error("Erreur : paramètre${if (missing.size > 1) "s" else ""} manquant${if (missing.size > 1) "s" else ""} : ${missing.joinToString(", ")}")
            logger.error("Usage attendu : ${resourceName} ${functionName} " +
                    fn.parameters.filter { it.kind == KParameter.Kind.VALUE }
                        .joinToString(" ") { "--${it.name}=<${it.type.toString().substringAfterLast('.') }>" })
            return
        }

        val callArgs = mutableMapOf<KParameter, Any?>()
        for (param in fn.parameters) {
            when (param.kind) {
                KParameter.Kind.INSTANCE -> callArgs[param] = resourceInstance
                KParameter.Kind.VALUE -> {
                    val raw = params[param.name]
                    val type = param.type.classifier
                    val converted = when (type) {
                        Int::class -> raw?.toInt()
                        Boolean::class -> raw?.toBoolean()
                        else -> raw
                    }
                    if (converted != null) callArgs[param] = converted
                }
                else -> {}
            }
        }

        fn.callBy(callArgs)
    }
}