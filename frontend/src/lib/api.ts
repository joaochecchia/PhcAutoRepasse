import axios from "axios";

// Authentication is carried only by the server-issued HttpOnly cookie. The
// frontend never reads or persists access tokens.
export const api = axios.create({
  baseURL: "/api/v1",
  withCredentials: true,
  withXSRFToken: true,
  xsrfCookieName: "XSRF-TOKEN",
  xsrfHeaderName: "X-XSRF-TOKEN",
  timeout: 15_000,
  headers: { Accept: "application/json" },
});

const csrfBootstrapApi = axios.create({
  baseURL: "/api/v1",
  withCredentials: true,
  timeout: 15_000,
  headers: { Accept: "application/json" },
});
let csrfBootstrap: Promise<void> | undefined;

api.interceptors.request.use(async (config) => {
  const method = (config.method ?? "get").toLowerCase();
  if (!["post", "put", "patch", "delete"].includes(method)) return config;

  let token = csrfCookie();
  if (!token) {
    csrfBootstrap ??= csrfBootstrapApi
      .get("/usuarios/sessao")
      .then(() => undefined)
      .finally(() => { csrfBootstrap = undefined; });
    await csrfBootstrap;
    token = csrfCookie();
  }
  if (token) config.headers.set("X-XSRF-TOKEN", token);
  return config;
});

function csrfCookie() {
  if (typeof document === "undefined") return undefined;
  const prefix = "XSRF-TOKEN=";
  const value = document.cookie.split("; ").find((cookie) => cookie.startsWith(prefix));
  return value ? decodeURIComponent(value.slice(prefix.length)) : undefined;
}

let refreshing: Promise<void> | undefined;
api.interceptors.response.use(undefined, async (error) => {
  const request = error.config as (typeof error.config & { _refreshAttempted?: boolean }) | undefined;
  const url = request?.url ?? "";
  if (error.response?.status !== 401 || !request || request._refreshAttempted ||
      url.includes("/autenticacao/login") || url.includes("/autenticacao/2fa") ||
      url.includes("/credenciais/redefinicoes") || url.includes("/usuarios/refresh")) {
    throw error;
  }
  request._refreshAttempted = true;
  refreshing ??= api.post("/usuarios/refresh").then(() => undefined).finally(() => { refreshing = undefined; });
  await refreshing;
  return api.request(request);
});
