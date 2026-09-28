-- Preserva anúncios legados incompletos, mas impede novas gravações inválidas no banco principal.
ALTER TABLE catalogo.veiculos
    ADD CONSTRAINT ck_veiculos_dados_obrigatorios
    CHECK (ano_fabricacao IS NOT NULL AND ano_modelo IS NOT NULL AND condicao IS NOT NULL) NOT VALID;

ALTER TABLE catalogo.carros
    ADD CONSTRAINT ck_carros_dados_obrigatorios
    CHECK (quilometragem IS NOT NULL AND quilometragem >= 0
        AND NULLIF(BTRIM(carroceria), '') IS NOT NULL
        AND NULLIF(BTRIM(cambio), '') IS NOT NULL
        AND NULLIF(BTRIM(combustivel), '') IS NOT NULL
        AND NULLIF(BTRIM(motorizacao), '') IS NOT NULL
        AND cilindrada_litros IS NOT NULL AND cilindrada_litros > 0
        AND numero_portas IS NOT NULL AND numero_portas > 0) NOT VALID;

ALTER TABLE catalogo.motos
    ADD CONSTRAINT ck_motos_dados_obrigatorios
    CHECK (quilometragem IS NOT NULL AND quilometragem >= 0
        AND cilindradas IS NOT NULL AND cilindradas > 0
        AND NULLIF(BTRIM(categoria), '') IS NOT NULL
        AND NULLIF(BTRIM(cambio), '') IS NOT NULL
        AND NULLIF(BTRIM(combustivel), '') IS NOT NULL) NOT VALID;

ALTER TABLE catalogo.caminhoes
    ADD CONSTRAINT ck_caminhoes_dados_obrigatorios
    CHECK (quilometragem IS NOT NULL AND quilometragem >= 0
        AND NULLIF(BTRIM(configuracao), '') IS NOT NULL
        AND NULLIF(BTRIM(carroceria), '') IS NOT NULL
        AND NULLIF(BTRIM(cambio), '') IS NOT NULL
        AND NULLIF(BTRIM(combustivel), '') IS NOT NULL
        AND numero_eixos IS NOT NULL AND numero_eixos > 0) NOT VALID;

ALTER TABLE catalogo.caminhonetes
    ADD CONSTRAINT ck_caminhonetes_dados_obrigatorios
    CHECK (quilometragem IS NOT NULL AND quilometragem >= 0
        AND NULLIF(BTRIM(tipo_cabine), '') IS NOT NULL
        AND NULLIF(BTRIM(carroceria), '') IS NOT NULL
        AND NULLIF(BTRIM(cambio), '') IS NOT NULL
        AND NULLIF(BTRIM(combustivel), '') IS NOT NULL
        AND NULLIF(BTRIM(motorizacao), '') IS NOT NULL
        AND cilindrada_litros IS NOT NULL AND cilindrada_litros > 0
        AND numero_portas IS NOT NULL AND numero_portas > 0) NOT VALID;
