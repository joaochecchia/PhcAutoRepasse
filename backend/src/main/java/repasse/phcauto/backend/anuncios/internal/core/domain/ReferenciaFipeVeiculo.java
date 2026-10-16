package repasse.phcauto.backend.anuncios.internal.core.domain;
import repasse.phcauto.backend.domain.model.catalogo.FipeVeiculo;
public record ReferenciaFipeVeiculo(FipeVeiculo snapshot,String tipo,String marca,String modelo,
        int anoModelo,boolean zeroKm,String combustivel) { }
