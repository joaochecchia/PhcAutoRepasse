-- O write é a autoridade para FKs e unicidades de negócio.
-- A projeção recebe linhas assíncronas e pode ter relações temporariamente incompletas.
-- Mantemos PKs, tipos, NOT NULL, CHECKs e índices; não aceitamos escrita de negócio aqui.
DO $$
DECLARE constraint_row record;
BEGIN
    FOR constraint_row IN
        SELECT n.nspname, t.relname, c.conname
        FROM pg_constraint c JOIN pg_class t ON t.oid = c.conrelid
        JOIN pg_namespace n ON n.oid = t.relnamespace
        WHERE n.nspname IN ('identidade', 'assinaturas', 'catalogo', 'vendas')
          AND c.contype IN ('f', 'u')
    LOOP
        EXECUTE format('ALTER TABLE %I.%I DROP CONSTRAINT %I',
            constraint_row.nspname, constraint_row.relname, constraint_row.conname);
    END LOOP;
END $$;
