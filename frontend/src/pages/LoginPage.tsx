import { useState } from "react";
import { ArrowRight, LockKeyhole } from "lucide-react";
import { AccountLayout } from "./PageLayout";
import { Fields, PasswordField } from "../components/FormFields";

export function LoginPage() {
  const [values, setValues] = useState({ email: "", senha: "" });
  const [submitted, setSubmitted] = useState(false);
  return (
    <AccountLayout
      eyebrow="ENTRAR NA SUA CONTA"
      title="Bom te ver por aqui."
      description="Entre para encontrar seu próximo veículo e dar visibilidade ao seu anúncio."
    >
      <form
        className="page-form login-form"
        onSubmit={(e) => {
          e.preventDefault();
          setSubmitted(true);
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
        <button className="button primary" type="submit">
          Entrar
          <ArrowRight size={17} />
        </button>
        {submitted && (
          <p className="inline-notice" role="status">
            O login ainda não está conectado. Nenhuma credencial foi enviada e
            nenhuma sessão foi criada.
          </p>
        )}
        <p className="preview-caption">
          <LockKeyhole size={15} />
          Prévia de interface. Seus dados não serão enviados ou salvos.
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
