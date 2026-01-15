import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import {SeijUIProvider} from "@seij/common-ui";
import './index.css'
import {Button, Field, Input, makeStyles, MessageBar, Title3} from "@fluentui/react-components";
import logoUrl from '../public/favicon/favicon.svg'

const useStyles = makeStyles({
  page: {
    minHeight: "100vh",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    background: "linear-gradient(145deg, #f6f7fb 0%, #eef1f8 100%)"
  },
  form: {
    width: "min(420px, 100%)",
    display: "flex",
    flexDirection: "column",
    gap: "12px",
    padding: "28px",
    borderRadius: "12px",
    background: "#ffffff",
    boxShadow: "0 12px 36px rgba(19, 33, 68, 0.12)"
  },
  brand: {
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    gap: "8px"
  },
  logo: {
    width: "64px",
    height: "64px"
  },
  fields: {
    display: "flex",
    flexDirection: "column",
    gap: "1em"
  },
  submitRow: {
    display: "flex",
    alignItems: "flex-end"
  }
})

function LoginForm() {
  const styles = useStyles()
  const config = window.__MEDATARUN_CONFIG__ ?? {};
  const errorMessage = config.error ?? "";
  const authCtx = config.auth_ctx ?? "";
  const defaultUserName = config.username ?? ""

  return (
    <div className={styles.page}>
      <form method="post" action="" className={styles.form}>
        <input type="hidden" name="auth_ctx" value={authCtx}/>
        <div className={styles.brand}>
          <img className={styles.logo} src={logoUrl} alt="Medatarun"/>
          <div>
            <Title3>Medatarun</Title3>
          </div>
        </div>
        <div className={styles.fields}>
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
          <div className={styles.submitRow}>
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
