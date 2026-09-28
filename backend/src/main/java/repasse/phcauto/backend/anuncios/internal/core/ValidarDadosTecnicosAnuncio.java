package repasse.phcauto.backend.anuncios.internal.core;

import repasse.phcauto.backend.domain.model.catalogo.CondicaoVeiculo;

/** Regras puras compartilhadas pela criação e pelo resultado mesclado do PATCH. */
public final class ValidarDadosTecnicosAnuncio {
    private ValidarDadosTecnicosAnuncio() { }

    public static void validar(CriarAnuncioCommand c) {
        obrigatorio(c.anoFabricacao(), "anoFabricacao");
        obrigatorio(c.anoModelo(), "anoModelo");
        if (c.condicao() == null) throw new AnuncioInvalidoException("Condição do veículo é obrigatória");

        validarComDetalhes(c.tipoVeiculo(), c.condicao(), c.detalhes());
    }

    static void validarComDetalhes(repasse.phcauto.backend.domain.model.catalogo.TipoVeiculo tipo,
            CondicaoVeiculo condicao, CriarAnuncioCommand.DetalhesVeiculo detalhes) {
        Integer km = null;
        if (detalhes instanceof CriarAnuncioCommand.Carro d) {
            km = d.quilometragem(); quilometragem(km);
            texto(d.carroceria(), "carroceria"); texto(d.cambio(), "cambio");
            texto(d.combustivel(), "combustivel"); texto(d.motorizacao(), "motorizacao");
            positivo(d.cilindradaLitros(), "cilindradaLitros"); positivo(d.numeroPortas(), "numeroPortas");
        } else if (detalhes instanceof CriarAnuncioCommand.Moto d) {
            km = d.quilometragem(); quilometragem(km); positivo(d.cilindradas(), "cilindradas");
            texto(d.categoria(), "categoria"); texto(d.cambio(), "cambio"); texto(d.combustivel(), "combustivel");
        } else if (detalhes instanceof CriarAnuncioCommand.Caminhao d) {
            km = d.quilometragem(); quilometragem(km);
            texto(d.configuracao(), "configuracao"); texto(d.carroceria(), "carroceria");
            texto(d.cambio(), "cambio"); texto(d.combustivel(), "combustivel");
            positivo(d.numeroEixos(), "numeroEixos");
        } else if (detalhes instanceof CriarAnuncioCommand.Caminhonete d) {
            km = d.quilometragem(); quilometragem(km);
            texto(d.tipoCabine(), "tipoCabine"); texto(d.carroceria(), "carroceria");
            texto(d.cambio(), "cambio"); texto(d.combustivel(), "combustivel");
            texto(d.motorizacao(), "motorizacao"); positivo(d.cilindradaLitros(), "cilindradaLitros");
            positivo(d.numeroPortas(), "numeroPortas");
        }
        if (condicao == CondicaoVeiculo.ZERO_KM && km != null && km != 0)
            throw new AnuncioInvalidoException("Veículo zero km deve ter quilometragem igual a zero");
    }

    private static void texto(String valor, String campo) {
        if (valor == null || valor.isBlank()) throw new AnuncioInvalidoException(campo + " é obrigatório");
    }
    private static void obrigatorio(Object valor, String campo) {
        if (valor == null) throw new AnuncioInvalidoException(campo + " é obrigatório");
    }
    private static void positivo(Number valor, String campo) {
        if (valor == null || valor.doubleValue() <= 0) throw new AnuncioInvalidoException(campo + " deve ser positivo");
    }
    private static void quilometragem(Integer valor) {
        if (valor == null || valor < 0) throw new AnuncioInvalidoException("quilometragem deve ser informada e não negativa");
    }
}
