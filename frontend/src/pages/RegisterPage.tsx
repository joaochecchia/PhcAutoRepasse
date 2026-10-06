import { useEffect, useState } from "react";
import { ArrowRight, FileText } from "lucide-react";
import { AccountLayout } from "./PageLayout";
import {
  PasswordField,
  ProfileFields,
  type Draft,
} from "../components/FormFields";
import { apiErrorMessage, authApi } from "../lib/backend";
import type { Session } from "../lib/contracts";

export function RegisterPage({
  onAuthenticated,
}: {
  onAuthenticated: (session: Session, values: Draft) => void;
}) {
  const [values, setValues] = useState<Draft>({ tipoPessoa: "PF" });
  const [accepted, setAccepted] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [versions, setVersions] = useState<{
    versaoTermosUso: string;
    versaoPoliticaPrivacidade: string;
  } | null>(null);
  const [termsOpen, setTermsOpen] = useState(false);
  const change = (key: string, value: string) =>
    setValues((prev) => ({ ...prev, [key]: value }));
  useEffect(() => {
    authApi
      .registrationConfig()
      .then(setVersions)
      .catch((requestError) =>
        setError(
          apiErrorMessage(
            requestError,
            "Não foi possível carregar os documentos vigentes.",
          ),
        ),
      );
  }, []);
  return (
    <AccountLayout
      wide
      eyebrow="CRIAR CONTA"
      title="Seu próximo passo começa aqui."
      description="Cadastre seus dados para comprar e anunciar na PHC Auto. Campos com * são obrigatórios no cadastro."
    >
      <form
        className="page-form"
        onSubmit={async (e) => {
          e.preventDefault();
          setLoading(true);
          setError("");
          try {
            if (!versions) throw new Error("DOCUMENTS_NOT_LOADED");
            await authApi.register(values, versions);
            const session = await authApi.login(values.email, values.senha);
            onAuthenticated(session, values);
          } catch (requestError) {
            setError(
              apiErrorMessage(requestError, "Não foi possível criar sua conta."),
            );
          } finally {
            setLoading(false);
          }
        }}
      >
        <div className="form-section-title">
          <span>01</span>
          <div>
            <h2>Sobre você</h2>
            <p>
              Escolha pessoa física ou jurídica para ver os campos do seu
              perfil.
            </p>
          </div>
        </div>
        <ProfileFields values={values} onChange={change} />
        <section className="form-section">
          <div className="form-section-title">
            <span>03</span>
            <div>
              <h2>Acesso à conta</h2>
              <p>Use uma senha exclusiva para a PHC Auto.</p>
            </div>
          </div>
          <PasswordField
            registration
            value={values.senha || ""}
            onChange={(v) => change("senha", v)}
          />
        </section>
        <div className="terms-preview">
          <FileText size={19} />
          <div>
            <strong>Termos de uso e privacidade</strong>
            <label className="check-label terms-check">
              <input
                type="checkbox"
                checked={accepted}
                onChange={(event) => setAccepted(event.target.checked)}
                required
              />
              Li e aceito os Termos de Uso e a Política de Privacidade vigentes.
            </label>
            <button
              className="text-button"
              type="button"
              aria-expanded={termsOpen}
              onClick={() => setTermsOpen(!termsOpen)}
            >
              Sobre o aceite
            </button>
            {termsOpen && (
              <p className="inline-notice">
                O momento deste aceite será enviado ao servidor junto com o
                cadastro e registrado para fins de comprovação.
              </p>
            )}
          </div>
        </div>
        <button className="button primary" type="submit" disabled={loading || !accepted || !versions}>
          {loading ? "Criando sua conta…" : "Criar conta e continuar"}
          <ArrowRight size={17} />
        </button>
        {error && (
          <p className="inline-notice error-notice" role="alert">
            {error}
          </p>
        )}
        <p className="preview-caption">
          Os dados serão validados pelo servidor. Após o cadastro, sua sessão
          será iniciada por cookie HttpOnly.
        </p>
      </form>
      <p className="page-switch">
        Já tem uma conta?{" "}
        <a href="#entrar">
          Entrar
          <ArrowRight size={14} />
        </a>
      </p>
    </AccountLayout>
  );
}
