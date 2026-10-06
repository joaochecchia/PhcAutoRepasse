package repasse.phcauto.backend.anuncios.internal.core.domain;

import java.math.BigDecimal;
import repasse.phcauto.backend.domain.model.catalogo.CondicaoVeiculo;
import repasse.phcauto.backend.domain.model.catalogo.TipoVeiculo;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;

public record BuscarAnunciosFiltro(TipoVeiculo tipoVeiculo, String cidade, String uf,
        String marca, String modelo, String termo, TipoPessoa tipoPessoa, String perfil, Long precoMinimoCentavos,
        Long precoMaximoCentavos, Integer anoMinimo, Integer anoMaximo, String cambio,
        String combustivel, String motorizacao, CondicaoVeiculo condicao,
        String tipoDirecao, String tracao, Boolean ipvaPago, Boolean blindado,
        Integer numeroPortas, BigDecimal cilindradaLitros, String tipoFreio,
        String carroceria, Double latitude, Double longitude, int pagina, int tamanho) {
    public BuscarAnunciosFiltro {
        if (pagina < 0) throw new IllegalArgumentException("Página não pode ser negativa");
        if (tamanho < 1 || tamanho > 52) throw new IllegalArgumentException("Tamanho deve estar entre 1 e 52");
        if ((latitude == null) != (longitude == null)) {
            throw new IllegalArgumentException("Latitude e longitude devem ser informadas juntas");
        }
        if (latitude != null) {
            if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90
                    || !Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
                throw new IllegalArgumentException("Coordenadas geográficas inválidas");
            }
        }
    }
    public boolean buscaPorProximidade() { return latitude != null; }
}
