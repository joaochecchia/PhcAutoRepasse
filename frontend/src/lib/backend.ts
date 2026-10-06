import axios from "axios";
import { api } from "./api";
import type { Draft } from "../components/FormFields";
import type { Filters } from "../data/filters";
import type { Address, SearchResponse, Session, UserProfile } from "./contracts";
import type { Vehicle } from "../data/catalog";

type RegistrationConfig = {
  versaoTermosUso: string;
  versaoPoliticaPrivacidade: string;
};

let pendingSession: Promise<Session> | undefined;
let registrationConfig: RegistrationConfig | undefined;
let pendingRegistrationConfig: Promise<RegistrationConfig> | undefined;

export function apiErrorMessage(error: unknown, fallback: string) {
  if (!axios.isAxiosError(error)) return fallback;
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
    const position = await currentPosition();
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
  index: number,
): Vehicle {
  const price =
    ad.tipoPreco === "SOB_CONSULTA" || ad.precoCentavos == null
      ? "Sob consulta"
      : new Intl.NumberFormat("pt-BR", {
          minimumFractionDigits: 0,
          maximumFractionDigits: 0,
        }).format(ad.precoCentavos / 100);
  const fallbackImages = ["porsche", "bmw", "mercedes", "audi", "golf", "toyota", "mustang", "jeep"];
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
    image: fallbackImages[index % fallbackImages.length],
    fuel: ad.combustivel || "Não informado",
    transmission: ad.cambio || "Não informado",
    seller: ad.nomePerfil || "Anunciante",
    live: true,
    condition: ad.condicao,
    type: ad.tipoVeiculo,
  };
}

export async function createAd(payload: unknown) {
  return (await api.post<{ id: string }>("/anuncios", payload)).data;
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
    titulo: values.titulo,
    descricao: values.descricao || null,
    tipoPreco: values.tipoPreco,
    precoCentavos:
      values.tipoPreco === "FIXO"
        ? Math.round(Number(values.precoReais) * 100)
        : null,
    aceitaTroca: optionalBoolean(values.aceitaTroca),
    publicarAgora: optionalBoolean(values.publicarAgora) ?? false,
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

function currentPosition() {
  return new Promise<GeolocationPosition>((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error("Seu navegador não oferece localização."));
      return;
    }
    navigator.geolocation.getCurrentPosition(resolve, reject, {
      enableHighAccuracy: false,
      timeout: 8_000,
      maximumAge: 300_000,
    });
  });
}
