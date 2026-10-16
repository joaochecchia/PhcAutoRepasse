package repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.persistence;
import java.time.Clock;
import java.util.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import jakarta.persistence.*;
import repasse.phcauto.backend.fipe.internal.core.domain.*;
import repasse.phcauto.backend.fipe.internal.core.exception.FipeException;
import repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.persistence.entity.FipeCatalogoEntity;
import repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.persistence.repository.FipeCatalogoRepository;
@Repository
@Transactional(transactionManager="writeTransactionManager",readOnly=true)
public class JpaOpcoesFipeGateway implements repasse.phcauto.backend.fipe.internal.core.gateway.EdicaoAtivaFipeGateway {
    private final FipeCatalogoRepository repository;
    private final Clock clock;
    @PersistenceContext(unitName="write") private EntityManager em;
    public JpaOpcoesFipeGateway(FipeCatalogoRepository repository,Clock clock){this.repository=repository;this.clock=clock;}
    @Override public Optional<OpcaoFipe> buscar(String tipo) {
        return repository.edicoesComMarcas(tipo,PageRequest.of(0,1)).stream().findFirst().map(FipeCatalogoEntity::opcao);
    }
    public List<OpcaoFipe> listar(ConsultaOpcoesFipe c,int pagina,int tamanho) {
        String termo="%"+c.termo().toLowerCase(Locale.ROOT).replace("!","!!").replace("%","!%").replace("_","!_")+"%";
        return repository.pesquisar(EscopoCatalogoFipe.chave(c),termo,PageRequest.of(pagina,tamanho)).stream().map(FipeCatalogoEntity::opcao).toList();
    }
    /** Só um carregamento completo do fornecedor marca o escopo; linhas parciais não contam. */
    public boolean escopoCarregado(ConsultaOpcoesFipe c) {
        return !em.createNativeQuery("select 1 from fipe.escopos_carregados where escopo=:escopo")
            .setParameter("escopo",EscopoCatalogoFipe.chave(c)).setMaxResults(1).getResultList().isEmpty();
    }
    @Transactional(transactionManager="writeTransactionManager")
    public void marcarCarregado(ConsultaOpcoesFipe c,int quantidade) {
        em.createNativeQuery("""
            insert into fipe.escopos_carregados(escopo,quantidade,carregado_em) values(:escopo,:quantidade,now())
            on conflict(escopo) do update set quantidade=excluded.quantidade,carregado_em=excluded.carregado_em
            """).setParameter("escopo",EscopoCatalogoFipe.chave(c)).setParameter("quantidade",quantidade).executeUpdate();
    }
    /** A combinação precisa ter vindo do catálogo: ano listado para o modelo, ou modelo listado para a marca naquele ano. */
    public void validar(ConsultaExterna c) {
        var anosDoModelo=new ConsultaOpcoesFipe(c.tipo(),c.marcaId(),c.modeloId(),c.referencia(),"",0,50);
        var modelosDoAno=ConsultaOpcoesFipe.modelosDoAno(c.tipo(),c.marcaId(),c.anoId(),c.referencia());
        if((repository.findByEscopoAndCodigoExterno(EscopoCatalogoFipe.chave(anosDoModelo),c.anoId()).isEmpty()
                && repository.findByEscopoAndCodigoExterno(EscopoCatalogoFipe.chave(modelosDoAno),c.modeloId()).isEmpty())
            || repository.findByEscopoAndCodigoExterno("references",Integer.toString(c.referencia()))
                .map(e->!c.mesReferencia().equals(e.opcao().mesReferencia())).orElse(true))
            throw new FipeException(FipeException.Motivo.INVALIDA,"Combinação de marca, modelo, ano e mês ausente no catálogo local");
    }
    @Transactional(transactionManager="writeTransactionManager")
    public void salvar(ConsultaOpcoesFipe c,List<OpcaoFipe> opcoes) {
        String escopo=EscopoCatalogoFipe.chave(c);
        em.createNativeQuery("select pg_advisory_xact_lock(hashtextextended(:chave,0))").setParameter("chave","fipe-catalogo:"+escopo).getSingleResult();
        UUID pai=c.tipo()==null?null:repository.findByEscopoAndCodigoExterno(EscopoCatalogoFipe.pai(c),EscopoCatalogoFipe.codigoPai(c))
            .orElseThrow(()->new FipeException(FipeException.Motivo.INVALIDA,"Sincronize primeiro o nível anterior do catálogo")).getId();
        if(opcoes.size()>100)throw new FipeException(FipeException.Motivo.INVALIDA,"Lote de catálogo excede 100 itens");
        var existentes=new java.util.HashMap<String,FipeCatalogoEntity>();
        repository.findByEscopoAndCodigoExternoIn(escopo,opcoes.stream().map(OpcaoFipe::codigo).toList())
            .forEach(e->existentes.put(e.opcao().codigo(),e));
        for(var o:opcoes) {
            if(o.codigo()==null || !o.codigo().matches("[0-9]{1,10}(-[0-9])?") || o.nome()==null || o.nome().isBlank() || o.nome().length()>250)
                throw new FipeException(FipeException.Motivo.INVALIDA,"Resposta de catálogo inválida");
            var existente=existentes.get(o.codigo());
            if(existente!=null) existente.atualizar(o,clock.instant());
            else repository.save(FipeCatalogoEntity.criar(escopo,pai,o,clock.instant()));
        }
    }
}
