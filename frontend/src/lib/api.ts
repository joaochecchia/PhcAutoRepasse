import axios from "axios";

// Authentication is carried only by the server-issued HttpOnly cookie. The
// frontend never reads or persists access tokens.
export const api = axios.create({
  baseURL: "/api/v1",
  withCredentials: true,
  timeout: 15_000,
  headers: { Accept: "application/json" },
});

let refreshing: Promise<void> | undefined;
api.interceptors.response.use(undefined, async (error) => {
  const request = error.config as (typeof error.config & { _refreshAttempted?: boolean }) | undefined;
  const url = request?.url ?? "";
  if (error.response?.status !== 401 || !request || request._refreshAttempted ||
      url.includes("/usuarios/login") || url.includes("/usuarios/refresh")) {
    throw error;
  }
  request._refreshAttempted = true;
  refreshing ??= api.post("/usuarios/refresh").then(() => undefined).finally(() => { refreshing = undefined; });
  await refreshing;
  return api.request(request);
});
