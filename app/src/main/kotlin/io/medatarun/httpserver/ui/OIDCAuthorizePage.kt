package io.medatarun.httpserver.ui


import io.medatarun.auth.domain.AuthCtx
import io.medatarun.auth.ports.exposed.AuthEmbeddedOIDCService
import io.medatarun.auth.ports.exposed.AuthEmbeddedUserService
import io.medatarun.lang.trimToNull
import kotlinx.html.*
import kotlinx.html.stream.createHTML

class OIDCAuthorizePage(
    private val oidcService: AuthEmbeddedOIDCService,
    private val userService: AuthEmbeddedUserService,

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
                val user = userService.loginUser(userNameSafe, passwordSafe)
                val redirectUri = oidcService.oidcAuthorizeCreateCode(authCtxCode, user.login)
                return OIDCAuthorizePageResult.Redirect(redirectUri)
            } catch (ex: Exception) {
                loginError = ex.message ?: "Login failed"
                return OIDCAuthorizePageResult.HtmlPage(create(ctx, userNameSafe, loginError))
            }
        }
        return OIDCAuthorizePageResult.HtmlPage(create(ctx, userNameSafe, loginError))

    }

    fun create(ctx: AuthCtx, username: String?, error: String?): String {

        return createHTML().html {
            head {
                title { +"Medatarun Login" }
            }
            body {
                h1 { +"Medatarun Login" }
                p { +"Enter your username and password" }
                div {
                    if (error!=null) {
                        div {
                            style = "color:red;"
                            +error
                        }
                    }
                    form(method = FormMethod.post) {
                        input(type = InputType.hidden, name = PARAM_AUTH_CTX) { value = ctx.authCtxCode }
                        div {
                            label() {
                                htmlFor = PARAM_USERNAME

                                +"Login"
                            }
                            input(type = InputType.text, name = PARAM_USERNAME) {
                                value = username ?: ""
                            }
                        }
                        div {
                            label() {
                                htmlFor = PARAM_PASSWORD
                                +"Password"
                            }
                            input(type = InputType.password, name = PARAM_PASSWORD) {

                            }
                        }
                        button(type = ButtonType.submit) {
                            +"Sign-in"
                        }
                    }
                }
            }
        }.toString()
    }

    companion object {
        const val PARAM_AUTH_CTX = "auth_ctx"
        const val PARAM_USERNAME = "username"
        const val PARAM_PASSWORD = "password"
    }
}