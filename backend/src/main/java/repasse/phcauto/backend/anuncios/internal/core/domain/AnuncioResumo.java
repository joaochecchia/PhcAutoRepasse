package repasse.phcauto.backend.anuncios.internal.core.domain;

import java.math.BigDecimal;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.catalogo.CondicaoVeiculo;
import repasse.phcauto.backend.domain.model.catalogo.TipoPreco;
import repasse.phcauto.backend.domain.model.catalogo.TipoVeiculo;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;

/** Projeção pura de domínio da busca pública, sem endereço completo. */
public record AnuncioResumo(UUID anuncioId, UUID veiculoId, UUID anuncianteId,
        TipoVeiculo tipoVeiculo, String fabricante, String modelo, Integer anoFabricacao,
        Integer anoModelo, CondicaoVeiculo condicao, String titulo, TipoPreco tipoPreco,
        Long precoCentavos, String cidade, String uf, TipoPessoa tipoPessoa,
        String nomePerfil, String cambio, String combustivel, String motorizacao,
        String tipoDirecao, String tracao, Boolean ipvaPago, Boolean blindado,
        Integer numeroPortas, BigDecimal cilindradaLitros, Integer cilindradas,
        String tipoFreio, String carroceria, Boolean historicoLeilao, Boolean historicoSinistro,
        Double distanciaKm, UUID fotoPrincipalId,
        repasse.phcauto.backend.domain.model.catalogo.FipeVeiculo fipe) {
    public AnuncioResumo(UUID anuncioId, UUID veiculoId, UUID anuncianteId,
        TipoVeiculo tipoVeiculo, String fabricante, String modelo, Integer anoFabricacao,
        Integer anoModelo, CondicaoVeiculo condicao, String titulo, TipoPreco tipoPreco,
        Long precoCentavos, String cidade, String uf, TipoPessoa tipoPessoa,
        String nomePerfil, String cambio, String combustivel, String motorizacao,
        String tipoDirecao, String tracao, Boolean ipvaPago, Boolean blindado,
        Integer numeroPortas, BigDecimal cilindradaLitros, Integer cilindradas,
        String tipoFreio, String carroceria, Boolean historicoLeilao, Boolean historicoSinistro,
        Double distanciaKm, UUID fotoPrincipalId) {
        this(anuncioId, veiculoId, anuncianteId, tipoVeiculo, fabricante, modelo, anoFabricacao, anoModelo, condicao, titulo, tipoPreco, precoCentavos, cidade, uf, tipoPessoa, nomePerfil, cambio, combustivel, motorizacao, tipoDirecao, tracao, ipvaPago, blindado, numeroPortas, cilindradaLitros, cilindradas, tipoFreio, carroceria, historicoLeilao, historicoSinistro, distanciaKm, fotoPrincipalId, null);
    }
 }
