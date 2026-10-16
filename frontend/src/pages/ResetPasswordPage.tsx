import { useEffect, useRef, useState } from "react";
import { AccountLayout } from "./PageLayout";
import { PasswordField, VerificationCodeField } from "../components/FormFields";
import { apiErrorMessage, authApi } from "../lib/backend";

type Step = "identify" | "code" | "password" | "done";

export function ResetPasswordPage({ authenticated }: { authenticated: boolean }) {
  const params = new URLSearchParams(window.location.hash.split("?")[1] || "");
  const fromProfile = params.get("origem") === "perfil" && authenticated;
  const started = useRef(false);
  const [step, setStep] = useState<Step>(fromProfile ? "code" : "identify");
  const [email, setEmail] = useState("");
  const [challengeId, setChallengeId] = useState("");
  const [maskedEmail, setMaskedEmail] = useState("");
  const [codigo, setCodigo] = useState("");
  const [token, setToken] = useState("");
  const [senha, setSenha] = useState("");
  const [confirmacaoSenha, setConfirmacaoSenha] = useState("");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  async function startChallenge() {
    setLoading(true); setMessage("");
    try {
      const response = fromProfile ? await authApi.requestPasswordChange() : await authApi.requestPasswordReset(email);
      if (!response.challengeId) throw new Error("CHALLENGE_MISSING");
      setChallengeId(response.challengeId); setMaskedEmail(response.emailMascarado || "seu e-mail");
      setStep("code"); setMessage(`Enviamos um código para ${response.emailMascarado || "seu e-mail"}.`);
    } catch (error) {
      setStep("identify"); setMessage(apiErrorMessage(error, "Não foi possível enviar o código."));
    } finally { setLoading(false); }
  }
  useEffect(() => {
    if (!fromProfile || started.current) return;
    started.current = true; void startChallenge();
  }, [fromProfile]);
  return <AccountLayout eyebrow="SEGURANÇA DA CONTA" title="Crie uma nova senha."
    description={step === "password" ? "Agora informe e confirme sua nova senha."
      : "Confirme sua identidade com o código de verificação antes de alterar a senha."}>
    {step === "identify" && <form className="page-form login-form" onSubmit={(event) => {
      event.preventDefault(); void startChallenge();
    }}>
      <label className="field"><span>E-mail</span><input type="email" autoComplete="email" value={email}
        onChange={(event) => setEmail(event.target.value)} required /></label>
      <button className="button primary" disabled={loading}>{loading ? "Enviando…" : "Enviar código"}</button>
      {message && <p className="inline-notice error-notice" role="alert">{message}</p>}
      <a className="button secondary" href={fromProfile ? "#minha-conta" : "#entrar"}>Voltar</a>
    </form>}
    {step === "code" && <form className="page-form login-form" onSubmit={async (event) => {
      event.preventDefault(); setLoading(true); setMessage("");
      try {
        const response = fromProfile ? await authApi.authorizePasswordChange(challengeId, codigo)
          : await authApi.authorizePasswordReset(challengeId, codigo);
        if (!response.token) throw new Error("TOKEN_MISSING");
        setToken(response.token); setCodigo(""); setStep("password");
      } catch (error) { setMessage(apiErrorMessage(error, "Código inválido ou expirado.")); }
      finally { setLoading(false); }
    }}>
      <p className="inline-notice">Digite o código enviado para {maskedEmail}.</p>
      <VerificationCodeField value={codigo} onChange={setCodigo} autoFocus />
      <button className="button primary" disabled={loading || codigo.length !== 6}>{loading ? "Validando…" : "Validar código"}</button>
      {message && <p className="inline-notice error-notice" role="alert">{message}</p>}
      {!fromProfile && <button className="button secondary" type="button" onClick={() => { setStep("identify"); setMessage(""); }}>
        Usar outro e-mail
      </button>}
    </form>}
    {step === "password" && <form className="page-form login-form" onSubmit={async (event) => {
      event.preventDefault(); setLoading(true); setMessage("");
      try {
        if (senha !== confirmacaoSenha) {
          setMessage("As senhas não coincidem.");
          return;
        }
        await authApi.confirmPasswordReset(token, senha, confirmacaoSenha);
        setToken(""); setSenha(""); setConfirmacaoSenha(""); setStep("done");
      } catch (error) {
        setMessage(apiErrorMessage(error, "A autorização expirou. Solicite um novo código."));
      } finally { setLoading(false); }
    }}>
      <PasswordField registration value={senha} onChange={setSenha} />
      <PasswordField confirmation label="Confirme a nova senha" value={confirmacaoSenha}
        onChange={setConfirmacaoSenha} />
      <button className="button primary" disabled={loading} type="submit">
        {loading ? "Alterando…" : "Alterar senha"}
      </button>
      {message && <p className="inline-notice" role="status">{message}</p>}
    </form>}
    {step === "done" && <div className="page-form login-form">
      <p className="inline-notice" role="status">Senha alterada com sucesso. Entre novamente com a nova senha.</p>
      <a className="button primary" href="#entrar">Ir para o login</a>
    </div>}
  </AccountLayout>;
}
