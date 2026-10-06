// Field names follow BuscarAnunciosRequest.java. Values are form state only;
// validation, proximity, permissions and result selection belong to the server.
export type FilterField = {
  key: string;
  label: string;
  options?: [string, string][];
  type?: string;
  placeholder?: string;
};
export const filterGroups: { title: string; fields: FilterField[] }[] = [
  {
    title: "Localização",
    fields: [
      {
        key: "modoLocalizacao",
        label: "Onde procurar",
        options: [
          ["BRASIL", "Todo o Brasil"],
          ["CIDADE", "Por cidade"],
          ["UF", "Por estado"],
          ["DISPOSITIVO", "Minha localização"],
          ["ENDERECO_CADASTRADO", "Meu endereço cadastrado"],
        ],
      },
      { key: "cidade", label: "Cidade", placeholder: "Digite a cidade" },
      {
        key: "uf",
        label: "Estado",
        options:
          "AC AL AP AM BA CE DF ES GO MA MT MS MG PA PB PR PE PI RJ RN RS RO RR SC SP SE TO"
            .split(" ")
            .map((s) => [s, s]),
      },
    ],
  },
  {
    title: "Marca e condição",
    fields: [
      {
        key: "marca",
        label: "Marca",
        options: [
          "Volkswagen",
          "Chevrolet",
          "Fiat",
          "Toyota",
          "Honda",
          "Jeep",
          "BMW",
          "Hyundai",
          "Porsche",
          "Audi",
          "Mercedes-Benz",
          "Ford",
          "Renault",
          "Nissan",
          "BYD",
        ].map((s) => [s, s]),
      },
      {
        key: "modelo",
        label: "Modelo",
        placeholder: "Ex.: Corolla, HB20, Polo",
      },
      {
        key: "condicao",
        label: "Condição",
        options: [
          ["ZERO_KM", "Zero km"],
          ["USADO", "Usado"],
        ],
      },
    ],
  },
  {
    title: "Preço e ano",
    fields: [
      {
        key: "precoMinimoCentavos",
        label: "Preço mínimo (R$)",
        type: "number",
        placeholder: "De",
      },
      {
        key: "precoMaximoCentavos",
        label: "Preço máximo (R$)",
        type: "number",
        placeholder: "Até",
      },
      {
        key: "anoMinimo",
        label: "Ano mínimo",
        type: "number",
        placeholder: "De",
      },
      {
        key: "anoMaximo",
        label: "Ano máximo",
        type: "number",
        placeholder: "Até",
      },
    ],
  },
  {
    title: "Motor e transmissão",
    fields: [
      {
        key: "cambio",
        label: "Câmbio",
        options: ["Automático", "Manual", "Automatizado", "CVT"].map((s) => [
          s,
          s,
        ]),
      },
      {
        key: "combustivel",
        label: "Combustível",
        options: [
          "Flex",
          "Gasolina",
          "Diesel",
          "Elétrico",
          "Híbrido",
          "Etanol",
        ].map((s) => [s, s]),
      },
      {
        key: "motorizacao",
        label: "Motorização",
        placeholder: "Ex.: 1.0 Turbo",
      },
      {
        key: "cilindradaLitros",
        label: "Cilindrada (litros)",
        type: "number",
        placeholder: "Ex.: 2.0",
      },
      {
        key: "tracao",
        label: "Tração",
        options: ["Dianteira", "Traseira", "Integral", "4x2", "4x4"].map(
          (s) => [s, s],
        ),
      },
    ],
  },
  {
    title: "Características",
    fields: [
      {
        key: "carroceria",
        label: "Carroceria",
        placeholder: "Ex.: SUV, sedã, hatch",
      },
      {
        key: "tipoDirecao",
        label: "Direção",
        options: [
          "Elétrica",
          "Hidráulica",
          "Eletro-hidráulica",
          "Mecânica",
        ].map((s) => [s, s]),
      },
      { key: "tipoFreio", label: "Freios", placeholder: "Ex.: Disco" },
      { key: "numeroPortas", label: "Número de portas", type: "number" },
      {
        key: "ipvaPago",
        label: "IPVA pago",
        options: [
          ["true", "Sim"],
          ["false", "Não"],
        ],
      },
      {
        key: "blindado",
        label: "Blindado",
        options: [
          ["true", "Sim"],
          ["false", "Não"],
        ],
      },
    ],
  },
  {
    title: "Anunciante",
    fields: [
      {
        key: "tipoPessoa",
        label: "Tipo de anunciante",
        options: [
          ["PF", "Pessoa física"],
          ["PJ", "Pessoa jurídica"],
        ],
      },
      {
        key: "perfil",
        label: "Nome da pessoa ou loja",
        placeholder: "Buscar anunciante",
      },
    ],
  },
];
export type Filters = Record<string, string>;
