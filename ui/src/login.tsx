import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import {SeijUIProvider} from "@seij/common-ui";
import './index.css'
import './login.css'
import {Button, Field, Input, MessageBar, Title3} from "@fluentui/react-components";
import logoUrl from '../public/favicon/favicon.svg'

function LoginForm() {
  const config = window.__MEDATARUN_CONFIG__ ?? {};
  const errorMessage = config.error ?? "";
  const authCtx = config.auth_ctx ?? "";
  const  defaultUserName = config.username ?? ""

  return (
    <div className="login-page">
      <form method="post" action="" className="login-form">
        <input type="hidden" name="auth_ctx" value={authCtx}/>
        <div className="login-brand">
          <img className="login-logo" src={logoUrl} alt="Medatarun"/>
          <div>
            <Title3>Medatarun</Title3>
          </div>
        </div>
        <div style={{display: "flex", flexDirection: "column", gap: "1em"}}>
          <div>
            <Field label="Username">
              <Input id="login-username" name="username" type="text" autoFocus={true} autoComplete="username" required defaultValue={defaultUserName}/>
            </Field>
          </div>
          <div>
            <Field label="Password">
              <Input id="login-password" name="password" type="password" autoComplete="current-password" required/>
            </Field>
          </div>
          <div>
            {errorMessage ? <MessageBar intent="error" role="alert">{errorMessage}</MessageBar> : null}
          </div>
          <div style={{display:"flex", alignItems:"flex-end"}}>
            <Button appearance="primary" type="submit">Sign in</Button>
          </div>
        </div>
      </form>
    </div>
  )
}

function LoginApp() {
  return (
    <SeijUIProvider>
      <LoginForm/>
    </SeijUIProvider>
  )
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <LoginApp/>
  </StrictMode>,
)
