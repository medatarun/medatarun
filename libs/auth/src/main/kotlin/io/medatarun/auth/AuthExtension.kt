package io.medatarun.auth

import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.auth.actions.AuthEmbeddedActionsProvider
import io.medatarun.auth.domain.*
import io.medatarun.auth.infra.ActorStorageSQLite
import io.medatarun.auth.infra.DbConnectionFactoryImpl
import io.medatarun.auth.infra.OidcStorageSQLite
import io.medatarun.auth.infra.UserStorageSQLite
import io.medatarun.auth.internal.*
import io.medatarun.auth.ports.exposed.ActorService
import io.medatarun.auth.ports.exposed.BootstrapSecretLifecycle.Companion.DEFAULT_BOOTSTRAP_SECRET_PATH_NAME
import io.medatarun.auth.ports.exposed.JwtSigninKeyRegistry.Companion.DEFAULT_KEYSTORE_PATH_NAME
import io.medatarun.auth.ports.exposed.OAuthService
import io.medatarun.auth.ports.exposed.OidcService
import io.medatarun.auth.ports.exposed.UserService
import io.medatarun.auth.ports.needs.AuthClock
import io.medatarun.auth.ports.needs.OidcStorage
import io.medatarun.auth.ports.needs.UserServiceEvents
import io.medatarun.kernel.ExtensionId
import io.medatarun.kernel.MedatarunExtension
import io.medatarun.kernel.MedatarunExtensionCtx
import io.medatarun.kernel.MedatarunServiceCtx
import io.medatarun.types.JsonTypeEquiv
import io.medatarun.types.TypeDescriptor
import java.time.Instant
import kotlin.reflect.KClass

class AuthExtension() : MedatarunExtension {
    override val id: ExtensionId = "auth"
    override fun init(ctx: MedatarunExtensionCtx) {
        val actionProvider = AuthEmbeddedActionsProvider()
        ctx.register(ActionProvider::class, actionProvider)
        ctx.register(TypeDescriptor::class, UsernameTypeDescriptor())
        ctx.register(TypeDescriptor::class, FullnameTypeDescriptor())
        ctx.register(TypeDescriptor::class, PasswordClearTypeDescriptor())
    }

    class UsernameTypeDescriptor: TypeDescriptor<Username> {
        override val target: KClass<Username> = Username::class
        override val equivMultiplatorm: String = "Username"
        override val equivJson: JsonTypeEquiv = JsonTypeEquiv.STRING
        override fun validate(value: Username): Username {
            return value.validate()
        }
    }
    class FullnameTypeDescriptor: TypeDescriptor<Fullname> {
        override val target: KClass<Fullname> = Fullname::class
        override val equivMultiplatorm: String = "Fullname"
        override val equivJson: JsonTypeEquiv = JsonTypeEquiv.STRING
        override fun validate(value: Fullname): Fullname {
            return value.validate()
        }
    }

    class PasswordClearTypeDescriptor : TypeDescriptor<PasswordClear> {
        override val target: KClass<PasswordClear> = PasswordClear::class
        override val equivMultiplatorm: String = "PasswordClear"
        override val equivJson: JsonTypeEquiv = JsonTypeEquiv.STRING
        override fun validate(value: PasswordClear): PasswordClear {
            // No validation in entrance because the rules are too specific
            // Business will do it
            return value
        }

    }

    override fun initServices(ctx: MedatarunServiceCtx) {
        val cfgBootstrapSecretPath = ctx.resolveApplicationHomePath(DEFAULT_BOOTSTRAP_SECRET_PATH_NAME)
        val cfgKeyStorePath = ctx.resolveApplicationHomePath(DEFAULT_KEYSTORE_PATH_NAME)
        val dbConnectionFactory = DbConnectionFactoryImpl(
            ctx.resolveApplicationHomePath("data/database.db").toAbsolutePath().toString()
        )
        val authClock = object : AuthClock {
            override fun now(): Instant = Instant.now()
        }
        val passwordEncryptionDefaultIterations = UserPasswordEncrypter.DEFAULT_ITERATIONS
        val cfgBootstrapSecret = ctx.getConfigProperty(ConfigProperties.BootstrapSecret.key)

        // ------------------------------------------
        // Should be the same in test initializations
        // ------------------------------------------

        val userStorage = UserStorageSQLite(dbConnectionFactory)
        val authStorage: OidcStorage = OidcStorageSQLite(dbConnectionFactory)
        val actorStorage = ActorStorageSQLite(dbConnectionFactory)

        val actorClaimsAdapter = ActorClaimsAdapter()
        val authEmbeddedKeyRegistry = JwtSigninKeyRegistryImpl(cfgKeyStorePath)
        val authEmbeddedKeys = authEmbeddedKeyRegistry.loadOrCreateKeys()

        val jwtCfg = JwtConfig(
            issuer = "urn:medatarun:${authEmbeddedKeys.kid}",  // stable tant que tes fichiers sont là
            audience = "medatarun",
            ttlSeconds = 3600
        )

        val bootstrapper = BootstrapSecretLifecycleImpl(cfgBootstrapSecretPath, cfgBootstrapSecret)

        val actorService: ActorService = ActorServiceImpl(actorStorage, authClock)
        val userEvents: UserServiceEvents = UserServiceEventsActorProvisioning(actorService, jwtCfg.issuer)

        val userService: UserService = UserServiceImpl(
            userStorage = userStorage,
            clock = authClock,
            passwordEncryptionIterations = passwordEncryptionDefaultIterations,
            bootstrapper = bootstrapper,
            userEvents = userEvents
        )

        val oauthService = OAuthServiceImpl(
            userService = userService,
            keys = authEmbeddedKeys,
            jwtConfig = jwtCfg,
            actorClaimsAdapter = actorClaimsAdapter,
            actorService = actorService
        )

        val oidcService: OidcService = OidcServiceImpl(
            oidcAuthCodeStorage = authStorage,
            actorClaimsAdapter = actorClaimsAdapter,
            oauthService = oauthService,
            authEmbeddedKeys = authEmbeddedKeys,
            jwtCfg = jwtCfg,
            clock = authClock,
            actorService = actorService,
            authCtxDurationSeconds = DEFAULT_AUTH_CTX_DURATION_SECONDS
        )

        ctx.register(UserService::class, userService)
        ctx.register(OidcService::class, oidcService)
        ctx.register(OAuthService::class, oauthService)
        ctx.register(ActorService::class, actorService)


    }

    companion object {
        const val DEFAULT_AUTH_CTX_DURATION_SECONDS: Long = 60 * 15
    }
}
