-- CPF, CNPJ e CEP são persistidos e projetados somente com dígitos.
ALTER TABLE identidade.usuarios_pf
    ADD CONSTRAINT ck_usuarios_pf_cpf_digitos CHECK (cpf ~ '^[0-9]{11}$');

ALTER TABLE identidade.usuarios_pj
    ADD CONSTRAINT ck_usuarios_pj_cnpj_digitos CHECK (cnpj ~ '^[0-9]{14}$');

ALTER TABLE identidade.enderecos_usuario
    ADD CONSTRAINT ck_enderecos_usuario_cep_digitos CHECK (cep ~ '^[0-9]{8}$');
