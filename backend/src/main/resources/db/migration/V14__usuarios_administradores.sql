ALTER TABLE identidade.usuarios DROP CONSTRAINT usuarios_papel_check;
ALTER TABLE identidade.usuarios ADD CONSTRAINT usuarios_papel_check
    CHECK (papel IN ('CLIENTE', 'ANUNCIANTE', 'OPERADOR', 'ADMIN', 'DONO'));

CREATE TABLE identidade.usuarios_admin (
    usuario_id uuid NOT NULL PRIMARY KEY,
    tipo_pessoa varchar(32) NOT NULL CHECK (tipo_pessoa IN ('PF', 'PJ')),
    cpf char(11),
    data_nascimento date,
    cnpj char(14),
    razao_social varchar(200),
    lock_version bigint NOT NULL DEFAULT 0,
    CONSTRAINT ck_usuario_admin_documento CHECK (
        (tipo_pessoa = 'PF' AND cpf IS NOT NULL AND cpf ~ '^[0-9]{11}$'
            AND data_nascimento IS NOT NULL AND cnpj IS NULL AND razao_social IS NULL)
        OR
        (tipo_pessoa = 'PJ' AND cnpj IS NOT NULL AND cnpj ~ '^[0-9]{14}$'
            AND razao_social IS NOT NULL AND btrim(razao_social) <> ''
            AND cpf IS NULL AND data_nascimento IS NULL)
    )
);
