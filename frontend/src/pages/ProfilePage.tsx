import { ProfilePhotoEditor } from "../components/ProfilePhoto";
import { useState, type Dispatch, type SetStateAction } from "react";
import { ArrowRight, UserRound, ShieldCheck } from "lucide-react";
import { PasswordField, ProfileFields, type Draft } from "../components/FormFields";
import { PageBreadcrumb } from "./PageLayout";
import { apiErrorMessage, authApi } from "../lib/backend";

export function ProfilePage({
  values,
  setValues,
  usuarioId,
  onContinue,
  mode = "announce",
  onPhotoUpdated,
}: {
  values: Draft;
  setValues: Dispatch<SetStateAction<Draft>>;
  usuarioId: string;
  onContinue?: () => void;
  mode?: "announce" | "account";
  onPhotoUpdated?: () => void;
}) {
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [passwordSaving, setPasswordSaving] = useState(false);
  const [passwordMessage, setPasswordMessage] = useState("");
  const [savedMessage, setSavedMessage] = useState("");
  const isAccount = mode === "account";
  return (
    <section className="container dedicated-page">
      <PageBreadcrumb label={isAccount ? "Meu perfil" : "Dados do anunciante"} />
      <div className="page-title-row">
        <div>
          <span className="eyebrow">{isAccount ? "SUA CONTA" : "VAMOS PREPARAR SEU ANÚNCIO"}</span>
          <h1>{isAccount ? "Seus dados de perfil." : "Primeiro, vamos conhecer você."}</h1>
          <p className="page-intro">
            {isAccount
              ? "Consulte e atualize os dados, a foto e a senha da sua conta."
              : "Confira os dados do anunciante e depois preencha as informações do veículo."}
          </p>
        </div>
        <span className="button secondary">
          <UserRound size={17} />
          Sessão autenticada
        </span>
      </div>
      {!isAccount && <div className="workflow-trail">
        <span className="active">01 · Seus dados</span>
        <span>02 · Seu veículo</span>
        <span>03 · Revisão do anúncio</span>
      </div>}
      <ProfilePhotoEditor userId={usuarioId} onUpdated={onPhotoUpdated} />
      <div className="editor-layout">
        <form
          className="page-form page-panel"
          onSubmit={async (e) => {
            e.preventDefault();
            setSaving(true);
            setError("");
            setSavedMessage("");
            try {
              await authApi.updateProfile(usuarioId, values);
              if (isAccount) setSavedMessage("Dados do perfil atualizados com sucesso.");
              else onContinue?.();
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
              <h2>{isAccount ? "Dados pessoais" : "Dados do anunciante"}</h2>
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
              {saving ? "Salvando…" : isAccount ? "Salvar alterações" : "Continuar para o veículo"}
              {!isAccount && <ArrowRight size={17} />}
            </button>
          </div>
          {savedMessage && <p className="inline-notice" role="status">{savedMessage}</p>}
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
      <form className="page-form page-panel password-reset-panel" onSubmit={async (event) => {
        event.preventDefault();
        setPasswordSaving(true);
        setPasswordMessage("");
        try {
          await authApi.resetPassword(usuarioId, newPassword);
          setNewPassword("");
          setPasswordMessage("Senha alterada com sucesso.");
        } catch (requestError) {
          setPasswordMessage(apiErrorMessage(requestError, "Não foi possível alterar a senha."));
        } finally {
          setPasswordSaving(false);
        }
      }}>
        <div className="form-section-title">
          <span><ShieldCheck size={17} /></span>
          <div>
            <h2>Alterar senha</h2>
            <p>A nova senha precisa atender a todos os critérios de segurança.</p>
          </div>
        </div>
        <PasswordField registration label="Nova senha" value={newPassword} onChange={setNewPassword} />
        <div className="page-form-actions">
          <button className="button primary" disabled={passwordSaving}>
            {passwordSaving ? "Alterando…" : "Alterar senha"}
          </button>
        </div>
        {passwordMessage && <p className="inline-notice" role="status">{passwordMessage}</p>}
      </form>
    </section>
  );
}
