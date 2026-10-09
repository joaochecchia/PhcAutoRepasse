ALTER TABLE catalogo.veiculos
    ADD COLUMN historico_leilao boolean,
    ADD COLUMN historico_sinistro boolean;

COMMENT ON COLUMN catalogo.veiculos.historico_leilao IS
    'Indica se o veículo possui histórico de leilão; nulo somente para registros legados.';
COMMENT ON COLUMN catalogo.veiculos.historico_sinistro IS
    'Indica se o veículo possui histórico de sinistro; nulo somente para registros legados.';
