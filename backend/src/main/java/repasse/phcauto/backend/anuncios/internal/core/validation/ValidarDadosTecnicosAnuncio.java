package repasse.phcauto.backend.anuncios.internal.core.validation;

import repasse.phcauto.backend.domain.model.catalogo.CondicaoVeiculo;

import repasse.phcauto.backend.anuncios.internal.core.domain.CriarAnuncioCommand;
import repasse.phcauto.backend.anuncios.internal.core.exception.AnuncioInvalidoException;
/** Regras puras compartilhadas pela criação e pelo resultado mesclado do PATCH. */
public final class ValidarDadosTecnicosAnuncio {
    private ValidarDadosTecnicosAnuncio() { }

    public static void validar(CriarAnuncioCommand c) {
        obrigatorio(c.anoFabricacao(), "anoFabricacao");
        obrigatorio(c.anoModelo(), "anoModelo");
        if (c.condicao() == null) throw new AnuncioInvalidoException("Condição do veículo é obrigatória");
        obrigatorio(c.historicoLeilao(), "historicoLeilao");
        obrigatorio(c.historicoSinistro(), "historicoSinistro");

        validarComDetalhes(c.tipoVeiculo(), c.condicao(), c.detalhes());
    }

    static void validarComDetalhes(repasse.phcauto.backend.domain.model.catalogo.TipoVeiculo tipo,
            CondicaoVeiculo condicao, CriarAnuncioCommand.DetalhesVeiculo detalhes) {
        Integer km = null;
        if (detalhes instanceof CriarAnuncioCommand.Carro d) {
            placa(d.placa(), d.exibirPlacaCompleta());
            km = d.quilometragem(); quilometragem(km);
            texto(d.carroceria(), "carroceria"); texto(d.cambio(), "cambio");
            texto(d.combustivel(), "combustivel"); texto(d.motorizacao(), "motorizacao");
            positivoOpcional(d.cilindradaLitros(), "cilindradaLitros"); positivoOpcional(d.numeroPortas(), "numeroPortas");
            positivoOpcional(d.numeroLugares(), "numeroLugares");
            obrigatorio(d.ipvaPago(), "ipvaPago"); obrigatorio(d.unicoDono(), "unicoDono");
            motorizacao(d.motorizacao());
        } else if (detalhes instanceof CriarAnuncioCommand.Moto d) {
            placa(d.placa(), d.exibirPlacaCompleta());
            km = d.quilometragem(); quilometragem(km); positivoOpcional(d.cilindradas(), "cilindradas");
            obrigatorio(d.ipvaPago(), "ipvaPago");
            texto(d.categoria(), "categoria"); texto(d.cambio(), "cambio"); texto(d.combustivel(), "combustivel");
        } else if (detalhes instanceof CriarAnuncioCommand.Caminhao d) {
            placa(d.placa(), d.exibirPlacaCompleta());
            km = d.quilometragem(); quilometragem(km);
            texto(d.configuracao(), "configuracao"); texto(d.carroceria(), "carroceria");
            texto(d.cambio(), "cambio"); texto(d.combustivel(), "combustivel");
            positivo(d.numeroEixos(), "numeroEixos");
            obrigatorio(d.ipvaPago(), "ipvaPago");
        } else if (detalhes instanceof CriarAnuncioCommand.Caminhonete d) {
            placa(d.placa(), d.exibirPlacaCompleta());
            km = d.quilometragem(); quilometragem(km);
            texto(d.tipoCabine(), "tipoCabine"); texto(d.carroceria(), "carroceria");
            texto(d.cambio(), "cambio"); texto(d.combustivel(), "combustivel");
            motorizacao(d.motorizacao()); positivoOpcional(d.cilindradaLitros(), "cilindradaLitros");
            positivoOpcional(d.numeroPortas(), "numeroPortas");
            obrigatorio(d.ipvaPago(), "ipvaPago"); obrigatorio(d.unicoDono(), "unicoDono");
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
    private static void positivoOpcional(Number valor, String campo) {
        if (valor != null) positivo(valor, campo);
    }
    private static void motorizacao(String valor) {
        if (valor == null || !valor.matches("[0-9]{1,2}[.,][0-9]{1,2}( Turbo)?")
                || Double.parseDouble(valor.replace(" Turbo", "").replace(',', '.')) <= 0)
            throw new AnuncioInvalidoException("Motorização deve informar litros, como 1.0 ou 4.1, com Turbo opcional");
    }
    private static void placa(String valor, Boolean exibirCompleta) {
        if (valor == null || !valor.matches("[A-Z]{3}[0-9][A-Z0-9][0-9]{2}"))
            throw new AnuncioInvalidoException("placa deve conter os 7 caracteres no padrão brasileiro, sem pontuação");
        if (exibirCompleta == null) throw new AnuncioInvalidoException("exibirPlacaCompleta é obrigatório");
    }
    private static void quilometragem(Integer valor) {
        if (valor == null || valor < 0) throw new AnuncioInvalidoException("quilometragem deve ser informada e não negativa");
    }
}
