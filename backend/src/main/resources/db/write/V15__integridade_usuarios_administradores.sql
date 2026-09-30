ALTER TABLE identidade.usuarios_admin ADD CONSTRAINT fk_usuario_admin_usuario
    FOREIGN KEY (usuario_id) REFERENCES identidade.usuarios(id);
ALTER TABLE identidade.usuarios_admin ADD CONSTRAINT uq_usuario_admin_cpf UNIQUE (cpf);
ALTER TABLE identidade.usuarios_admin ADD CONSTRAINT uq_usuario_admin_cnpj UNIQUE (cnpj);
