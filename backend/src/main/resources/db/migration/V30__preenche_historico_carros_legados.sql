-- Preenche apenas carros legados que antecedem a obrigatoriedade dos campos.
-- O hash do UUID produz o mesmo resultado nos bancos write e read.
DO $$
DECLARE
    possui_constraint_dados_obrigatorios boolean;
BEGIN
    SELECT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conrelid = 'catalogo.veiculos'::regclass
          AND conname = 'ck_veiculos_dados_obrigatorios'
    ) INTO possui_constraint_dados_obrigatorios;

    -- No write, a constraint V9 precisa ser recriada para permitir a correção
    -- parcial de legados que ainda não possuem condição ou ano completos.
    IF possui_constraint_dados_obrigatorios THEN
        ALTER TABLE catalogo.veiculos
            DROP CONSTRAINT ck_veiculos_dados_obrigatorios;
    END IF;

    UPDATE catalogo.veiculos
    SET historico_leilao = COALESCE(
            historico_leilao,
            get_byte(decode(md5(id::text || ':leilao'), 'hex'), 0) < 39
        ),
        historico_sinistro = COALESCE(
            historico_sinistro,
            get_byte(decode(md5(id::text || ':sinistro'), 'hex'), 0) < 31
        )
    WHERE tipo = 'CARRO'
      AND (historico_leilao IS NULL OR historico_sinistro IS NULL);

    IF possui_constraint_dados_obrigatorios THEN
        ALTER TABLE catalogo.veiculos
            ADD CONSTRAINT ck_veiculos_dados_obrigatorios
            CHECK (ano_fabricacao IS NOT NULL AND ano_modelo IS NOT NULL AND condicao IS NOT NULL)
            NOT VALID;
    END IF;
END $$;
