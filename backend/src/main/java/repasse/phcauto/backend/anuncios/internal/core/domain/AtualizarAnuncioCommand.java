package repasse.phcauto.backend.anuncios.internal.core.domain;

import repasse.phcauto.backend.domain.model.catalogo.TipoPreco;

public record AtualizarAnuncioCommand(String fabricante, String modelo, String versao,
        Integer anoFabricacao, Integer anoModelo, String cor, String identificadorPublico,
        repasse.phcauto.backend.domain.model.catalogo.CondicaoVeiculo condicao, String tipoFreio,
        Boolean historicoLeilao, Boolean historicoSinistro,
        String titulo, String descricao, TipoPreco tipoPreco, Long precoCentavos,
        Boolean aceitaTroca, Boolean publicarAgora, CriarAnuncioCommand.Endereco endereco,
        CriarAnuncioCommand.DetalhesVeiculo detalhes,
        java.util.UUID fipeReferenciaId, boolean removerFipe) {
    public AtualizarAnuncioCommand(String fabricante, String modelo, String versao,
        Integer anoFabricacao, Integer anoModelo, String cor, String identificadorPublico,
        repasse.phcauto.backend.domain.model.catalogo.CondicaoVeiculo condicao, String tipoFreio,
        Boolean historicoLeilao, Boolean historicoSinistro,
        String titulo, String descricao, TipoPreco tipoPreco, Long precoCentavos,
        Boolean aceitaTroca, Boolean publicarAgora, CriarAnuncioCommand.Endereco endereco,
        CriarAnuncioCommand.DetalhesVeiculo detalhes) {
        this(fabricante, modelo, versao, anoFabricacao, anoModelo, cor, identificadorPublico, condicao, tipoFreio, historicoLeilao, historicoSinistro, titulo, descricao, tipoPreco, precoCentavos, aceitaTroca, publicarAgora, endereco, detalhes, null, false);
    }
 }
