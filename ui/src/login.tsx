import { type PropsWithChildren, StrictMode, useState } from "react";
import { createRoot } from "react-dom/client";
import { SeijUIProvider } from "@seij/common-ui";
import "./index.css";
import {
  Button,
  Field,
  Input,
  makeStyles,
  MessageBar,
  Title3,
} from "@fluentui/react-components";
import logoUrl from "../public/favicon/favicon.svg";
import { useAppI18n } from "@/services/appI18n.tsx";

const useStyles = makeStyles({
  page: {
    minHeight: "100vh",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    background: "linear-gradient(145deg, #f6f7fb 0%, #eef1f8 100%)",
  },
  form: {
    width: "min(420px, 100%)",
    display: "flex",
    flexDirection: "column",
    gap: "12px",
    padding: "28px",
    borderRadius: "12px",
    background: "#ffffff",
    boxShadow: "0 12px 36px rgba(19, 33, 68, 0.12)",
  },
  brand: {
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    gap: "8px",
  },
  logo: {
    width: "64px",
    height: "64px",
  },
  fields: {
    display: "flex",
    flexDirection: "column",
    gap: "1em",
  },
  submitRow: {
    display: "flex",
    alignItems: "flex-end",
  },
});

function LoginForm() {
  const [step, setStep] = useState<number>(1);
  return (
    <Layout>
      {step == 1 && <StepLoginForm />}
      {step == 2 && <StepChoosePersona />}
    </Layout>
  );
}

function Layout({ children }: PropsWithChildren) {
  const styles = useStyles();
  const config = window.__MEDATARUN_CONFIG__ ?? {};
  const clientInternal = config.clientInternal ?? "";
  const clientName = config.clientName ?? "";
  const errorMessage = config.error ?? "";
  const authCtx = config.auth_ctx ?? "";
  return (
    <div className={styles.page}>
      <form method="post" action="" className={styles.form}>
        <input type="hidden" name="auth_ctx" value={authCtx} />
        <div className={styles.brand}>
          <img className={styles.logo} src={logoUrl} alt="Medatarun" />
          <div>
            <Title3>Medatarun</Title3>
          </div>
          {!clientInternal && (
            <div>
              Connect with <strong>{clientName}</strong>
            </div>
          )}
        </div>
        <div className={styles.fields}>
          <div>
            {errorMessage ? (
              <MessageBar intent="error" role="alert">
                {errorMessage}
              </MessageBar>
            ) : null}
          </div>
          {children}
        </div>
      </form>
    </div>
  );
}

function StepLoginForm({}: {}) {
  const { t } = useAppI18n();
  const styles = useStyles();
  const config = window.__MEDATARUN_CONFIG__ ?? {};
  const defaultUserName = config.username ?? "";
  return (
    <>
      <div>
        <Field label={t("loginPage_usernameLabel")}>
          <Input
            id="login-username"
            name="username"
            type="text"
            autoFocus={true}
            autoComplete="username"
            required
            defaultValue={defaultUserName}
          />
        </Field>
      </div>
      <div>
        <Field label={t("loginPage_passwordLabel")}>
          <Input
            id="login-password"
            name="password"
            type="password"
            autoComplete="current-password"
            required
          />
        </Field>
      </div>
      <div className={styles.submitRow}>
        <Button appearance="primary" type="submit">
          {t("loginPage_submitButton")}
        </Button>
      </div>
    </>
  );
}

function StepChoosePersona() {
  const config = window.__MEDATARUN_CONFIG__ ?? {};
  const clientInternal = config.clientInternal ?? "";
  const styles = useStyles();
  const [isSeparatedIdentity, setIsSeparatedIdentity] = useState<boolean>(true);
  const [identityValue, setIdentityValue] = useState<string>("__new__");
  if (clientInternal) return null;
  return (
    <div>
      <div>
        <h4>Hello Theo,</h4>
      </div>
      <p>
        You can choose to let this tool act as yourself or separate its activity
        from yours. This is especially useful if you are using an AI Agent to
        separate what <i>you</i> do from what the <i>AI does for you</i>.
      </p>
      <div>
        <div>
          <input
            type={"checkbox"}
            checked={!isSeparatedIdentity}
            onChange={(e) => setIsSeparatedIdentity(!e.target.checked)}
          ></input>{" "}
          As me
        </div>
        <div>
          <input
            type={"checkbox"}
            checked={isSeparatedIdentity}
            onChange={(e) => setIsSeparatedIdentity(e.target.checked)}
          ></input>{" "}
          Use a separated identity
          {isSeparatedIdentity && (
            <span>
              <select value={identityValue}>
                <option value={"code"}>Codex</option>
                <option value={"__new__"}>-- New identity --</option>
              </select>
              <input type={"name"} placeholder={"Choose a name"} />
            </span>
          )}
        </div>
      </div>
      <p>
        You can review and adjust this tool permissions on your profile page.
      </p>
      <p>
        If you are unsure of what you are doing, just close this page, nothing
        will be transmitted to this application.
      </p>
      <div className={styles.submitRow}>
        <Button appearance="primary" type="submit">
          Continue
        </Button>
      </div>
    </div>
  );
}

function LoginApp() {
  return (
    <SeijUIProvider>
      <LoginForm />
    </SeijUIProvider>
  );
}

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <LoginApp />
  </StrictMode>,
);
