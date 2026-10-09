export type Vehicle = {
  id: string;
  brand: string;
  model: string;
  version: string;
  price: string;
  year: string;
  mileage?: string;
  location: string;
  image?: string;
  imageUrl?: string | null;
  fuel: string;
  transmission: string;
  tag?: string;
  seller: string;
  live?: boolean;
  condition?: string;
  type?: string;
  auctionHistory?: boolean | null;
  accidentHistory?: boolean | null;
};
// Editorial fixtures, not a client-side implementation of the catalog service.
export const vehicles: Vehicle[] = [
  {
    id: "1",
    brand: "PORSCHE",
    model: "Panamera",
    version: "4.0 V8 Turbo PDK",
    price: "459.900",
    year: "2018 / 2019",
    mileage: "32.000",
    location: "São Paulo, SP",
    image: "porsche",
    fuel: "Gasolina",
    transmission: "Automático",
    tag: "EM DESTAQUE",
    seller: "PHC Auto",
  },
  {
    id: "2",
    brand: "BMW",
    model: "M5",
    version: "4.4 V8 TwinPower Turbo",
    price: "549.900",
    year: "2019 / 2020",
    mileage: "28.500",
    location: "Curitiba, PR",
    image: "bmw",
    fuel: "Gasolina",
    transmission: "Automático",
    seller: "Loja demonstrativa",
  },
  {
    id: "3",
    brand: "MERCEDES-BENZ",
    model: "AMG GT",
    version: "4.0 V8 Biturbo Performance",
    price: "729.900",
    year: "2020 / 2021",
    mileage: "21.000",
    location: "Campinas, SP",
    image: "mercedes",
    fuel: "Gasolina",
    transmission: "Automático",
    seller: "Loja demonstrativa",
  },
  {
    id: "4",
    brand: "AUDI",
    model: "RS7",
    version: "4.0 V8 TFSI Sportback Quattro",
    price: "769.900",
    year: "2021 / 2022",
    mileage: "19.000",
    location: "Belo Horizonte, MG",
    image: "audi",
    fuel: "Gasolina",
    transmission: "Automático",
    tag: "EM DESTAQUE",
    seller: "PHC Auto",
  },
  {
    id: "5",
    brand: "PORSCHE",
    model: "911 Carrera",
    version: "3.0 Coupé PDK",
    price: "619.900",
    year: "2017 / 2018",
    mileage: "42.000",
    location: "São Paulo, SP",
    image: "golf",
    fuel: "Gasolina",
    transmission: "Automático",
    seller: "Anunciante demonstrativo",
  },
  {
    id: "6",
    brand: "TOYOTA",
    model: "Camry",
    version: "3.5 V6 XLE",
    price: "219.900",
    year: "2020 / 2021",
    mileage: "35.000",
    location: "Goiânia, GO",
    image: "toyota",
    fuel: "Gasolina",
    transmission: "Automático",
    seller: "Loja demonstrativa",
  },
  {
    id: "7",
    brand: "FORD",
    model: "Expedition",
    version: "3.5 V6 EcoBoost Limited",
    price: "389.900",
    year: "2019 / 2020",
    mileage: "52.000",
    location: "Brasília, DF",
    image: "mustang",
    fuel: "Gasolina",
    transmission: "Automático",
    seller: "Loja demonstrativa",
  },
  {
    id: "8",
    brand: "FORD",
    model: "Mustang",
    version: "5.0 V8 GT Premium",
    price: "359.900",
    year: "2018 / 2019",
    mileage: "38.000",
    location: "Ribeirão Preto, SP",
    image: "jeep",
    fuel: "Gasolina",
    transmission: "Automático",
    seller: "Loja demonstrativa",
  },
];
export const brands = [
  ["Volkswagen", "volkswagen"],
  ["Chevrolet", "chevrolet"],
  ["Fiat", "fiat"],
  ["Toyota", "toyota"],
  ["Honda", "honda"],
  ["Jeep", "jeep"],
  ["BMW", "bmw"],
  ["Hyundai", "hyundai"],
];
export const types = [
  ["CARRO", "Carros"],
  ["MOTO", "Motos"],
  ["CAMINHONETE", "Caminhonetes"],
  ["CAMINHAO", "Caminhões"],
  ["BARCO", "Barcos"],
  ["LINHA_AMARELA", "Linha amarela"],
];
