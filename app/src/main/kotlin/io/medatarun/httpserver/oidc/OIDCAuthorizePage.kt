package io.medatarun.httpserver.oidc

import io.medatarun.auth.domain.oidc.OidcAuthorizeCtx
import io.medatarun.auth.domain.user.PasswordClear
import io.medatarun.auth.domain.user.Username
import io.medatarun.auth.ports.exposed.OidcService
import io.medatarun.auth.ports.exposed.UserService
import io.medatarun.lang.strings.trimToNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class OIDCAuthorizePage(
    private val oidcService: OidcService,
    private val userService: UserService,

    ) {

    sealed interface OIDCAuthorizePageResult {
        class HtmlPage(val body: String) : OIDCAuthorizePageResult
        class Redirect(val location: String) : OIDCAuthorizePageResult
        class Fatal(val message: String) : OIDCAuthorizePageResult
    }

    fun process(
        authCtxCode: String?,
        username: String?,
        password: String?,
    ): OIDCAuthorizePageResult {
        if (authCtxCode == null) return OIDCAuthorizePageResult.Fatal("No auth credentials")
        val ctx = oidcService.oidcAuthorizeFindAuthCtx(authCtxCode)
            ?: return OIDCAuthorizePageResult.Fatal("No auth context found")

        var loginError: String? = null
        val userNameSafe = username?.trimToNull()
        val passwordSafe = password // do not trim to null

        if (!userNameSafe.isNullOrBlank() && !passwordSafe.isNullOrBlank()) {
            try {
                val user = userService.loginUser(Username(userNameSafe).validate(), PasswordClear(passwordSafe))
                val redirectUri = oidcService.oidcAuthorizeCreateCode(authCtxCode, user.username.value)
                return OIDCAuthorizePageResult.Redirect(redirectUri)
            } catch (ex: Exception) {
                loginError = ex.message ?: "Login failed"
                return OIDCAuthorizePageResult.HtmlPage(create(ctx, userNameSafe, loginError))
            }
        }
        return OIDCAuthorizePageResult.HtmlPage(create(ctx, userNameSafe, loginError))

    }

    fun create(ctx: OidcAuthorizeCtx, username: String?, error: String?): String {
        val index = javaClass.classLoader.getResource("static/login.html")

        val html = index.readText()
        val json = buildJsonObject {
            put(PARAM_USERNAME, username ?: "")
            put("error", error ?: "")
            put(PARAM_AUTH_CTX, ctx.authCtxCode)
        }
        val replaced = html.replace("<!-- __SERVER_CONFIG__ -->", "<script>window.__MEDATARUN_CONFIG__=${json}</script>")
        return replaced

    }

    companion object {
        const val PARAM_AUTH_CTX = "auth_ctx"
        const val PARAM_USERNAME = "username"
        const val PARAM_PASSWORD = "password"
    }
}