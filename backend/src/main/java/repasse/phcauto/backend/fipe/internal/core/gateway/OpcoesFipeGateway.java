package repasse.phcauto.backend.fipe.internal.core.gateway;
import java.util.*;
import repasse.phcauto.backend.fipe.internal.core.domain.*;
public interface OpcoesFipeGateway {
    List<OpcaoFipe> opcoes(ConsultaOpcoesFipe consulta);
    default PaginaOpcoesFipe pagina(ConsultaOpcoesFipe c) {
        var filtradas=opcoes(c).stream().filter(o->o.nome().toLowerCase(Locale.ROOT).contains(c.termo().toLowerCase(Locale.ROOT))).toList();
        int inicio=Math.min(c.pagina()*c.tamanho(),filtradas.size()),fim=Math.min(inicio+c.tamanho(),filtradas.size());
        return new PaginaOpcoesFipe(filtradas.subList(inicio,fim),c.pagina(),c.tamanho(),fim<filtradas.size());
    }
}
