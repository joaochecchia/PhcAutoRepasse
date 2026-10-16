CREATE SCHEMA IF NOT EXISTS fipe;

CREATE TABLE fipe.importacoes (
    id uuid PRIMARY KEY,
    checksum varchar(64) NOT NULL,
    fonte varchar(120) NOT NULL,
    mes_referencia date NOT NULL,
    status varchar(20) NOT NULL,
    posicao_bytes bigint NOT NULL DEFAULT 0,
    registros bigint NOT NULL DEFAULT 0,
    erro varchar(500),
    criado_em timestamptz NOT NULL,
    atualizado_em timestamptz NOT NULL,
    lock_version bigint NOT NULL DEFAULT 0,
    CONSTRAINT ck_fipe_importacao_status CHECK (status IN ('PENDENTE','PROCESSANDO','CONCLUIDA','FALHOU')),
    CONSTRAINT ck_fipe_importacao_mes CHECK (extract(day from mes_referencia)=1),
    CONSTRAINT ck_fipe_importacao_progresso CHECK (posicao_bytes>=0 AND registros>=0)
);
CREATE INDEX idx_fipe_importacoes_pendentes ON fipe.importacoes(criado_em)
    WHERE status IN ('PENDENTE','PROCESSANDO');

-- Descrições são snapshots da edição da tabela: revisões não reescrevem meses anteriores.
CREATE TABLE fipe.referencias (
    id uuid PRIMARY KEY,
    codigo_fipe varchar(16) NOT NULL,
    tipo varchar(16) NOT NULL,
    marca varchar(100) NOT NULL,
    modelo varchar(250) NOT NULL,
    ano_modelo integer NOT NULL,
    combustivel varchar(60) NOT NULL,
    mes_referencia date NOT NULL,
    valor_centavos bigint NOT NULL,
    fonte varchar(120) NOT NULL,
    chave_consulta varchar(100),
    importacao_id uuid,
    criado_em timestamptz NOT NULL,
    lock_version bigint NOT NULL DEFAULT 0,
    CONSTRAINT ck_fipe_codigo CHECK (codigo_fipe ~ '^[0-9]{6}-[0-9]$'),
    CONSTRAINT ck_fipe_tipo CHECK (tipo IN ('CARRO','MOTO','CAMINHAO')),
    CONSTRAINT ck_fipe_ano CHECK (ano_modelo BETWEEN 1886 AND 2200 OR ano_modelo=32000),
    CONSTRAINT ck_fipe_mes CHECK (extract(day from mes_referencia)=1),
    CONSTRAINT ck_fipe_valor CHECK (valor_centavos>0 AND valor_centavos<=100000000000)
);
CREATE INDEX idx_fipe_referencias_busca ON fipe.referencias(tipo, lower(marca), mes_referencia DESC, ano_modelo);
CREATE INDEX idx_fipe_referencias_mes ON fipe.referencias(mes_referencia DESC, marca, modelo, ano_modelo, id);
CREATE INDEX idx_fipe_referencias_importacao ON fipe.referencias(importacao_id);
CREATE INDEX idx_fipe_referencias_consulta ON fipe.referencias(chave_consulta,mes_referencia);

CREATE INDEX idx_fipe_referencias_modelo ON fipe.referencias USING gin(lower(modelo) gin_trgm_ops);

ALTER TABLE catalogo.veiculos
    ADD COLUMN fipe_referencia_id uuid,
    ADD COLUMN fipe_codigo varchar(16),
    ADD COLUMN fipe_valor_centavos bigint,
    ADD COLUMN fipe_mes_referencia date,
    ADD COLUMN fipe_modelo varchar(250),
    ADD COLUMN fipe_fonte varchar(120),
    ADD CONSTRAINT ck_veiculo_fipe_snapshot CHECK (
        (num_nonnulls(fipe_referencia_id,fipe_codigo,fipe_valor_centavos,fipe_mes_referencia,fipe_modelo,fipe_fonte)=0)
        OR
        (num_nonnulls(fipe_referencia_id,fipe_codigo,fipe_valor_centavos,fipe_mes_referencia,fipe_modelo,fipe_fonte)=6
         AND tipo IN ('CARRO','MOTO','CAMINHAO','CAMINHONETE') AND fipe_valor_centavos>0
         AND extract(day from fipe_mes_referencia)=1)
    );
CREATE INDEX idx_veiculo_fipe_referencia ON catalogo.veiculos(fipe_referencia_id) WHERE fipe_referencia_id IS NOT NULL;
