package repasse.phcauto.backend.localizacao.internal.core.usecase;

import java.util.Locale;
import repasse.phcauto.backend.localizacao.internal.core.exception.LocalizacaoNaoEncontradaException;
import repasse.phcauto.backend.localizacao.internal.core.domain.Municipio;
import repasse.phcauto.backend.localizacao.internal.core.gateway.ConsultarMunicipioGateway;

public final class ConsultarMunicipio implements ConsultarMunicipioUseCase {
    private final ConsultarMunicipioGateway gateway;

    public ConsultarMunicipio(ConsultarMunicipioGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public Municipio buscarPorCidadeEUf(String cidade, String uf) {
        if (cidade == null || cidade.isBlank()) {
            throw new LocalizacaoNaoEncontradaException("Cidade obrigatória");
        }
        if (uf == null || !uf.strip().toUpperCase(Locale.ROOT).matches("[A-Z]{2}")) {
            throw new LocalizacaoNaoEncontradaException("UF inválida");
        }
        return gateway.buscarPorCidadeEUf(cidade.strip(), uf.strip().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new LocalizacaoNaoEncontradaException(
                        "Município brasileiro não encontrado para a cidade e UF informadas"));
    }

    @Override
    public Municipio buscarPorCodigoIbge(int codigoIbge) {
        return gateway.buscarPorCodigoIbge(codigoIbge)
                .orElseThrow(() -> new LocalizacaoNaoEncontradaException("Município brasileiro não encontrado"));
    }
}
