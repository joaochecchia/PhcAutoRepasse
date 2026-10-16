package repasse.phcauto.backend.fipe.internal.core.domain;
import repasse.phcauto.backend.fipe.internal.core.exception.FipeException;
/** Modelos de uma marca em um ano-modelo, somando todos os combustíveis; 32000 é o código FIPE de zero km. */
public record ConsultaModelosAnoFipe(String tipo,String marcaId,int ano,Integer referencia,String termo,int pagina,int tamanho) {
    public ConsultaModelosAnoFipe {
        if(termo==null)termo="";
        if(tipo==null || marcaId==null || (ano!=ConsultaModelosAnoFipe.ZERO_KM && (ano<1900 || ano>2100)))
            throw new FipeException(FipeException.Motivo.INVALIDA,"Informe marca e ano do modelo");
        new ConsultaOpcoesFipe(tipo,marcaId,null,referencia,termo,pagina,tamanho);
    }
    public static final int ZERO_KM=32000;
    public ConsultaOpcoesFipe anosDaMarca(){return ConsultaOpcoesFipe.anosDaMarca(tipo,marcaId,referencia);}
    public ConsultaOpcoesFipe modelos(String anoId){return ConsultaOpcoesFipe.modelosDoAno(tipo,marcaId,anoId,referencia);}
}
