package repasse.phcauto.backend.fipe.internal.core.domain;

import java.util.Locale;
import java.util.Set;
import repasse.phcauto.backend.fipe.internal.core.exception.FipeException;

public record BuscarSugestoesFipe(String tipoVeiculo, String fabricante, String modelo,
        String versao, int anoModelo, String condicao, String combustivel) {
    public BuscarSugestoesFipe {
        tipoVeiculo = texto(tipoVeiculo, 20).toUpperCase(Locale.ROOT);
        fabricante = texto(fabricante, 100);
        modelo = texto(modelo, 120);
        versao = texto(versao, 180);
        condicao = texto(condicao, 20).toUpperCase(Locale.ROOT);
        combustivel = texto(combustivel, 60);
        if (!Set.of("CARRO", "MOTO", "CAMINHAO", "CAMINHONETE").contains(tipoVeiculo)
                || !Set.of("USADO", "ZERO_KM").contains(condicao)
                || anoModelo < 1886 || anoModelo > 2200)
            throw new FipeException(FipeException.Motivo.INVALIDA, "Dados do veículo inválidos para consulta FIPE");
    }
    private static String texto(String valor, int limite) {
        if (valor == null || valor.isBlank() || valor.trim().length() > limite)
            throw new FipeException(FipeException.Motivo.INVALIDA,
                    "Marca, modelo, versão e combustível são obrigatórios para consultar a FIPE");
        return valor.trim();
    }
    public String tipoFornecedor() {
        return switch (tipoVeiculo) {
            case "MOTO" -> "motorcycles";
            case "CAMINHAO" -> "trucks";
            default -> "cars";
        };
    }
}
