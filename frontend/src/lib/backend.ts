import axios from "axios";
import { api } from "./api";
import type { Draft } from "../components/FormFields";
import type { Filters } from "../data/filters";
import type { AdPhoto, Address, HomeAdsResponse, SearchResponse, Session, StrongAuthResponse, UserProfile } from "./contracts";
import type { Vehicle } from "../data/catalog";

type RegistrationConfig = {
  versaoTermosUso: string;
  versaoPoliticaPrivacidade: string;
};
type PasswordChangeResponse = {
  status: "AUTORIZADA" | "SEGUNDO_FATOR_NECESSARIO";
  challengeId: string | null;
  challengeExpiresIn: number;
  emailMascarado: string | null;
  token: string | null;
};

export type AdFormCatalog = {
  fabricantes: { valor: string; aliases: string[] }[];
  modelosPorFabricante: Record<string, string[]>;
  cores: string[];
  carrocerias: string[];
  cambios: string[];
  combustiveis: string[];
  motorizacoes: string[];
  tiposFreio: string[];
  tracoes: string[];
  direcoes: string[];
  fabricantesMotos: { valor: string; aliases: string[] }[];
  modelosMotosPorFabricante: Record<string, string[]>;
  categoriasMotos: string[];
  partidasMotos: string[];
  refrigeracoesMotos: string[];
  cambiosMotos: string[];
  tiposFreioMotos: string[];
  fabricantesCaminhoes: { valor: string; aliases: string[] }[];
  modelosCaminhoesPorFabricante: Record<string, string[]>;
  configuracoesCaminhao: string[];
  carroceriasCaminhao: string[];
  cambiosCaminhao: string[];
  tracoesCaminhao: string[];
  direcoesCaminhao: string[];
  tiposFreioCaminhao: string[];
  implementosCaminhao: string[];
};

export type FipeOption = {
  codigo: string;
  nome: string;
  mesReferencia: string | null;
  /** Ano/combustível FIPE (ex.: 2020-5); só nos modelos listados por ano, usado para consultar o preço. */
  anoId?: string | null;
};

export type FipeReference = {
  id: string;
  codigoFipe: string;
  tipo: "CARRO" | "MOTO" | "CAMINHAO";
  marca: string;
  modelo: string;
  anoModelo: number;
  zeroKm: boolean;
  combustivel: string;
  mesReferencia: string;
  valorCentavos: number;
  fonte: string;
};

export type FipeOptionsResponse = {
  opcoes: FipeOption[];
  pagina: number;
  tamanho: number;
  temProximaPagina: boolean;
  /** O backend ainda está carregando este nível do catálogo FIPE em segundo plano. */
  catalogoPendente: boolean;
};

let pendingSession: Promise<Session> | undefined;
let registrationConfig: RegistrationConfig | undefined;
let pendingRegistrationConfig: Promise<RegistrationConfig> | undefined;

export function apiErrorMessage(error: unknown, fallback: string) {
  if (!axios.isAxiosError(error)) return fallback;
  if (
    error.response?.status === 401 &&
    !error.config?.url?.includes("/autenticacao/login") &&
    !error.config?.url?.includes("/autenticacao/2fa")
  ) {
    return "Sua sessão expirou. Entre novamente.";
  }
  const data = error.response?.data as
    | { detail?: string; title?: string; errors?: Record<string, string> }
    | undefined;
  if (data?.detail) return data.detail;
  if (data?.title) return data.title;
  if (!error.response) return "Não foi possível conectar ao servidor.";
  return fallback;
}

