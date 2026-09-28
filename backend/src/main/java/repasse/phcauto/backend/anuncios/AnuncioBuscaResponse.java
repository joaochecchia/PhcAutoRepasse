package repasse.phcauto.backend.anuncios;

import java.math.BigDecimal;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.catalogo.CondicaoVeiculo;
import repasse.phcauto.backend.domain.model.catalogo.TipoPreco;
import repasse.phcauto.backend.domain.model.catalogo.TipoVeiculo;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;

/** Resumo público da busca; não expõe o endereço completo do veículo. */
public record AnuncioBuscaResponse(UUID anuncioId, UUID veiculoId, UUID anuncianteId,
        TipoVeiculo tipoVeiculo, String fabricante, String modelo, Integer anoFabricacao,
        Integer anoModelo, CondicaoVeiculo condicao, String titulo, TipoPreco tipoPreco,
        Long precoCentavos, String cidade, String uf, TipoPessoa tipoPessoa,
        String nomePerfil, String cambio, String combustivel, String motorizacao,
        String tipoDirecao, String tracao, Boolean ipvaPago, Boolean blindado,
        Integer numeroPortas, BigDecimal cilindradaLitros, Integer cilindradas,
        String tipoFreio, String carroceria) { }
