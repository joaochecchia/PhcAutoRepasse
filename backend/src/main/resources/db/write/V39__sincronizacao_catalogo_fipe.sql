ALTER TABLE fipe.catalogo ADD CONSTRAINT uq_fipe_catalogo UNIQUE(escopo,codigo_externo);
ALTER TABLE fipe.catalogo ADD CONSTRAINT fk_fipe_catalogo_pai FOREIGN KEY(pai_id) REFERENCES fipe.catalogo(id);
-- Fila operacional exclusiva do write; não é uma entidade de negócio projetada.
CREATE TABLE fipe.sincronizacoes (
 id uuid PRIMARY KEY, chave varchar(200) NOT NULL UNIQUE, comando text NOT NULL,
 status varchar(20) NOT NULL DEFAULT 'PENDENTE', tentativas integer NOT NULL DEFAULT 0,
 executar_em timestamptz NOT NULL DEFAULT now(), lease uuid, lease_ate timestamptz,
 erro varchar(100), atualizado_em timestamptz NOT NULL DEFAULT now(),
 CHECK(status IN ('PENDENTE','EXECUTANDO','CONCLUIDA','FALHOU'))
);
CREATE INDEX idx_fipe_sync_pendentes ON fipe.sincronizacoes(executar_em) WHERE status IN ('PENDENTE','EXECUTANDO');
