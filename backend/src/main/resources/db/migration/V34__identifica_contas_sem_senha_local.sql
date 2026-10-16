ALTER TABLE identidade.usuarios
    ADD COLUMN senha_local_habilitada boolean NOT NULL DEFAULT true;

UPDATE identidade.usuarios AS usuario
SET senha_local_habilitada = false,
    atualizado_em = CURRENT_TIMESTAMP,
    sessao_versao = sessao_versao + 1
WHERE EXISTS (
      SELECT 1
      FROM identidade.identidades_externas AS identidade_externa
      WHERE identidade_externa.usuario_id = usuario.id
  );
