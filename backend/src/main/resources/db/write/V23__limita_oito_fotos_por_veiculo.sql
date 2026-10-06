CREATE OR REPLACE FUNCTION catalogo.validar_limite_fotos_veiculo()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    PERFORM pg_advisory_xact_lock(hashtextextended(NEW.anuncio_id::text, 0));
    IF (SELECT count(*) FROM catalogo.fotos WHERE anuncio_id = NEW.anuncio_id) >= 8 THEN
        RAISE EXCEPTION 'Cada veículo pode ter no máximo 8 fotos'
            USING ERRCODE = '23514', CONSTRAINT = 'ck_fotos_maximo_oito_por_veiculo';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_fotos_maximo_oito_por_veiculo
BEFORE INSERT ON catalogo.fotos
FOR EACH ROW
EXECUTE FUNCTION catalogo.validar_limite_fotos_veiculo();
