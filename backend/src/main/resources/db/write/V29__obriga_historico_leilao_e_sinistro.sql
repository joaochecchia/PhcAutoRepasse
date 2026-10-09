-- Mantém registros legados sem valores inventados e exige a informação em novas gravações/alterações.
ALTER TABLE catalogo.veiculos
    ADD CONSTRAINT ck_veiculos_historico_leilao_informado
        CHECK (historico_leilao IS NOT NULL) NOT VALID,
    ADD CONSTRAINT ck_veiculos_historico_sinistro_informado
        CHECK (historico_sinistro IS NOT NULL) NOT VALID;
