import axios from "axios";

export type ViaCepAddress = {
  cep: string;
  logradouro: string;
  complemento: string;
  bairro: string;
  localidade: string;
  uf: string;
  erro?: boolean;
};

const viacep = axios.create({
  baseURL: "https://viacep.com.br/ws",
  timeout: 8_000,
  headers: { Accept: "application/json" },
});

const cache = new Map<string, ViaCepAddress>();

export async function findAddressByCep(cep: string, signal?: AbortSignal) {
  const cached = cache.get(cep);
  if (cached) return cached;

  const { data } = await viacep.get<ViaCepAddress>(`/${cep}/json/`, {
    signal,
  });

  if (data.erro) throw new Error("CEP_NOT_FOUND");

  cache.set(cep, data);
  return data;
}
