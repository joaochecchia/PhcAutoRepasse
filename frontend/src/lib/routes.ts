// Keep the existing hash navigation: each screen has a bookmarkable address,
// works after reload, and needs no server-side rewrite configuration.
export type Page =
  | "home"
  | "search"
  | "favorites"
  | "login"
  | "register"
  | "profile"
  | "announce"
  | "vehicle"
  | "not-found";
const paths: Partial<Record<Page, string>> = {
  home: "inicio",
  search: "buscar",
  favorites: "favoritos",
  login: "entrar",
  register: "cadastro",
  profile: "anunciar/dados",
  announce: "anunciar/veiculo",
};
export function readRoute(): { page: Page; id?: string } {
  const path = window.location.hash.slice(1).split("?")[0];
  if (!path) return { page: "home" };
  if (path.startsWith("anuncio/"))
    return { page: "vehicle", id: path.slice(8) };
  const found = Object.entries(paths).find(([, value]) => value === path);
  return { page: (found?.[0] as Page) || "not-found" };
}
export function pageHash(page: Page, id?: string) {
  return page === "vehicle"
    ? `anuncio/${encodeURIComponent(id || "")}`
    : paths[page] || "nao-encontrado";
}
export const pageTitles: Record<Page, string> = {
  home: "Seu próximo caminho começa aqui",
  search: "Comprar veículos",
  favorites: "Seus favoritos",
  login: "Entrar",
  register: "Criar conta",
  profile: "Dados do anunciante",
  announce: "Anunciar veículo",
  vehicle: "Detalhes do veículo",
  "not-found": "Página não encontrada",
};
