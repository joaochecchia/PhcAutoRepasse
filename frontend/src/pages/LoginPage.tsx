import { useEffect, useState } from "react";
import { ArrowRight, LockKeyhole, UserPlus } from "lucide-react";
import { AccountLayout } from "./PageLayout";
import { Fields, PasswordField, VerificationCodeField } from "../components/FormFields";
import { apiErrorMessage, authApi } from "../lib/backend";
import type { Session } from "../lib/contracts";
import { SocialAuthButtons } from "../components/SocialAuthButtons";

export function LoginPage({ onAuthenticated }: { onAuthenticated: (session: Session) => void }) {
  const [values, setValues] = useState({ email: "", senha: "" });
  const [loading, setLoading] = useState(false);
  const oauthError = new URLSearchParams(window.location.hash.split("?")[1] || "").get("oauth_error");
  const [error, setError] = useState(() => oauthError === "conta_existente"
    ? "Já existe uma conta com este e-mail. Entre com sua senha para vincular a conta social com segurança."
    : oauthError ? "Não foi possível autenticar com a conta social. Tente novamente." : "");
  const [challenge, setChallenge] = useState<{ id: string; email: string | null } | null>(null);
  const [codigo, setCodigo] = useState("");
  const [confiar, setConfiar] = useState(true);
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
      compact
      eyebrow="ENTRAR NA SUA CONTA"
      title="Bom te ver por aqui."
      description="Entre para encontrar seu próximo veículo e dar visibilidade ao seu anúncio."
    >
      {!challenge && <SocialAuthButtons action="entrar" />}
      <form
        className="page-form login-form"
        onSubmit={async (e) => {
          e.preventDefault();
          setLoading(true);
          setError("");
          try {
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
        {!challenge && <PasswordField
          value={values.senha}
          onChange={(senha) => setValues({ ...values, senha })}
        />}
        {challenge && <VerificationCodeField value={codigo} onChange={setCodigo} autoFocus />}
        {challenge && <label className="check-label"><input type="checkbox" checked={confiar}
          onChange={(event) => setConfiar(event.target.checked)} />Confiar neste dispositivo por 30 dias</label>}
        <button className="button primary" type="submit" disabled={loading}>
          {loading ? "Aguarde…" : challenge ? "Validar código" : "Entrar"}
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
        {!challenge && <a className="text-button" href="#redefinir-senha">Esqueci minha senha</a>}
        <p className="preview-caption login-security-note">
          <LockKeyhole size={15} />
          Sua sessão é protegida e seus dados de acesso não ficam expostos ao navegador.
        </p>
      </form>
      {!challenge && <section className="login-register-cta" aria-label="Criar uma conta">
        <div>
          <span className="login-register-icon" aria-hidden="true">
            <UserPlus size={19} />
          </span>
          <span>
            <strong>Ainda não tem uma conta?</strong>
            <small>Cadastre-se para comprar ou anunciar.</small>
          </span>
        </div>
        <a className="button secondary" href="#cadastro">
          Criar minha conta
          <ArrowRight size={16} />
        </a>
      </section>}
    </AccountLayout>
  );
}
