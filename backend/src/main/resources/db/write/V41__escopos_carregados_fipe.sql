-- Registro técnico, exclusivo do write, dos escopos do catálogo FIPE carregados por completo.
-- Linhas parciais em fipe.catalogo não indicam catálogo completo; só este registro dispensa nova consulta externa.
CREATE TABLE fipe.escopos_carregados (
 escopo varchar(100) PRIMARY KEY,
 quantidade integer NOT NULL CHECK (quantidade >= 0),
 carregado_em timestamptz NOT NULL DEFAULT now()
);
-- Escopos concluídos pela fila já foram gravados integralmente; os demais serão confirmados no próximo acesso.
INSERT INTO fipe.escopos_carregados(escopo, quantidade, carregado_em)
SELECT s.chave, (SELECT count(*) FROM fipe.catalogo c WHERE c.escopo = s.chave), s.atualizado_em
FROM fipe.sincronizacoes s
WHERE s.status = 'CONCLUIDA' AND EXISTS (SELECT 1 FROM fipe.catalogo c WHERE c.escopo = s.chave)
ON CONFLICT (escopo) DO NOTHING;
