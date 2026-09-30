package repasse.phcauto.backend.anuncios.internal.core.domain;

import repasse.phcauto.backend.domain.model.catalogo.TipoPreco;

public record AtualizarAnuncioCommand(String fabricante, String modelo, String versao,
        Integer anoFabricacao, Integer anoModelo, String cor, String identificadorPublico,
        repasse.phcauto.backend.domain.model.catalogo.CondicaoVeiculo condicao, String tipoFreio,
        String titulo, String descricao, TipoPreco tipoPreco, Long precoCentavos,
        Boolean aceitaTroca, Boolean publicarAgora, CriarAnuncioCommand.Endereco endereco,
        CriarAnuncioCommand.DetalhesVeiculo detalhes) { }
