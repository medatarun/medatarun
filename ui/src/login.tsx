import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import {SeijUIProvider} from "@seij/common-ui";
import './index.css'
import './login.css'
import {Button, Input} from "@fluentui/react-components";

function LoginForm() {
  const config = window.__MEDATARUN_CONFIG__ ?? {};
  const errorMessage = config.error ?? "";
  const authCtx = config.auth_ctx ?? "";

  return (
    <div className="login-page">
      <form method="post" action="" className="login-form">
        <h1 className="login-title">Sign in</h1>
        <label className="login-label" htmlFor="login-username">Username</label>
        <Input id="login-username" name="username" type="text" autoComplete="username" required />
        <label className="login-label" htmlFor="login-password">Password</label>
        <Input id="login-password" name="password" type="password" autoComplete="current-password" required />
        <input type="hidden" name="auth_ctx" value={authCtx} />
        {errorMessage ? <div className="login-error" role="alert">{errorMessage}</div> : null}
        <div className="login-actions">
          <Button appearance="primary" type="submit">Log in</Button>
        </div>
      </form>
    </div>
  )
}

function LoginApp() {
  return (
    <SeijUIProvider>
      <LoginForm />
    </SeijUIProvider>
  )
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <LoginApp />
  </StrictMode>,
)
