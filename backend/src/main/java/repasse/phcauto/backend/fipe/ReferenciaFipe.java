package repasse.phcauto.backend.fipe;

import java.time.LocalDate;
import java.util.UUID;

/** Contrato público de uma referência persistida, nunca de um preço enviado pelo anunciante. */
public record ReferenciaFipe(UUID id, String codigoFipe, String tipo, String marca,
        String modelo, int anoModelo, boolean zeroKm, String combustivel,
        LocalDate mesReferencia, long valorCentavos, String fonte) { }
