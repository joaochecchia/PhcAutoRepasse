-- Integridade de negócio somente na fonte; a projeção read é assíncrona.
ALTER TABLE identidade.enderecos_usuario ADD CONSTRAINT fk_endereco_usuario
    FOREIGN KEY (usuario_id) REFERENCES identidade.usuarios(id);
ALTER TABLE identidade.dados_compra_pf ADD CONSTRAINT fk_dados_compra_pf
    FOREIGN KEY (usuario_id) REFERENCES identidade.usuarios_pf(usuario_id);
ALTER TABLE identidade.dados_compra_pj ADD CONSTRAINT fk_dados_compra_pj
    FOREIGN KEY (usuario_id) REFERENCES identidade.usuarios_pj(usuario_id);
ALTER TABLE identidade.identidades_externas ADD CONSTRAINT fk_identidade_externa_usuario
    FOREIGN KEY (usuario_id) REFERENCES identidade.usuarios(id);
ALTER TABLE identidade.identidades_externas ADD CONSTRAINT uq_identidade_externa
    UNIQUE (provedor, identificador_externo);
