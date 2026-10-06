import axios from "axios";

// Authentication is carried only by the server-issued HttpOnly cookie. The
// frontend never reads or persists access tokens.
export const api = axios.create({
  baseURL: "/api/v1",
  withCredentials: true,
  timeout: 15_000,
  headers: { Accept: "application/json" },
});
