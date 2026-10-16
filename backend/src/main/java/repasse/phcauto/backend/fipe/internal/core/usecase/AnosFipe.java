package repasse.phcauto.backend.fipe.internal.core.usecase;
import java.util.*;
import repasse.phcauto.backend.fipe.internal.core.domain.*;
/** A FIPE identifica cada ano junto com o combustível ("2020-5" = "2020 Flex"); o formulário trabalha só com o ano. */
final class AnosFipe {
    private AnosFipe() {}
    static String ano(OpcaoFipe anoCombustivel){return anoCombustivel.codigo().split("-")[0];}
    static String combustivel(OpcaoFipe anoCombustivel){
        String nome=anoCombustivel.nome().trim();
        int espaco=nome.indexOf(' ');
        return espaco<0?"":nome.substring(espaco+1).trim();
    }
    static String nomeAno(String ano){return Integer.toString(ConsultaModelosAnoFipe.ZERO_KM).equals(ano)?"Zero km":ano;}
    static PaginaOpcoesFipe paginar(List<OpcaoFipe> opcoes,String termo,int pagina,int tamanho,boolean pendente){
        String filtro=termo.toLowerCase(Locale.ROOT);
        var filtradas=opcoes.stream().filter(o->o.nome().toLowerCase(Locale.ROOT).contains(filtro)).toList();
        int inicio=Math.min(pagina*tamanho,filtradas.size()),fim=Math.min(inicio+tamanho,filtradas.size());
        return new PaginaOpcoesFipe(filtradas.subList(inicio,fim),pagina,tamanho,fim<filtradas.size(),pendente);
    }
}
