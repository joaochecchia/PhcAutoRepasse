export type Session = {
  autenticado: boolean;
  usuarioId: string | null;
  papel: string | null;
  expiresIn: number;
};

export type Address = {
  cep: string;
  cidade: string;
  bairro: string;
  rua: string;
  numero?: string | null;
  complemento?: string | null;
  uf: string;
};

export type UserProfile = {
  id: string;
  tipoPessoa: "PF" | "PJ";
  nome: string;
  email: string;
  telefone: string;
  cpf?: string | null;
  dataNascimento?: string | null;
  cnpj?: string | null;
  razaoSocial?: string | null;
  endereco: Address;
};

export type SearchAd = {
  anuncioId: string;
  veiculoId: string;
  anuncianteId: string;
  tipoVeiculo: string;
  fabricante: string;
  modelo: string;
  anoFabricacao: number;
  anoModelo: number;
  condicao: string;
  titulo: string;
  tipoPreco: "FIXO" | "SOB_CONSULTA";
  precoCentavos: number | null;
  cidade: string;
  uf: string;
  nomePerfil: string;
  cambio?: string | null;
  combustivel?: string | null;
  motorizacao?: string | null;
  historicoLeilao?: boolean | null;
  historicoSinistro?: boolean | null;
  fotoPrincipalUrl?: string | null;
};

export type SearchResponse = {
  carros: SearchAd[];
  total: number;
  pagina: number;
  tamanho: number;
  raioKmAplicado: number | null;
  temProximaPagina: boolean;
};

export type HomeAdsResponse = {
  carros: SearchAd[];
  mensagem: string;
};

export type AdPhoto = {
  id: string;
  anuncioId: string;
  posicao: number;
  textoAlternativo?: string | null;
  url: string;
};
