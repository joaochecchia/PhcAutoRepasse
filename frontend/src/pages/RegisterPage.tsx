import { useState } from "react";
import { ArrowRight, FileText } from "lucide-react";
import { AccountLayout } from "./PageLayout";
import {
  PasswordField,
  ProfileFields,
  type Draft,
} from "../components/FormFields";

export function RegisterPage() {
  const [values, setValues] = useState<Draft>({ tipoPessoa: "PF" });
  const [submitted, setSubmitted] = useState(false);
  const [termsOpen, setTermsOpen] = useState(false);
  const change = (key: string, value: string) =>
    setValues((prev) => ({ ...prev, [key]: value }));
  return (
    <AccountLayout
      wide
      eyebrow="CRIAR CONTA"
      title="Seu próximo passo começa aqui."
      description="Cadastre seus dados para comprar e anunciar na PHC Auto. Campos com * são obrigatórios no cadastro."
    >
      <form
        className="page-form"
        onSubmit={(e) => {
          e.preventDefault();
          setSubmitted(true);
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
            <p>
              Os documentos oficiais e o aceite estarão disponíveis antes da
              ativação do cadastro.
            </p>
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
                Esta prévia não coleta consentimento. Na versão conectada, você
                poderá ler os documentos vigentes antes de aceitar. Nenhum
                aceite é registrado aqui.
              </p>
            )}
          </div>
        </div>
        <button className="button primary" type="submit">
          Conferir cadastro
          <ArrowRight size={17} />
        </button>
        {submitted && (
          <p className="inline-notice" role="status">
            Prévia preenchida. O cadastro e o aceite oficial ainda não estão
            disponíveis. Nenhuma conta foi criada e nenhum dado foi enviado.
          </p>
        )}
        <p className="preview-caption">
          Dados mantidos apenas enquanto esta página estiver aberta. Validação e
          criação da conta serão feitas pelo servidor.
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
