-- Novas gravações exigem respostas explícitas; dados legados não são inventados.
ALTER TABLE catalogo.carros DROP CONSTRAINT ck_carros_dados_obrigatorios;
ALTER TABLE catalogo.carros
    ADD CONSTRAINT ck_carros_dados_obrigatorios
    CHECK (quilometragem IS NOT NULL AND quilometragem >= 0
        AND NULLIF(BTRIM(carroceria), '') IS NOT NULL
        AND NULLIF(BTRIM(cambio), '') IS NOT NULL
        AND NULLIF(BTRIM(combustivel), '') IS NOT NULL
        AND NULLIF(BTRIM(motorizacao), '') IS NOT NULL
        AND (cilindrada_litros IS NULL OR cilindrada_litros > 0)
        AND (numero_portas IS NULL OR numero_portas > 0) AND unico_dono IS NOT NULL AND ipva_pago IS NOT NULL) NOT VALID;

ALTER TABLE catalogo.motos DROP CONSTRAINT ck_motos_dados_obrigatorios;
ALTER TABLE catalogo.motos
    ADD CONSTRAINT ck_motos_dados_obrigatorios
    CHECK (quilometragem IS NOT NULL AND quilometragem >= 0
        AND (cilindradas IS NULL OR cilindradas > 0)
        AND NULLIF(BTRIM(categoria), '') IS NOT NULL
        AND NULLIF(BTRIM(cambio), '') IS NOT NULL
        AND NULLIF(BTRIM(combustivel), '') IS NOT NULL AND ipva_pago IS NOT NULL) NOT VALID;

ALTER TABLE catalogo.caminhoes DROP CONSTRAINT ck_caminhoes_dados_obrigatorios;
ALTER TABLE catalogo.caminhoes
    ADD CONSTRAINT ck_caminhoes_dados_obrigatorios
    CHECK (quilometragem IS NOT NULL AND quilometragem >= 0
        AND NULLIF(BTRIM(configuracao), '') IS NOT NULL
        AND NULLIF(BTRIM(carroceria), '') IS NOT NULL
        AND NULLIF(BTRIM(cambio), '') IS NOT NULL
        AND NULLIF(BTRIM(combustivel), '') IS NOT NULL
        AND numero_eixos IS NOT NULL AND numero_eixos > 0 AND ipva_pago IS NOT NULL) NOT VALID;

ALTER TABLE catalogo.caminhonetes DROP CONSTRAINT ck_caminhonetes_dados_obrigatorios;
ALTER TABLE catalogo.caminhonetes
    ADD CONSTRAINT ck_caminhonetes_dados_obrigatorios
    CHECK (quilometragem IS NOT NULL AND quilometragem >= 0
        AND NULLIF(BTRIM(tipo_cabine), '') IS NOT NULL
        AND NULLIF(BTRIM(carroceria), '') IS NOT NULL
        AND NULLIF(BTRIM(cambio), '') IS NOT NULL
        AND NULLIF(BTRIM(combustivel), '') IS NOT NULL
        AND NULLIF(BTRIM(motorizacao), '') IS NOT NULL
        AND (cilindrada_litros IS NULL OR cilindrada_litros > 0)
        AND (numero_portas IS NULL OR numero_portas > 0) AND unico_dono IS NOT NULL AND ipva_pago IS NOT NULL) NOT VALID;

