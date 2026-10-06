import { useState, type Dispatch, type SetStateAction } from "react";
import { ArrowRight, UserRound, ShieldCheck } from "lucide-react";
import { ProfileFields, type Draft } from "../components/FormFields";
import { PageBreadcrumb } from "./PageLayout";
import { apiErrorMessage, authApi } from "../lib/backend";

export function ProfilePage({
  values,
  setValues,
  usuarioId,
  onContinue,
}: {
  values: Draft;
  setValues: Dispatch<SetStateAction<Draft>>;
  usuarioId: string;
  onContinue: () => void;
}) {
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  return (
    <section className="container dedicated-page">
      <PageBreadcrumb label="Dados do anunciante" />
      <div className="page-title-row">
        <div>
          <span className="eyebrow">VAMOS PREPARAR SEU ANÚNCIO</span>
          <h1>Primeiro, vamos conhecer você.</h1>
          <p className="page-intro">
            Confira os dados do anunciante e depois preencha as informações do
            veículo.
          </p>
        </div>
        <span className="button secondary">
          <UserRound size={17} />
          Sessão autenticada
        </span>
      </div>
      <div className="workflow-trail">
        <span className="active">01 · Seus dados</span>
        <span>02 · Seu veículo</span>
        <span>03 · Revisão do anúncio</span>
      </div>
      <div className="editor-layout">
        <form
          className="page-form page-panel"
          onSubmit={async (e) => {
            e.preventDefault();
            setSaving(true);
            setError("");
            try {
              await authApi.updateProfile(usuarioId, values);
              onContinue();
            } catch (requestError) {
              setError(apiErrorMessage(requestError, "Não foi possível atualizar seus dados."));
            } finally {
              setSaving(false);
            }
          }}
        >
          <div className="form-section-title">
            <span>
              <UserRound size={17} />
            </span>
            <div>
              <h2>Dados do anunciante</h2>
              <p>
                Confira os dados carregados da sua conta. * Campos necessários
                no cadastro.
              </p>
            </div>
          </div>
          <ProfileFields
            values={values}
            onChange={(key, value) =>
              setValues((current) => ({ ...current, [key]: value }))
            }
          />
          <div className="page-form-actions">
            <a href="#inicio" className="button secondary">
              Voltar ao início
            </a>
            <button className="button primary" disabled={saving}>
              {saving ? "Salvando…" : "Continuar para o veículo"}
              <ArrowRight size={17} />
            </button>
          </div>
          {error && <p className="inline-notice error-notice" role="alert">{error}</p>}
        </form>
        <aside className="editor-aside">
          <div className="page-panel">
            <span className="dialog-emblem">
              <ShieldCheck size={27} />
            </span>
            <h2>Cada informação no seu lugar.</h2>
            <p>
              Seus documentos e endereço completo fazem parte do cadastro. A
              vitrine pública mostra apenas a cidade do veículo.
            </p>
            <p>
              Estes dados foram carregados da sua conta e suas alterações serão
              validadas pelo servidor antes de continuar.
            </p>
          </div>
        </aside>
      </div>
    </section>
  );
}