export const authApi = {
  async registrationConfig() {
    if (registrationConfig) return registrationConfig;
    pendingRegistrationConfig ??= api
      .get<RegistrationConfig>("/compliance/documentos-vigentes")
      .then((response) => {
        registrationConfig = response.data;
        return response.data;
      })
      .finally(() => {
        pendingRegistrationConfig = undefined;
      });
    return pendingRegistrationConfig;
  },
  async session() {
    pendingSession ??= api
      .get<Session>("/usuarios/sessao")
      .then((response) => response.data)
      .finally(() => {
        pendingSession = undefined;
      });
    return pendingSession;
  },
  async login(email: string, senha: string) {
    return (await api.post<StrongAuthResponse>("/autenticacao/login", { email, senha })).data;
  },
  async verifySecondFactor(challengeId: string, codigo: string, confiarNesteDispositivo: boolean) {
    return (await api.post<StrongAuthResponse>("/autenticacao/2fa/verificar", {
      challengeId, codigo, confiarNesteDispositivo,
    })).data;
  },
  async resendSecondFactor(challengeId: string) {
    return (await api.post<StrongAuthResponse>("/autenticacao/2fa/reenviar", { challengeId })).data;
  },
  asSession(response: StrongAuthResponse): Session {
    return { autenticado: response.status === "AUTENTICADO", usuarioId: response.usuarioId,
      papel: response.papel, expiresIn: response.expiresIn };
  },
  async requestPasswordReset(email: string) {
    return (await api.post<PasswordChangeResponse>("/credenciais/redefinicoes", { email })).data;
  },
  async authorizePasswordReset(challengeId: string, codigo: string) {
    return (await api.post<PasswordChangeResponse>("/credenciais/redefinicoes/autorizar", { challengeId, codigo })).data;
  },
  async confirmPasswordReset(token: string, novaSenha: string, confirmacaoSenha: string) {
    await api.post("/credenciais/redefinicoes/confirmar", { token, novaSenha, confirmacaoSenha });
  },
  async logout() {
    await api.post("/usuarios/logout");
  },
  async register(
    values: Draft,
    versions: { versaoTermosUso: string; versaoPoliticaPrivacidade: string },
  ) {
    const payload = {
      tipoPessoa: values.tipoPessoa,
      nome: values.nome,
      email: values.email,
      telefone: onlyDigits(values.telefone),
      senha: values.senha,
      confirmacaoSenha: values.confirmacaoSenha,
      cpf: values.tipoPessoa === "PF" ? onlyDigits(values.cpf) : undefined,
      dataNascimento:
        values.tipoPessoa === "PF" ? values.dataNascimento : undefined,
      cnpj: values.tipoPessoa === "PJ" ? onlyDigits(values.cnpj) : undefined,
      razaoSocial:
        values.tipoPessoa === "PJ" ? values.razaoSocial : undefined,
      endereco: addressFromDraft(values),
      aceitouTermos: true,
      aceiteTermosEm: new Date().toISOString(),
      versaoTermosUso: versions.versaoTermosUso,
      versaoPoliticaPrivacidade: versions.versaoPoliticaPrivacidade,
    };
    return (await api.post<{ id: string }>("/usuarios/registrar", payload)).data;
  },
  async socialRegistrationInfo(token: string) {
    return (await api.get<{ provedor: string; nome: string; email: string }>(
      `/autenticacao/social/cadastro/${encodeURIComponent(token)}`,
    )).data;
  },
  async socialRegister(
    token: string,
    values: Draft,
    versions: { versaoTermosUso: string; versaoPoliticaPrivacidade: string },
  ) {
    const cadastro = {
      tipoPessoa: values.tipoPessoa,
      nome: values.nome,
      email: values.email,
      telefone: onlyDigits(values.telefone),
      cpf: values.tipoPessoa === "PF" ? onlyDigits(values.cpf) : undefined,
      dataNascimento: values.tipoPessoa === "PF" ? values.dataNascimento : undefined,
      cnpj: values.tipoPessoa === "PJ" ? onlyDigits(values.cnpj) : undefined,
      razaoSocial: values.tipoPessoa === "PJ" ? values.razaoSocial : undefined,
      endereco: addressFromDraft(values),
      aceitouTermos: true,
      aceiteTermosEm: new Date().toISOString(),
      versaoTermosUso: versions.versaoTermosUso,
      versaoPoliticaPrivacidade: versions.versaoPoliticaPrivacidade,
    };
    return (await api.post<StrongAuthResponse>("/autenticacao/social/cadastro", { token, cadastro })).data;
  },
  async profile(usuarioId: string) {
    return (await api.get<UserProfile>(`/usuarios/${usuarioId}`)).data;
  },
  async updateProfile(usuarioId: string, values: Draft) {
    const payload = {
      nome: values.nome,
      email: values.email,
      telefone: onlyDigits(values.telefone),
      cpf: values.tipoPessoa === "PF" ? onlyDigits(values.cpf) : undefined,
      dataNascimento:
        values.tipoPessoa === "PF" ? values.dataNascimento : undefined,
      cnpj: values.tipoPessoa === "PJ" ? onlyDigits(values.cnpj) : undefined,
      razaoSocial:
        values.tipoPessoa === "PJ" ? values.razaoSocial : undefined,
      endereco: addressFromDraft(values),
    };
    return (await api.patch<UserProfile>(`/usuarios/${usuarioId}`, payload)).data;
  },
  async requestPasswordChange() {
    return (await api.post<PasswordChangeResponse>("/credenciais/senha/desafio")).data;
  },
  async authorizePasswordChange(challengeId: string, codigo: string) {
    return (await api.post<PasswordChangeResponse>("/credenciais/senha/autorizar", {
      challengeId, codigo,
    })).data;
  },
};

