package repasse.phcauto.backend.fipe.internal.core.domain;
import java.util.Set;
import repasse.phcauto.backend.fipe.internal.core.exception.FipeException;
/**
 * Um nível do catálogo: edições, marcas, modelos da marca, anos do modelo, anos da marca ({@code anosDaMarca})
 * ou modelos da marca em um ano/combustível ({@code anoId}, ex.: 2020-5).
 */
public record ConsultaOpcoesFipe(String tipo,String marcaId,String modeloId,Integer referencia,String termo,int pagina,int tamanho,
        String anoId,boolean anosDaMarca){
    public ConsultaOpcoesFipe {
        if(termo==null)termo="";
        if(pagina<0 || pagina>1000 || tamanho<1 || tamanho>50 || termo.length()>100
                || (tipo!=null && (!Set.of("cars","motorcycles","trucks").contains(tipo) || referencia==null || referencia<1))
                || (tipo==null && (marcaId!=null || modeloId!=null || referencia!=null))
                || (marcaId!=null && !marcaId.matches("[0-9]{1,10}"))
                || (modeloId!=null && (!modeloId.matches("[0-9]{1,10}") || marcaId==null))
                || (anoId!=null && (!anoId.matches("[0-9]{4,5}-[0-9]") || marcaId==null || modeloId!=null || anosDaMarca))
                || (anosDaMarca && (marcaId==null || modeloId!=null)))
            throw new FipeException(FipeException.Motivo.INVALIDA,"Filtros do catálogo externo inválidos");
    }
    public ConsultaOpcoesFipe(String tipo,String marcaId,String modeloId,Integer referencia,String termo,int pagina,int tamanho){
        this(tipo,marcaId,modeloId,referencia,termo,pagina,tamanho,null,false);
    }
    public static ConsultaOpcoesFipe anosDaMarca(String tipo,String marcaId,Integer referencia){
        return new ConsultaOpcoesFipe(tipo,marcaId,null,referencia,"",0,50,null,true);
    }
    public static ConsultaOpcoesFipe modelosDoAno(String tipo,String marcaId,String anoId,Integer referencia){
        return new ConsultaOpcoesFipe(tipo,marcaId,null,referencia,"",0,50,anoId,false);
    }
}
