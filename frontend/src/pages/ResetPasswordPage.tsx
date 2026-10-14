import { useState } from "react";
import { AccountLayout } from "./PageLayout";
import { PasswordField } from "../components/FormFields";
import { apiErrorMessage, authApi } from "../lib/backend";

export function ResetPasswordPage() {
  const [senha, setSenha] = useState("");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const params = new URLSearchParams(window.location.hash.split("?")[1] || "");
  const token = params.get("token") || "";
  return <AccountLayout eyebrow="REDEFINIR SENHA" title="Crie uma nova senha."
    description="O link é de uso único e expira em poucos minutos.">
    <form className="page-form login-form" onSubmit={async (event) => {
      event.preventDefault(); setLoading(true); setMessage("");
      try {
        if (!token) throw new Error("TOKEN_AUSENTE");
        await authApi.confirmPasswordReset(token, senha);
        setMessage("Senha alterada. Você já pode entrar novamente.");
      } catch (error) {
        setMessage(apiErrorMessage(error, "Link inválido ou expirado."));
      } finally { setLoading(false); }
    }}>
      <PasswordField registration value={senha} onChange={setSenha} />
      <button className="button primary" disabled={loading || !token} type="submit">
        {loading ? "Alterando…" : "Alterar senha"}
      </button>
      {message && <p className="inline-notice" role="status">{message}</p>}
      <a className="button secondary" href="#entrar">Voltar para o login</a>
    </form>
  </AccountLayout>;
}
