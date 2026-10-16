-- Preserva descrições completas do modelo/versão, sem truncar os dados do fornecedor.
ALTER TABLE catalogo.veiculos ALTER COLUMN modelo TYPE varchar(250);
ALTER TABLE catalogo.veiculos ALTER COLUMN versao TYPE varchar(250);
