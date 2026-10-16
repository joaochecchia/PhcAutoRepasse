package repasse.phcauto.backend.fipe.internal.core.domain;
import java.util.Set;
import repasse.phcauto.backend.fipe.internal.core.exception.FipeException;
public record ConsultaOpcoesFipe(String tipo,String marcaId,String modeloId,Integer referencia,String termo,int pagina,int tamanho){
    public ConsultaOpcoesFipe {
        if(termo==null)termo="";
        if(pagina<0 || pagina>1000 || tamanho<1 || tamanho>50 || termo.length()>100
                || (tipo!=null && (!Set.of("cars","motorcycles","trucks").contains(tipo) || referencia==null || referencia<1))
                || (tipo==null && (marcaId!=null || modeloId!=null || referencia!=null))
                || (marcaId!=null && !marcaId.matches("[0-9]{1,10}"))
                || (modeloId!=null && (!modeloId.matches("[0-9]{1,10}") || marcaId==null)))
            throw new FipeException(FipeException.Motivo.INVALIDA,"Filtros do catálogo externo inválidos");
    }
}
