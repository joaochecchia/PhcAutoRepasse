-- A placa completa é obrigatória para novos anúncios. Registros anteriores são
-- preservados com placa nula até que o anunciante atualize os dados do veículo.
ALTER TABLE catalogo.carros ADD COLUMN placa varchar(7), ADD COLUMN exibir_placa_completa boolean NOT NULL DEFAULT false;
ALTER TABLE catalogo.motos ADD COLUMN placa varchar(7), ADD COLUMN exibir_placa_completa boolean NOT NULL DEFAULT false;
ALTER TABLE catalogo.caminhoes ADD COLUMN placa varchar(7), ADD COLUMN exibir_placa_completa boolean NOT NULL DEFAULT false;
ALTER TABLE catalogo.caminhonetes ADD COLUMN placa varchar(7), ADD COLUMN exibir_placa_completa boolean NOT NULL DEFAULT false;
ALTER TABLE catalogo.carros ADD CONSTRAINT ck_carros_placa_completa CHECK (placa IS NULL OR placa ~ '^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$');
ALTER TABLE catalogo.motos ADD CONSTRAINT ck_motos_placa_completa CHECK (placa IS NULL OR placa ~ '^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$');
ALTER TABLE catalogo.caminhoes ADD CONSTRAINT ck_caminhoes_placa_completa CHECK (placa IS NULL OR placa ~ '^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$');
ALTER TABLE catalogo.caminhonetes ADD CONSTRAINT ck_caminhonetes_placa_completa CHECK (placa IS NULL OR placa ~ '^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$');
