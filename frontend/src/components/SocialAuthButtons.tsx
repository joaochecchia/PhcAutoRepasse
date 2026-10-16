const configuredOAuthOrigin = import.meta.env.VITE_OAUTH_ORIGIN?.replace(/\/$/, "");

export function SocialAuthButtons({ action }: { action: "entrar" | "cadastrar" }) {
  const label = action === "entrar" ? "Entrar" : "Cadastrar";
  const dividerLabel = action === "entrar" ? "ou entre com seu e-mail" : "ou preencha seus dados";
  const start = () => {
    // OAuth must start on the same host that receives Google's callback so
    // Spring's short-lived state/session cookie is sent back correctly.
    const oauthOrigin = configuredOAuthOrigin
      ?? (import.meta.env.DEV ? "http://localhost:8080" : window.location.origin);
    window.location.assign(`${oauthOrigin}/oauth2/authorization/google`);
  };
  return <section className="social-auth" aria-label={`${label} com uma conta social`}>
    <div className="social-auth-buttons google-only">
      <button type="button" className="button social-auth-button" onClick={start}>
        <span className="social-brand google-brand" aria-hidden="true">G</span>
        {label} com Google
      </button>
    </div>
    <div className="social-auth-divider"><span>{dividerLabel}</span></div>
  </section>;
}
