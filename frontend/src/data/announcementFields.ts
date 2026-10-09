import type { Field } from "../components/FormFields";

// Presentation of the current CriarAnuncioRequest fields. These descriptors do
// not validate documents, prices, publication permissions or domain invariants.
const number = (key: string, label: string, required = false): Field => ({
  key,
  label,
  type: "number",
  min: key === "quilometragem" || key === "horimetro" ? 0 : 1,
  step: ["cilindradaLitros", "tamanhoPes", "potenciaHp", "capacidadeCacambaM3"].includes(key) ? "any" : 1,
  required,
});
const text = (key: string, label: string, required = false): Field => ({
  key,
  label,
  required,
});
const yesNo = (key: string, label: string, required = false): Field => ({
  key,
  label,
  required,
  options: [
    ["true", "Sim"],
    ["false", "Não"],
  ],
});
const mileage = number("quilometragem", "Quilometragem (km)", true);
const transmission = text("cambio", "Câmbio", true);
const fuel = text("combustivel", "Combustível", true);
const body = text("carroceria", "Carroceria", true);
const engine: Field = { ...text("motorizacao", "Motorização", true), pattern: "[0-9]{1,2}[.,][0-9]{1,2}", placeholder: "Ex.: 4.1", hint: "Informe a capacidade em litros, como 1.0 ou 4.1. Turbo é informado separadamente." };
const turbo = yesNo("turbo", "Turbo?");
const displacement = { ...number("cilindradaLitros", "Cilindrada (litros)"), min: 0.01 };
const doors = number("numeroPortas", "Número de portas");
const traction = text("tracao", "Tração");
const steering = text("tipoDirecao", "Tipo de direção");
const roadDocuments: Field[] = [
  {
    key: "placa",
    label: "Placa completa",
    required: true,
    placeholder: "Ex.: ABC1D23",
    pattern: "[A-Za-z]{3}[0-9][A-Za-z0-9][0-9]{2}",
    maxLength: 7,
  },
  yesNo("exibirPlacaCompleta", "Exibir placa completa no anúncio?", true),
  yesNo("ipvaPago", "IPVA pago?", true),
  yesNo("licenciado", "Veículo licenciado?"),
];
export const vehicleFields: Record<string, Field[]> = {
  CARRO: [
    mileage,
    body,
    transmission,
    fuel,
    engine,
    turbo,
    displacement,
    doors,
    number("numeroLugares", "Número de lugares"),
    traction,
    steering,
    ...roadDocuments,
    yesNo("unicoDono", "Único dono?", true),
    yesNo("blindado", "Blindado?"),
  ],
  MOTO: [
    mileage,
    number("cilindradas", "Cilindrada (cc)"),
    text("categoria", "Categoria", true),
    transmission,
    fuel,
    text("partida", "Tipo de partida"),
    text("refrigeracao", "Refrigeração"),
    ...roadDocuments,
  ],
  CAMINHAO: [
    mileage,
    text("configuracao", "Configuração", true),
    body,
    transmission,
    fuel,
    number("numeroEixos", "Número de eixos", true),
    traction,
    steering,
    number("capacidadeCargaKg", "Capacidade de carga (kg)"),
    number("pesoBrutoTotalKg", "Peso bruto total (kg)"),
    text("implemento", "Implemento"),
    ...roadDocuments,
  ],
  CAMINHONETE: [
    mileage,
    text("tipoCabine", "Tipo de cabine", true),
    body,
    transmission,
    fuel,
    engine,
    turbo,
    displacement,
    doors,
    traction,
    steering,
    number("capacidadeCargaKg", "Capacidade de carga (kg)"),
    ...roadDocuments,
    yesNo("unicoDono", "Único dono?", true),
    yesNo("blindado", "Blindado?"),
  ],
  BARCO: [
    number("tamanhoPes", "Comprimento (pés)"),
    text("estilo", "Estilo"),
    text("materialCasco", "Material do casco"),
    number("capacidadePessoas", "Capacidade de pessoas"),
    number("numeroCabines", "Número de cabines"),
    number("horasUso", "Horas de uso"),
    text("registroMaritimo", "Registro marítimo"),
  ],
  LINHA_AMARELA: [
    text("tipoMaquina", "Tipo de máquina"),
    number("horimetro", "Horímetro (horas)"),
    number("pesoOperacionalKg", "Peso operacional (kg)"),
    number("potenciaHp", "Potência (hp)"),
    text("tipoEsteiraOuPneu", "Esteira ou pneu"),
    number("capacidadeCacambaM3", "Capacidade da caçamba (m³)"),
    text("numeroSerie", "Número de série"),
  ],
};
export const motorFields: Field[] = [
  number("posicao", "Posição do motor"),
  text("fabricante", "Fabricante do motor"),
  text("modelo", "Modelo do motor"),
  number("potenciaHp", "Potência (hp)"),
  number("ano", "Ano do motor"),
  number("horasUso", "Horas de uso do motor"),
  number("horasDesdeRevisao", "Horas desde a revisão"),
  text("combustivel", "Combustível do motor"),
];
export const commonVehicleFields: Field[] = [
  text("fabricante", "Marca / fabricante", true),
  text("modelo", "Modelo", true),
  text("versao", "Versão"),
  number("anoFabricacao", "Ano de fabricação", true),
  number("anoModelo", "Ano do modelo", true),
  {
    key: "condicao",
    label: "Condição",
    required: true,
    options: [
      ["USADO", "Usado"],
      ["ZERO_KM", "Zero km"],
    ],
  },
  text("cor", "Cor"),
  {
    key: "identificadorPublico",
    label: "Referência pública do veículo",
    hint: "Opcional. Identificador que poderá ser público; não use documentos pessoais.",
  },
];
