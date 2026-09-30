package repasse.phcauto.backend.anuncios.internal.core.domain;

import java.math.BigDecimal;
import repasse.phcauto.backend.domain.model.catalogo.CondicaoVeiculo;
import repasse.phcauto.backend.domain.model.catalogo.TipoVeiculo;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;

public record BuscarAnunciosFiltro(TipoVeiculo tipoVeiculo, String cidade, String uf,
        String marca, TipoPessoa tipoPessoa, String perfil, Long precoMinimoCentavos,
        Long precoMaximoCentavos, Integer anoMinimo, Integer anoMaximo, String cambio,
        String combustivel, String motorizacao, CondicaoVeiculo condicao,
        String tipoDirecao, String tracao, Boolean ipvaPago, Boolean blindado,
        Integer numeroPortas, BigDecimal cilindradaLitros, String tipoFreio,
        String carroceria, int pagina, int tamanho) { }