const pendingSearches = new Map<string, Promise<SearchResponse>>();

export async function homeAds() {
  return (await api.get<HomeAdsResponse>("/anuncios/pagina-inicial")).data;
}

export async function searchAds(filters: Filters, pagina: number, tamanho: 20 | 52) {
  const params: Record<string, string | number> = { pagina, tamanho };
  for (const [key, value] of Object.entries(filters)) {
    if (!value) continue;
    if (key === "precoMinimoCentavos" || key === "precoMaximoCentavos") {
      params[key] = Math.round(Number(value) * 100);
    } else {
      params[key] = value;
    }
  }
  if (params.modoLocalizacao === "CIDADE" && (!params.cidade || !params.uf)) {
    delete params.modoLocalizacao;
    delete params.cidade;
    delete params.uf;
  }
  if (params.modoLocalizacao === "DISPOSITIVO") {
    const position = await prepareDeviceLocation();
    params.latitude = position.coords.latitude;
    params.longitude = position.coords.longitude;
  }
  const key = JSON.stringify(
    Object.entries(params).sort(([left], [right]) => left.localeCompare(right)),
  );
  const pending = pendingSearches.get(key);
  if (pending) return pending;

  const request = api
    .get<SearchResponse>("/anuncios", { params })
    .then((response) => response.data)
    .finally(() => pendingSearches.delete(key));
  pendingSearches.set(key, request);
  return request;
}

export function searchAdToVehicle(
  ad: SearchResponse["carros"][number],
  _index: number,
): Vehicle {
  const price =
    ad.tipoPreco === "SOB_CONSULTA" || ad.precoCentavos == null
      ? "Sob consulta"
      : new Intl.NumberFormat("pt-BR", {
          minimumFractionDigits: 0,
          maximumFractionDigits: 0,
        }).format(ad.precoCentavos / 100);
  return {
    id: ad.anuncioId,
    brand: ad.fabricante.toUpperCase(),
    model: ad.modelo,
    version: ad.motorizacao || ad.titulo,
    price,
    year: `${ad.anoFabricacao} / ${ad.anoModelo}`,
    location:
      ad.cidade && ad.uf
        ? `${ad.cidade}, ${ad.uf}`
        : ad.cidade || ad.uf || "Localização não informada",
    imageUrl: ad.fotoPrincipalUrl ?? null,
    fuel: ad.combustivel || "Não informado",
    transmission: ad.cambio || "Não informado",
    seller: ad.nomePerfil || "Anunciante",
    sellerId: ad.anuncianteId,
    live: true,
    condition: ad.condicao,
    type: ad.tipoVeiculo,
    auctionHistory: ad.historicoLeilao,
    accidentHistory: ad.historicoSinistro,
  };
}

export async function createAd(payload: unknown) {
  return (await api.post<{ id: string }>("/anuncios", payload)).data;
}

export async function getAdFormCatalog() {
  return (await api.get<AdFormCatalog>("/anuncios/opcoes-cadastro")).data;
}

