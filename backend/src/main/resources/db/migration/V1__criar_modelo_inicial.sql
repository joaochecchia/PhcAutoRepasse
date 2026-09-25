-- Modelo inicial PHC Auto. Enums persistidos como varchar com CHECK.

-- lock_version adiciona controle otimista sem alterar os contratos de domínio.

CREATE SCHEMA identidade;

CREATE SCHEMA assinaturas;

CREATE SCHEMA catalogo;

CREATE SCHEMA vendas;

CREATE TABLE identidade.usuarios (
    id uuid NOT NULL PRIMARY KEY,
    nome varchar(160) NOT NULL,
    email varchar(254) NOT NULL UNIQUE,
    senha_hash text NOT NULL,
    tipo_pessoa varchar(32) NOT NULL CHECK (tipo_pessoa IN ('PF', 'PJ')),
    papel varchar(32) NOT NULL CHECK (papel IN ('CLIENTE', 'ANUNCIANTE', 'OPERADOR', 'ADMIN')),
    ativo boolean NOT NULL DEFAULT true,
    criado_em timestamptz NOT NULL,
    atualizado_em timestamptz NOT NULL,
    lock_version bigint NOT NULL DEFAULT 0
);

CREATE TABLE identidade.usuarios_pf (
    usuario_id uuid NOT NULL PRIMARY KEY,
    cpf char(11) NOT NULL UNIQUE,
    data_nascimento date,
    lock_version bigint NOT NULL DEFAULT 0
);

CREATE TABLE identidade.usuarios_pj (
    usuario_id uuid NOT NULL PRIMARY KEY,
    cnpj char(14) NOT NULL UNIQUE,
    razao_social varchar(200) NOT NULL,
    nome_fantasia varchar(200),
    lock_version bigint NOT NULL DEFAULT 0
);

CREATE TABLE assinaturas.planos (
    id uuid NOT NULL PRIMARY KEY,
    nome varchar(100) NOT NULL UNIQUE,
    valor_centavos bigint,
    periodo_meses smallint,
    limite_anuncios integer,
    ativo boolean NOT NULL DEFAULT false,
    criado_em timestamptz NOT NULL,
    lock_version bigint NOT NULL DEFAULT 0
);

CREATE TABLE assinaturas.assinaturas (
    id uuid NOT NULL PRIMARY KEY,
    usuario_id uuid NOT NULL,
    plano_id uuid NOT NULL,
    valor_contratado_centavos bigint NOT NULL,
    status varchar(32) NOT NULL CHECK (status IN ('PENDENTE', 'ATIVA', 'SUSPENSA', 'CANCELADA', 'EXPIRADA')),
    inicio_em timestamptz,
    fim_em timestamptz,
    criado_em timestamptz NOT NULL,
    lock_version bigint NOT NULL DEFAULT 0
);

CREATE TABLE catalogo.veiculos (
    id uuid NOT NULL PRIMARY KEY,
    proprietario_id uuid NOT NULL,
    tipo varchar(32) NOT NULL CHECK (tipo IN ('CARRO', 'MOTO', 'CAMINHAO', 'CAMINHONETE', 'BARCO', 'LINHA_AMARELA')),
    fabricante varchar(100) NOT NULL,
    modelo varchar(120) NOT NULL,
    versao varchar(180),
    ano_fabricacao smallint,
    ano_modelo smallint,
    cor varchar(60),
    identificador_publico varchar(80),
    criado_em timestamptz NOT NULL,
    atualizado_em timestamptz NOT NULL,
    lock_version bigint NOT NULL DEFAULT 0
);

CREATE TABLE catalogo.carros (
    veiculo_id uuid NOT NULL PRIMARY KEY,
    quilometragem integer,
    carroceria varchar(60),
    cambio varchar(60),
    combustivel varchar(60),
    tracao varchar(40),
    motorizacao varchar(100),
    numero_portas smallint,
    numero_lugares smallint,
    final_placa char(1),
    unico_dono boolean,
    ipva_pago boolean,
    licenciado boolean,
    blindado boolean,
    lock_version bigint NOT NULL DEFAULT 0
);

CREATE TABLE catalogo.motos (
    veiculo_id uuid NOT NULL PRIMARY KEY,
    quilometragem integer,
    cilindradas integer,
    categoria varchar(60),
    partida varchar(40),
    refrigeracao varchar(40),
    cambio varchar(60),
    combustivel varchar(60),
    final_placa char(1),
    ipva_pago boolean,
    licenciado boolean,
    lock_version bigint NOT NULL DEFAULT 0
);

