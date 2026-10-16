ALTER TABLE fipe.referencias DROP CONSTRAINT uq_fipe_referencia_edicao;
ALTER TABLE fipe.referencias ADD CONSTRAINT uq_fipe_referencia_edicao
    UNIQUE NULLS NOT DISTINCT(tipo,codigo_fipe,ano_modelo,combustivel,mes_referencia,importacao_id);
