import axios from "axios";

// Prepared only: no consumer calls the PHC backend in this visual prototype.
// The public ViaCEP lookup has its own client in viacep.ts. Future
// authentication must use a server-issued HttpOnly cookie. Never read cookies
// or persist tokens in JavaScript. Server must implement CSRF protection.
export const api = axios.create({
  baseURL: "/api/v1",
  withCredentials: true,
  timeout: 15_000,
  headers: { Accept: "application/json" },
});