CREATE TABLE catalogo.caminhoes (
    veiculo_id uuid NOT NULL PRIMARY KEY,
    quilometragem integer,
    configuracao varchar(80),
    carroceria varchar(80),
    cambio varchar(60),
    combustivel varchar(60),
    tracao varchar(40),
    numero_eixos smallint,
    capacidade_carga_kg integer,
    peso_bruto_total_kg integer,
    implemento varchar(100),
    final_placa char(1),
    ipva_pago boolean,
    licenciado boolean,
    lock_version bigint NOT NULL DEFAULT 0
);

CREATE TABLE catalogo.caminhonetes (
    veiculo_id uuid NOT NULL PRIMARY KEY,
    quilometragem integer,
    tipo_cabine varchar(50),
    carroceria varchar(60),
    cambio varchar(60),
    combustivel varchar(60),
    tracao varchar(40),
    motorizacao varchar(100),
    capacidade_carga_kg integer,
    numero_portas smallint,
    final_placa char(1),
    unico_dono boolean,
    ipva_pago boolean,
    licenciado boolean,
    lock_version bigint NOT NULL DEFAULT 0
);

CREATE TABLE catalogo.barcos (
    veiculo_id uuid NOT NULL PRIMARY KEY,
    tamanho_pes decimal(7,2),
    estilo varchar(80),
    material_casco varchar(80),
    capacidade_pessoas smallint,
    numero_cabines smallint,
    horas_uso integer,
    registro_maritimo varchar(80),
    lock_version bigint NOT NULL DEFAULT 0
);

CREATE TABLE catalogo.motores_barco (
    id uuid NOT NULL PRIMARY KEY,
    barco_id uuid NOT NULL,
    posicao smallint NOT NULL,
    fabricante varchar(100),
    modelo varchar(120),
    potencia_hp decimal(8,2),
    ano smallint,
    horas_uso integer,
    horas_desde_revisao integer,
    combustivel varchar(60),
    UNIQUE (barco_id, posicao),
    lock_version bigint NOT NULL DEFAULT 0
);

CREATE TABLE catalogo.linha_amarela (
    veiculo_id uuid NOT NULL PRIMARY KEY,
    tipo_maquina varchar(80),
    horimetro integer,
    peso_operacional_kg integer,
    potencia_hp decimal(8,2),
    tipo_esteira_ou_pneu varchar(30),
    capacidade_cacamba_m3 decimal(8,3),
    numero_serie varchar(100),
    lock_version bigint NOT NULL DEFAULT 0
);

CREATE TABLE catalogo.caracteristicas (
    id uuid NOT NULL PRIMARY KEY,
    tipo_veiculo varchar(32) NOT NULL CHECK (tipo_veiculo IN ('CARRO', 'MOTO', 'CAMINHAO', 'CAMINHONETE', 'BARCO', 'LINHA_AMARELA')),
    nome varchar(100) NOT NULL,
    grupo varchar(60),
    UNIQUE (tipo_veiculo, nome),
    lock_version bigint NOT NULL DEFAULT 0
);

CREATE TABLE catalogo.veiculo_caracteristicas (
    veiculo_id uuid NOT NULL,
    caracteristica_id uuid NOT NULL,
    observacao varchar(200),
    PRIMARY KEY (veiculo_id, caracteristica_id),
    lock_version bigint NOT NULL DEFAULT 0
);

CREATE TABLE catalogo.anuncios (
    id uuid NOT NULL PRIMARY KEY,
    veiculo_id uuid NOT NULL,
    anunciante_id uuid NOT NULL,
    titulo varchar(180) NOT NULL,
    descricao text,
    tipo_preco varchar(32) NOT NULL CHECK (tipo_preco IN ('FIXO', 'SOB_CONSULTA')),
    preco_centavos bigint,
    aceita_troca boolean,
    cidade varchar(120) NOT NULL,
    uf char(2) NOT NULL,
    status varchar(32) NOT NULL CHECK (status IN ('RASCUNHO', 'PUBLICADO', 'PAUSADO', 'RESERVADO', 'VENDIDO', 'ARQUIVADO')),
    publicado_em timestamptz,
    criado_em timestamptz NOT NULL,
    atualizado_em timestamptz NOT NULL,
    versao integer NOT NULL DEFAULT 0,
    CONSTRAINT ck_anuncio_preco CHECK ((tipo_preco = 'FIXO' AND preco_centavos IS NOT NULL) OR (tipo_preco = 'SOB_CONSULTA' AND preco_centavos IS NULL))
);

