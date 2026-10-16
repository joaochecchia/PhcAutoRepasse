-- Hierarquia normalizada: referência mensal -> marca -> modelo/versão -> ano/combustível.
CREATE TABLE fipe.catalogo (
 id uuid PRIMARY KEY, pai_id uuid, escopo varchar(100) NOT NULL,
 codigo_externo varchar(20) NOT NULL, nome varchar(250) NOT NULL,
 mes_referencia date, atualizado_em timestamptz NOT NULL,
 lock_version bigint NOT NULL DEFAULT 0
);
CREATE INDEX idx_fipe_catalogo_escopo ON fipe.catalogo(escopo, nome, codigo_externo);
CREATE INDEX idx_fipe_catalogo_pai ON fipe.catalogo(pai_id);
CREATE INDEX idx_fipe_catalogo_nome ON fipe.catalogo USING gin(lower(nome) gin_trgm_ops);