export type FipeOptionsQuery = {
  tipo?: "cars" | "motorcycles" | "trucks";
  marcaId?: string; modeloId?: string; referencia?: number; termo?: string; pagina?: number;
  /** true: anos-modelo da marca. */
  anos?: boolean;
  /** Modelos da marca neste ano-modelo (32000 = zero km), somando os combustíveis. */
  ano?: number;
};
const fipePages = new Map<string, { expires: number; page: FipeOptionsResponse }>();
export async function getFipePage(params: FipeOptionsQuery, signal?: AbortSignal) {
  const key = JSON.stringify(params);
  const cached = fipePages.get(key);
  if (cached && cached.expires > Date.now()) return cached.page;
  const page = (await api.get<FipeOptionsResponse>("/fipe/catalogo/opcoes", {
    params: { ...params, tamanho: 50 }, signal,
  })).data;
  if (fipePages.size >= 100) fipePages.delete(fipePages.keys().next().value!);
  if (page.opcoes.length && !page.catalogoPendente) fipePages.set(key, { page, expires: Date.now() + 60000 });
  return page;
}
export async function getFipeActiveEdition(tipo: "cars" | "motorcycles" | "trucks", signal?: AbortSignal) {
  return (await api.get<FipeOption>("/fipe/catalogo/edicao-ativa", { params: { tipo }, signal })).data;
}
export async function resolveFipeReference(input: {
  tipo: "cars" | "motorcycles" | "trucks";
  marcaId: string;
  modeloId: string;
  anoId: string;
  referencia: number;
  mesReferencia: string;
}, signal?: AbortSignal) {
  return (await api.post<FipeReference>("/fipe/consultas", input, { signal })).data;
}

export async function uploadAdPhoto(
  anuncioId: string,
  file: File,
  position: number,
  alt: string,
) {
  const data = new FormData();
  data.append("arquivo", file);
  data.append("posicao", String(position));
  if (alt.trim()) data.append("textoAlternativo", alt.trim());
  await api.post(`/anuncios/${anuncioId}/fotos`, data);
}

export async function listAdPhotos(anuncioId: string) {
  return (await api.get<AdPhoto[]>(`/anuncios/${anuncioId}/fotos`)).data;
}

export async function requestWhatsappContact(anuncioId: string) {
  return (await api.post<{ url: string }>(`/anuncios/${anuncioId}/contato/whatsapp`)).data;
}

export function adPayloadFromDraft(
  values: Draft,
  motors: Draft[],
  usuarioId: string,
  fipeReferenciaId?: string,
) {
  const tipo = values.tipoVeiculo;
  const details: Record<string, unknown> = Object.fromEntries(
    Object.entries(values)
      .filter(([key, value]) => key.startsWith(`${tipo}.`) && value !== "")
      .map(([key, value]) => [
        key.slice(tipo.length + 1),
        typedAdValue(key.slice(tipo.length + 1), value),
      ]),
  );
  if (tipo === "BARCO") {
    details.motores = motors.map((motor) =>
      Object.fromEntries(
        Object.entries(motor)
          .filter(([, value]) => value !== "")
          .map(([key, value]) => [key, typedAdValue(key, value)]),
      ),
    );
  }
  // The API stores the engine designation as one string; the form presents turbo separately.
  if (typeof details.motorizacao === "string") {
    details.motorizacao = details.motorizacao.replace(",", ".")
      + (values[`${tipo}.turbo`] === "true" ? " Turbo" : "");
  }
  delete details.turbo;
  return {
    anuncianteId: usuarioId,
    tipoVeiculo: tipo,
    fabricante: values.fabricante,
    modelo: values.modelo,
    versao: values.versao || null,
    anoFabricacao: Number(values.anoFabricacao),
    anoModelo: Number(values.anoModelo),
    cor: values.cor || null,
    identificadorPublico: values.identificadorPublico || null,
    condicao: values.condicao,
    tipoFreio: values.tipoFreio || null,
    historicoLeilao: optionalBoolean(values.historicoLeilao),
    historicoSinistro: optionalBoolean(values.historicoSinistro),
    titulo: values.titulo,
    descricao: values.descricao || null,
    tipoPreco: values.tipoPreco,
    precoCentavos:
      values.tipoPreco === "FIXO"
        ? Math.round(Number(values.precoReais) * 100)
        : null,
    aceitaTroca: optionalBoolean(values.aceitaTroca),
    publicarAgora: optionalBoolean(values.publicarAgora) ?? false,
    fipeReferenciaId: fipeReferenciaId || null,
    contato: {
      whatsapp: values.contatoWhatsapp === "true",
      ligacao: values.contatoLigacao === "true",
      naoDivulgar: values.contatoNaoDivulgar === "true",
      versaoTexto: values.contatoNaoDivulgar === "true" ? null : "1.0",
    },
    endereco: addressFromDraft(values),
    carro: tipo === "CARRO" ? details : null,
    moto: tipo === "MOTO" ? details : null,
    caminhao: tipo === "CAMINHAO" ? details : null,
    caminhonete: tipo === "CAMINHONETE" ? details : null,
    barco: tipo === "BARCO" ? details : null,
    linhaAmarela: tipo === "LINHA_AMARELA" ? details : null,
  };
}