CREATE TABLE catalogo.fotos (
    id uuid NOT NULL PRIMARY KEY,
    anuncio_id uuid NOT NULL,
    chave_arquivo text NOT NULL UNIQUE,
    posicao smallint NOT NULL,
    texto_alternativo varchar(180),
    UNIQUE (anuncio_id, posicao),
    lock_version bigint NOT NULL DEFAULT 0
);

CREATE TABLE vendas.compras (
    id uuid NOT NULL PRIMARY KEY,
    anuncio_id uuid NOT NULL,
    comprador_id uuid NOT NULL,
    vendedor_id uuid NOT NULL,
    valor_centavos bigint NOT NULL,
    titulo_veiculo_snapshot varchar(250) NOT NULL,
    status varchar(32) NOT NULL CHECK (status IN ('AGUARDANDO_PAGAMENTO', 'PAGO', 'CANCELADO', 'EXPIRADO', 'REEMBOLSADO')),
    criado_em timestamptz NOT NULL,
    pago_em timestamptz,
    lock_version bigint NOT NULL DEFAULT 0
);

CREATE TABLE vendas.pagamentos (
    id uuid NOT NULL PRIMARY KEY,
    compra_id uuid NOT NULL,
    provedor varchar(50) NOT NULL,
    referencia_externa varchar(180),
    metodo varchar(32) NOT NULL CHECK (metodo IN ('PIX', 'BOLETO')),
    status varchar(32) NOT NULL CHECK (status IN ('PENDENTE', 'CONFIRMADO', 'FALHOU', 'CANCELADO', 'EXPIRADO', 'ESTORNADO')),
    valor_centavos bigint NOT NULL,
    vence_em timestamptz,
    confirmado_em timestamptz,
    criado_em timestamptz NOT NULL,
    UNIQUE (provedor, referencia_externa),
    lock_version bigint NOT NULL DEFAULT 0
);

CREATE SEQUENCE vendas.eventos_gateway_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE vendas.eventos_gateway (
    id bigint NOT NULL PRIMARY KEY DEFAULT nextval('vendas.eventos_gateway_seq'),
    provedor varchar(50) NOT NULL,
    evento_externo_id varchar(180) NOT NULL,
    pagamento_id uuid,
    recebido_em timestamptz NOT NULL,
    processado_em timestamptz,
    UNIQUE (provedor, evento_externo_id),
    lock_version bigint NOT NULL DEFAULT 0
);

ALTER TABLE identidade.usuarios_pf ADD CONSTRAINT fk_usuarios_pf_usuario_id FOREIGN KEY (usuario_id) REFERENCES identidade.usuarios (id);

ALTER TABLE identidade.usuarios_pj ADD CONSTRAINT fk_usuarios_pj_usuario_id FOREIGN KEY (usuario_id) REFERENCES identidade.usuarios (id);

ALTER TABLE assinaturas.assinaturas ADD CONSTRAINT fk_assinaturas_usuario_id FOREIGN KEY (usuario_id) REFERENCES identidade.usuarios (id);

CREATE INDEX idx_assinaturas_usuario_id ON assinaturas.assinaturas (usuario_id);

ALTER TABLE assinaturas.assinaturas ADD CONSTRAINT fk_assinaturas_plano_id FOREIGN KEY (plano_id) REFERENCES assinaturas.planos (id);

CREATE INDEX idx_assinaturas_plano_id ON assinaturas.assinaturas (plano_id);

ALTER TABLE catalogo.veiculos ADD CONSTRAINT fk_veiculos_proprietario_id FOREIGN KEY (proprietario_id) REFERENCES identidade.usuarios (id);

CREATE INDEX idx_veiculos_proprietario_id ON catalogo.veiculos (proprietario_id);

ALTER TABLE catalogo.carros ADD CONSTRAINT fk_carros_veiculo_id FOREIGN KEY (veiculo_id) REFERENCES catalogo.veiculos (id);

ALTER TABLE catalogo.motos ADD CONSTRAINT fk_motos_veiculo_id FOREIGN KEY (veiculo_id) REFERENCES catalogo.veiculos (id);

