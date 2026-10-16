ALTER TABLE fipe.importacoes ADD CONSTRAINT uq_fipe_importacao_arquivo UNIQUE(checksum,fonte,mes_referencia);
ALTER TABLE fipe.referencias ADD CONSTRAINT fk_fipe_importacao FOREIGN KEY(importacao_id) REFERENCES fipe.importacoes(id);
ALTER TABLE fipe.referencias ADD CONSTRAINT uq_fipe_referencia_edicao
    UNIQUE NULLS NOT DISTINCT(codigo_fipe,ano_modelo,mes_referencia,importacao_id);
ALTER TABLE catalogo.veiculos ADD CONSTRAINT fk_veiculo_fipe_referencia
    FOREIGN KEY(fipe_referencia_id) REFERENCES fipe.referencias(id);
