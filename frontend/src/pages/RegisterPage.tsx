import { ProfilePhotoField } from "../components/ProfilePhoto";
import { useEffect, useState } from "react";
import { ArrowRight, ExternalLink, FileText } from "lucide-react";
import { AccountLayout } from "./PageLayout";
import {
  PasswordField,
  ProfileFields,
  type Draft,
} from "../components/FormFields";
import { apiErrorMessage, authApi } from "../lib/backend";
import type { Session } from "../lib/contracts";
import { SocialAuthButtons } from "../components/SocialAuthButtons";

export function RegisterPage({
  onAuthenticated,
}: {
  onAuthenticated: (session: Session, values: Draft) => void;
}) {
  const [values, setValues] = useState<Draft>({ tipoPessoa: "PF" });
  const [photo, setPhoto] = useState<File | null>(null);
  const [created, setCreated] = useState(false);
  const [accepted, setAccepted] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [versions, setVersions] = useState<{
    versaoTermosUso: string;
    versaoPoliticaPrivacidade: string;
  } | null>(null);
  const [termsOpen, setTermsOpen] = useState(false);
  const socialToken = new URLSearchParams(window.location.hash.split("?")[1] || "").get("social_token");
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
  useEffect(() => {
    if (!socialToken) return;
    authApi.socialRegistrationInfo(socialToken).then((social) => {
      setValues((current) => ({ ...current, nome: social.nome, email: social.email }));
    }).catch((requestError) => setError(apiErrorMessage(requestError,
      "Não foi possível continuar o cadastro social.")));
  }, [socialToken]);
  return (
    <AccountLayout
      wide
      eyebrow="CRIAR CONTA"
      title="Seu próximo passo começa aqui."
      description="Cadastre seus dados para comprar e anunciar na PHC Auto. Campos com * são obrigatórios no cadastro."
    >
      {!socialToken && <SocialAuthButtons action="cadastrar" />}
      <form
        className="page-form"
        onSubmit={async (e) => {
          e.preventDefault();
          setLoading(true);
          setError("");
          try {
            if (!versions) throw new Error("DOCUMENTS_NOT_LOADED");
            if (socialToken) {
              const response = await authApi.socialRegister(socialToken, values, versions);
              onAuthenticated(authApi.asSession(response), values);
              return;
            }
            if (values.senha !== values.confirmacaoSenha) {
              setError("As senhas não coincidem.");
              return;
            }
            if (!created) await authApi.register(values, versions);
            setCreated(true);
            window.location.hash = "entrar";
            return;
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
        <ProfilePhotoField file={photo} onChange={setPhoto} />
        {created && <p role="status">Conta criada. Entre para validar seu primeiro acesso.</p>}
        <section className="form-section">
          <div className="form-section-title">
            <span>03</span>
            <div>
              <h2>Acesso à conta</h2>
              <p>Use uma senha exclusiva para a PHC Auto.</p>
            </div>
          </div>
          {!socialToken && <PasswordField
            registration
            value={values.senha || ""}
            onChange={(v) => change("senha", v)}
          />}
          {!socialToken && <PasswordField
            confirmation
            label="Confirme sua senha"
            value={values.confirmacaoSenha || ""}
            onChange={(v) => change("confirmacaoSenha", v)}
          />}
          {socialToken && <p className="inline-notice" role="status">
            Conta Google confirmada. Você não precisa criar uma senha.
          </p>}
        </section>
        <div className="terms-preview">
          <FileText size={19} />
          <div>
            <strong>Termos de uso e privacidade</strong>
            <p className="terms-version">
              Documento vigente: versão {versions?.versaoTermosUso || "carregando…"}
            </p>
            <a
              className="terms-document-link"
              href="/documentos/termos-uso-politica-privacidade-v1.1.pdf"
              target="_blank"
              rel="noreferrer"
            >
              <FileText size={16} />
              Ler os Termos de Uso e a Política de Privacidade
              <ExternalLink size={14} />
            </a>
            <label className="check-label terms-check">
              <input
                type="checkbox"
                checked={accepted}
                onChange={(event) => setAccepted(event.target.checked)}
                required
              />
              Li o documento da versão vigente e aceito os Termos de Uso e a
              Política de Privacidade.
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
                O momento do aceite e as versões {versions?.versaoTermosUso} dos
                documentos serão enviados ao servidor junto com o cadastro e
                registrados para fins de comprovação.
              </p>
            )}
          </div>
        </div>
        <button className="button primary" type="submit" disabled={loading || !accepted || !versions}>
          {loading ? "Criando sua conta…" : created ? "Concluir cadastro" : "Criar conta e continuar"}
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
