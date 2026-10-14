import { useEffect, useState } from "react";
import { ArrowRight, LockKeyhole, UserPlus } from "lucide-react";
import { AccountLayout } from "./PageLayout";
import { Fields, PasswordField } from "../components/FormFields";
import { apiErrorMessage, authApi } from "../lib/backend";
import type { Session } from "../lib/contracts";

export function LoginPage({ onAuthenticated }: { onAuthenticated: (session: Session) => void }) {
  const [values, setValues] = useState({ email: "", senha: "" });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [challenge, setChallenge] = useState<{ id: string; email: string | null } | null>(null);
  const [codigo, setCodigo] = useState("");
  const [confiar, setConfiar] = useState(true);
  const [recuperando, setRecuperando] = useState(false);
  const [notice, setNotice] = useState("");
  const [resending, setResending] = useState(false);
  const [resendIn, setResendIn] = useState(0);
  useEffect(() => {
    if (resendIn <= 0) return;
    const timer = window.setInterval(() => setResendIn((seconds) => Math.max(0, seconds - 1)), 1000);
    return () => window.clearInterval(timer);
  }, [resendIn]);
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
            if (recuperando) {
              const response = await authApi.requestPasswordReset(values.email);
              setNotice(response.detail);
              return;
            }
            const response = challenge
              ? await authApi.verifySecondFactor(challenge.id, codigo, confiar)
              : await authApi.login(values.email, values.senha);
            if (response.status === "SEGUNDO_FATOR_NECESSARIO" && response.challengeId) {
              setChallenge({ id: response.challengeId, email: response.emailMascarado });
              setResendIn(60);
              setNotice(`Enviamos um código para ${response.emailMascarado || "seu e-mail"}.`);
            } else {
              onAuthenticated(authApi.asSession(response));
            }
          } catch (requestError) {
            setError(apiErrorMessage(requestError, "E-mail ou senha inválidos."));
          } finally {
            setLoading(false);
          }
        }}
      >
        {!challenge && <Fields
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
          showRequirement={false}
        />}
        {!challenge && !recuperando && <PasswordField
          value={values.senha}
          onChange={(senha) => setValues({ ...values, senha })}
        />}
        {challenge && <label className="field"><span>Código de 6 dígitos</span><input
          inputMode="numeric" autoComplete="one-time-code" pattern="[0-9]{6}" maxLength={6}
          value={codigo} onChange={(event) => setCodigo(event.target.value.replace(/\D/g, ""))} required /></label>}
        {challenge && <label className="check-label"><input type="checkbox" checked={confiar}
          onChange={(event) => setConfiar(event.target.checked)} />Confiar neste dispositivo por 30 dias</label>}
        <button className="button primary" type="submit" disabled={loading}>
          {loading ? "Aguarde…" : recuperando ? "Enviar instruções" : challenge ? "Validar código" : "Entrar"}
          <ArrowRight size={17} />
        </button>
        {challenge && <button className="button secondary" type="button"
          disabled={resending || resendIn > 0} onClick={async () => {
            setResending(true); setError(""); setNotice("");
            try {
              const response = await authApi.resendSecondFactor(challenge.id);
              if (!response.challengeId) throw new Error("CHALLENGE_INVALID");
              setChallenge({ id: response.challengeId, email: response.emailMascarado });
              setCodigo(""); setResendIn(60);
              setNotice(`Novo código enviado para ${response.emailMascarado || "seu e-mail"}. O código anterior foi invalidado.`);
            } catch (requestError) {
              setError(apiErrorMessage(requestError, "Não foi possível reenviar o código."));
            } finally { setResending(false); }
          }}>
          {resending ? "Reenviando…" : resendIn > 0 ? `Reenviar em ${resendIn}s` : "Reenviar código"}
        </button>}
        {error && (
          <p className="inline-notice error-notice" role="alert">
            {error}
          </p>
        )}
        {notice && <p className="inline-notice" role="status">{notice}</p>}
        {!challenge && <button type="button" className="text-button" onClick={() => { setRecuperando(!recuperando); setError(""); setNotice(""); }}>
          {recuperando ? "Voltar para o login" : "Esqueci minha senha"}
        </button>}
        <p className="preview-caption">
          <LockKeyhole size={15} />
          A sessão é protegida por cookie HttpOnly. O navegador não armazena nem
          acessa o token de autenticação.
        </p>
      </form>
      <section className="login-register-cta" aria-label="Criar uma conta">
        <div>
          <span className="login-register-icon" aria-hidden="true">
            <UserPlus size={19} />
          </span>
          <span>
            <strong>Ainda não tem uma conta?</strong>
            <small>Cadastre-se para anunciar seu veículo com segurança.</small>
          </span>
        </div>
        <a className="button secondary" href="#cadastro">
          Criar minha conta
          <ArrowRight size={16} />
        </a>
      </section>
    </AccountLayout>
  );
}
