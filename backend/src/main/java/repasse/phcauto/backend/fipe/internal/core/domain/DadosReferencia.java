package repasse.phcauto.backend.fipe.internal.core.domain;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Locale;
import repasse.phcauto.backend.fipe.internal.core.exception.FipeException;
import static repasse.phcauto.backend.fipe.internal.core.exception.FipeException.Motivo.INVALIDA;

/** Ano 32000 é a categoria zero km do fornecedor, não um ano civil. */
public record DadosReferencia(String codigoFipe, String tipo, String marca, String modelo,
        int anoModelo, String combustivel, LocalDate mesReferencia, long valorCentavos) {
    public DadosReferencia {
        codigoFipe = texto(codigoFipe, 16); tipo = texto(tipo, 16).toUpperCase(Locale.ROOT);
        marca = texto(marca, 100); modelo = texto(modelo, 250); combustivel = texto(combustivel, 60).toUpperCase(Locale.ROOT);
        if (!codigoFipe.matches("[0-9]{6}-[0-9]")) throw new FipeException(INVALIDA, "Código FIPE inválido");
        if (!java.util.Set.of("CARRO", "MOTO", "CAMINHAO").contains(tipo))
            throw new FipeException(INVALIDA, "Tipo FIPE deve ser CARRO, MOTO ou CAMINHAO (caminhonetes usam CARRO)");
        if (anoModelo != 32000 && (anoModelo < 1886 || anoModelo > 2200))
            throw new FipeException(INVALIDA, "Ano FIPE inválido");
        if (mesReferencia == null || mesReferencia.getDayOfMonth() != 1 || mesReferencia.getYear() < 2000)
            throw new FipeException(INVALIDA, "Referência deve ser o primeiro dia do mês, desde 2000");
        if (valorCentavos <= 0 || valorCentavos > 100_000_000_000L)
            throw new FipeException(INVALIDA, "Valor FIPE inválido");
    }
    private static String texto(String v, int max) {
        if (v == null || v.isBlank() || v.trim().length() > max)
            throw new FipeException(INVALIDA, "Campos FIPE obrigatórios ausentes ou muito longos");
        return v.trim();
    }
    public boolean zeroKm() { return anoModelo == 32000; }
}
