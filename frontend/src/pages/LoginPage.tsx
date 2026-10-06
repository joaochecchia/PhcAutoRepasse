import { useState } from "react";
import { ArrowRight, LockKeyhole } from "lucide-react";
import { AccountLayout } from "./PageLayout";
import { Fields, PasswordField } from "../components/FormFields";
import { apiErrorMessage, authApi } from "../lib/backend";
import type { Session } from "../lib/contracts";

export function LoginPage({ onAuthenticated }: { onAuthenticated: (session: Session) => void }) {
  const [values, setValues] = useState({ email: "", senha: "" });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  return (
    <AccountLayout
      eyebrow="ENTRAR NA SUA CONTA"
      title="Bom te ver por aqui."
      description="Entre para encontrar seu próximo veículo e dar visibilidade ao seu anúncio."
    >
      <form
        className="page-form login-form"
        onSubmit={async (e) => {
          e.preventDefault();
          setLoading(true);
          setError("");
          try {
            onAuthenticated(await authApi.login(values.email, values.senha));
          } catch (requestError) {
            setError(apiErrorMessage(requestError, "E-mail ou senha inválidos."));
          } finally {
            setLoading(false);
          }
        }}
      >
        <Fields
          fields={[
            {
              key: "email",
              label: "E-mail",
              type: "email",
              required: true,
              autoComplete: "email",
              placeholder: "voce@exemplo.com",
            },
          ]}
          values={values}
          onChange={(_, v) => setValues({ ...values, email: v })}
        />
        <PasswordField
          value={values.senha}
          onChange={(senha) => setValues({ ...values, senha })}
        />
        <button className="button primary" type="submit" disabled={loading}>
          {loading ? "Entrando…" : "Entrar"}
          <ArrowRight size={17} />
        </button>
        {error && (
          <p className="inline-notice error-notice" role="alert">
            {error}
          </p>
        )}
        <p className="preview-caption">
          <LockKeyhole size={15} />
          A sessão é protegida por cookie HttpOnly. O navegador não armazena nem
          acessa o token de autenticação.
        </p>
      </form>
      <p className="page-switch">
        Ainda não tem uma conta?{" "}
        <a href="#cadastro">
          Criar minha conta
          <ArrowRight size={14} />
        </a>
      </p>
    </AccountLayout>
  );
}
