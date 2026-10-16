package repasse.phcauto.backend.fipe.internal.core.domain;
import java.time.LocalDate;
public record SincronizarFipeCommand(String tipo,String marcaId,String modeloId,String anoId,Integer referencia,LocalDate mesReferencia) {
    public SincronizarFipeCommand {
        new ConsultaOpcoesFipe(tipo,marcaId,modeloId,referencia,"",0,50);
        if(anoId!=null && referencia==null)throw new repasse.phcauto.backend.fipe.internal.core.exception.FipeException(
            repasse.phcauto.backend.fipe.internal.core.exception.FipeException.Motivo.INVALIDA,"Informe a edição da referência");
        if(anoId!=null)new ConsultaExterna(tipo,marcaId,modeloId,anoId,referencia,mesReferencia);
    }
    public ConsultaOpcoesFipe opcoes(){return new ConsultaOpcoesFipe(tipo,marcaId,modeloId,referencia,"",0,50);}
    public ConsultaExterna consulta(){return new ConsultaExterna(tipo,marcaId,modeloId,anoId,referencia,mesReferencia);}
    public String chave(){return anoId==null?EscopoCatalogoFipe.chave(opcoes()):consulta().chave();}
}