export function profileToDraft(profile: UserProfile): Draft {
  return {
    tipoPessoa: profile.tipoPessoa,
    nome: profile.nome || "",
    email: profile.email || "",
    telefone: profile.telefone || "",
    cpf: profile.cpf || "",
    dataNascimento: profile.dataNascimento || "",
    cnpj: profile.cnpj || "",
    razaoSocial: profile.razaoSocial || "",
    "endereco.cep": profile.endereco?.cep || "",
    "endereco.cidade": profile.endereco?.cidade || "",
    "endereco.bairro": profile.endereco?.bairro || "",
    "endereco.rua": profile.endereco?.rua || "",
    "endereco.numero": profile.endereco?.numero || "",
    "endereco.complemento": profile.endereco?.complemento || "",
    "endereco.uf": profile.endereco?.uf || "",
  };
}

export function addressFromDraft(values: Draft): Address {
  return {
    cep: onlyDigits(values["endereco.cep"]),
    cidade: values["endereco.cidade"],
    bairro: values["endereco.bairro"],
    rua: values["endereco.rua"],
    numero: values["endereco.numero"] || null,
    complemento: values["endereco.complemento"] || null,
    uf: values["endereco.uf"],
  };
}

const onlyDigits = (value = "") => value.replace(/\D/g, "");

const numericAdFields = new Set([
  "quilometragem", "cilindradaLitros", "cilindradas", "numeroPortas",
  "numeroLugares", "numeroEixos", "capacidadeCargaKg", "pesoBrutoTotalKg",
  "tamanhoPes", "capacidadePessoas", "numeroCabines", "horasUso", "horimetro",
  "pesoOperacionalKg", "potenciaHp", "capacidadeCacambaM3", "posicao", "ano",
  "horasDesdeRevisao",
]);
const booleanAdFields = new Set([
  "exibirPlacaCompleta", "ipvaPago", "licenciado", "unicoDono", "blindado",
]);
function typedAdValue(key: string, value: string) {
  if (numericAdFields.has(key)) return Number(value);
  if (booleanAdFields.has(key)) return value === "true";
  return value;
}
function optionalBoolean(value?: string) {
  return value === undefined || value === "" ? null : value === "true";
}

let cachedPosition: { position: GeolocationPosition; capturedAt: number } | null = null;
let pendingPosition: Promise<GeolocationPosition> | null = null;

export function prepareDeviceLocation() {
  if (cachedPosition && Date.now() - cachedPosition.capturedAt < 300_000) {
    return Promise.resolve(cachedPosition.position);
  }
  if (pendingPosition) return pendingPosition;
  pendingPosition = new Promise<GeolocationPosition>((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error("Seu navegador não oferece localização."));
      return;
    }
    navigator.geolocation.getCurrentPosition(resolve, reject, {
      enableHighAccuracy: false,
      timeout: 8_000,
      maximumAge: 300_000,
    });
  }).then((position) => {
    cachedPosition = { position, capturedAt: Date.now() };
    return position;
  }).finally(() => {
    pendingPosition = null;
  });
  return pendingPosition;
}

export async function searchStores(
  location: (({ cidade: string; uf: string } | { latitude: number; longitude: number }) & { nome?: string }) | { nome?: string },
  pagina = 0,
) {
  return (await api.get<import("./contracts").StoresResponse>("/lojistas", {
    params: { ...location, pagina, tamanho: 12 },
  })).data;
}
export function currentDeviceLocation() {
  return new Promise<GeolocationPosition>((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error("Seu navegador não oferece localização."));
      return;
    }
    navigator.geolocation.getCurrentPosition(resolve, reject, {
      enableHighAccuracy: false,
      timeout: 10_000,
      maximumAge: 0,
    });
  });
}
export async function publicProfile(id: string) {
  return (await api.get<import("./contracts").PublicProfile>(`/perfis/${id}`)).data;
}
export async function uploadProfilePhoto(id: string, file: File) {
  const data = new FormData(); data.append("arquivo", file);
  await api.put(`/usuarios/${id}/foto`, data);
}
