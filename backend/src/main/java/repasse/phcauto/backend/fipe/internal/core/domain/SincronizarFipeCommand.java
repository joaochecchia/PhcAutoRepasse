package repasse.phcauto.backend.fipe.internal.core.domain;
import java.time.LocalDate;
/**
 * {@code anoId} preenchido = consultar o preço de marca/modelo/ano. Sem ele, o comando carrega um nível do catálogo;
 * {@code anoCatalogoId} e {@code anosDaMarca} selecionam os níveis por ano (modelos de um ano/combustível, anos da marca).
 */
public record SincronizarFipeCommand(String tipo,String marcaId,String modeloId,String anoId,Integer referencia,LocalDate mesReferencia,
        String anoCatalogoId,boolean anosDaMarca) {
    public SincronizarFipeCommand {
        new ConsultaOpcoesFipe(tipo,marcaId,modeloId,referencia,"",0,50,anoCatalogoId,anosDaMarca);
        if(anoId!=null && referencia==null)throw new repasse.phcauto.backend.fipe.internal.core.exception.FipeException(
            repasse.phcauto.backend.fipe.internal.core.exception.FipeException.Motivo.INVALIDA,"Informe a edição da referência");
        if(anoId!=null && (anoCatalogoId!=null || anosDaMarca))throw new repasse.phcauto.backend.fipe.internal.core.exception.FipeException(
            repasse.phcauto.backend.fipe.internal.core.exception.FipeException.Motivo.INVALIDA,"Consulta de preço não carrega nível do catálogo");
        if(anoId!=null)new ConsultaExterna(tipo,marcaId,modeloId,anoId,referencia,mesReferencia);
    }
    public SincronizarFipeCommand(String tipo,String marcaId,String modeloId,String anoId,Integer referencia,LocalDate mesReferencia) {
        this(tipo,marcaId,modeloId,anoId,referencia,mesReferencia,null,false);
    }
    public static SincronizarFipeCommand opcoes(ConsultaOpcoesFipe c) {
        return new SincronizarFipeCommand(c.tipo(),c.marcaId(),c.modeloId(),null,c.referencia(),null,c.anoId(),c.anosDaMarca());
    }
    public ConsultaOpcoesFipe opcoes(){return new ConsultaOpcoesFipe(tipo,marcaId,modeloId,referencia,"",0,50,anoCatalogoId,anosDaMarca);}
    public ConsultaExterna consulta(){return new ConsultaExterna(tipo,marcaId,modeloId,anoId,referencia,mesReferencia);}
    public String chave(){return anoId==null?EscopoCatalogoFipe.chave(opcoes()):consulta().chave();}
}