ALTER TABLE catalogo.caminhoes ADD CONSTRAINT fk_caminhoes_veiculo_id FOREIGN KEY (veiculo_id) REFERENCES catalogo.veiculos (id);

ALTER TABLE catalogo.caminhonetes ADD CONSTRAINT fk_caminhonetes_veiculo_id FOREIGN KEY (veiculo_id) REFERENCES catalogo.veiculos (id);

ALTER TABLE catalogo.barcos ADD CONSTRAINT fk_barcos_veiculo_id FOREIGN KEY (veiculo_id) REFERENCES catalogo.veiculos (id);

ALTER TABLE catalogo.linha_amarela ADD CONSTRAINT fk_linha_amarela_veiculo_id FOREIGN KEY (veiculo_id) REFERENCES catalogo.veiculos (id);

ALTER TABLE catalogo.motores_barco ADD CONSTRAINT fk_motores_barco_barco_id FOREIGN KEY (barco_id) REFERENCES catalogo.barcos (veiculo_id);

CREATE INDEX idx_motores_barco_barco_id ON catalogo.motores_barco (barco_id);

ALTER TABLE catalogo.veiculo_caracteristicas ADD CONSTRAINT fk_veiculo_caracteristicas_veiculo_id FOREIGN KEY (veiculo_id) REFERENCES catalogo.veiculos (id);

CREATE INDEX idx_veiculo_caracteristicas_veiculo_id ON catalogo.veiculo_caracteristicas (veiculo_id);

ALTER TABLE catalogo.veiculo_caracteristicas ADD CONSTRAINT fk_veiculo_caracteristicas_caracteristica_id FOREIGN KEY (caracteristica_id) REFERENCES catalogo.caracteristicas (id);

CREATE INDEX idx_veiculo_caracteristicas_caracteristica_id ON catalogo.veiculo_caracteristicas (caracteristica_id);

ALTER TABLE catalogo.anuncios ADD CONSTRAINT fk_anuncios_anunciante_id FOREIGN KEY (anunciante_id) REFERENCES identidade.usuarios (id);

CREATE INDEX idx_anuncios_anunciante_id ON catalogo.anuncios (anunciante_id);

ALTER TABLE catalogo.anuncios ADD CONSTRAINT fk_anuncios_veiculo_id FOREIGN KEY (veiculo_id) REFERENCES catalogo.veiculos (id);

CREATE INDEX idx_anuncios_veiculo_id ON catalogo.anuncios (veiculo_id);

ALTER TABLE catalogo.fotos ADD CONSTRAINT fk_fotos_anuncio_id FOREIGN KEY (anuncio_id) REFERENCES catalogo.anuncios (id);

CREATE INDEX idx_fotos_anuncio_id ON catalogo.fotos (anuncio_id);

ALTER TABLE vendas.compras ADD CONSTRAINT fk_compras_anuncio_id FOREIGN KEY (anuncio_id) REFERENCES catalogo.anuncios (id);

CREATE INDEX idx_compras_anuncio_id ON vendas.compras (anuncio_id);

ALTER TABLE vendas.compras ADD CONSTRAINT fk_compras_comprador_id FOREIGN KEY (comprador_id) REFERENCES identidade.usuarios (id);

CREATE INDEX idx_compras_comprador_id ON vendas.compras (comprador_id);

ALTER TABLE vendas.compras ADD CONSTRAINT fk_compras_vendedor_id FOREIGN KEY (vendedor_id) REFERENCES identidade.usuarios (id);

CREATE INDEX idx_compras_vendedor_id ON vendas.compras (vendedor_id);

ALTER TABLE vendas.pagamentos ADD CONSTRAINT fk_pagamentos_compra_id FOREIGN KEY (compra_id) REFERENCES vendas.compras (id);

CREATE INDEX idx_pagamentos_compra_id ON vendas.pagamentos (compra_id);

ALTER TABLE vendas.eventos_gateway ADD CONSTRAINT fk_eventos_gateway_pagamento_id FOREIGN KEY (pagamento_id) REFERENCES vendas.pagamentos (id);

CREATE INDEX idx_eventos_gateway_pagamento_id ON vendas.eventos_gateway (pagamento_id);

CREATE INDEX idx_anuncios_status_criado ON catalogo.anuncios (status, criado_em DESC, id DESC);

CREATE INDEX idx_eventos_nao_processados ON vendas.eventos_gateway (recebido_em) WHERE processado_em IS NULL;
