// Keep the existing hash navigation: each screen has a bookmarkable address,
// works after reload, and needs no server-side rewrite configuration.
export type Page =
  | "stores"
  | "store"
  | "home"
  | "search"
  | "favorites"
  | "login"
  | "register"
  | "reset-password"
  | "account"
  | "profile"
  | "announce"
  | "vehicle"
  | "not-found";
const paths: Partial<Record<Page, string>> = {
  stores: "lojistas",
  home: "inicio",
  search: "buscar",
  favorites: "favoritos",
  login: "entrar",
  register: "cadastro",
  "reset-password": "redefinir-senha",
  account: "minha-conta",
  profile: "anunciar/dados",
  announce: "anunciar/veiculo",
};
export function readRoute(): { page: Page; id?: string } {
  const path = window.location.hash.slice(1).split("?")[0];
  if (!path) return { page: "home" };
  if (path.startsWith("loja/")) return { page: "store", id: path.slice(5) };
  if (path.startsWith("anuncio/"))
    return { page: "vehicle", id: path.slice(8) };
  const found = Object.entries(paths).find(([, value]) => value === path);
  return { page: (found?.[0] as Page) || "not-found" };
}
export function pageHash(page: Page, id?: string) {
  if (page === "store") return `loja/${encodeURIComponent(id || "")}`;
  return page === "vehicle"
    ? `anuncio/${encodeURIComponent(id || "")}`
    : paths[page] || "nao-encontrado";
}
export const pageTitles: Record<Page, string> = {
  stores: "Encontrar lojistas",
  store: "Veículos da loja",
  home: "Seu próximo caminho começa aqui",
  search: "Comprar veículos",
  favorites: "Seus favoritos",
  login: "Entrar",
  register: "Criar conta",
  "reset-password": "Redefinir senha",
  account: "Meu perfil",
  profile: "Dados do anunciante",
  announce: "Anunciar veículo",
  vehicle: "Detalhes do veículo",
  "not-found": "Página não encontrada",
};
