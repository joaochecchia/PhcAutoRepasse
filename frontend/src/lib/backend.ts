import axios from "axios";
import { api } from "./api";
import type { Draft } from "../components/FormFields";
import type { Filters } from "../data/filters";
import type { AdPhoto, Address, HomeAdsResponse, SearchResponse, Session, UserProfile } from "./contracts";
import type { Vehicle } from "../data/catalog";

type RegistrationConfig = {
  versaoTermosUso: string;
  versaoPoliticaPrivacidade: string;
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

let pendingSession: Promise<Session> | undefined;
let registrationConfig: RegistrationConfig | undefined;
let pendingRegistrationConfig: Promise<RegistrationConfig> | undefined;

export function apiErrorMessage(error: unknown, fallback: string) {
  if (!axios.isAxiosError(error)) return fallback;
  if (
    error.response?.status === 401 &&
    !error.config?.url?.includes("/usuarios/login")
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
    return (await api.post<Session>("/usuarios/login", { email, senha })).data;
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
