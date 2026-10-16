package repasse.phcauto.backend.fipe.internal.core.domain;
import java.time.LocalDate;
import repasse.phcauto.backend.fipe.internal.core.exception.FipeException;
public record PesquisaFipe(String tipo,String marca,String termo,Integer ano,LocalDate mes,int pagina,int tamanho) {
    public PesquisaFipe {
        if(tipo!=null && !java.util.Set.of("CARRO","MOTO","CAMINHAO","CAMINHONETE").contains(tipo))invalida();
        if("CAMINHONETE".equals(tipo))tipo="CARRO";
        if(pagina<0 || pagina>1000 || tamanho<1 || tamanho>50
                || (marca!=null && marca.length()>100) || (termo!=null && termo.length()>100)
                || (ano!=null && ano!=32000 && (ano<1886 || ano>2200))
                || (mes!=null && mes.getDayOfMonth()!=1))invalida();
    }
    private static void invalida(){throw new FipeException(FipeException.Motivo.INVALIDA,"Filtros FIPE inválidos; máximo 50 resultados por página");}
}
