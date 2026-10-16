package repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.persistence.repository;
import java.util.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.persistence.entity.FipeCatalogoEntity;
public interface FipeCatalogoRepository extends JpaRepository<FipeCatalogoEntity,UUID> {
    Optional<FipeCatalogoEntity> findByEscopoAndCodigoExterno(String escopo,String codigoExterno);
    List<FipeCatalogoEntity> findByEscopoAndCodigoExternoIn(String escopo,Collection<String> codigos);
    @Query("select c from FipeCatalogoEntity c where c.escopo=:escopo and lower(c.nome) like :termo escape '!' order by c.mesReferencia desc nulls last,c.nome,c.codigoExterno")
    List<FipeCatalogoEntity> pesquisar(String escopo,String termo,Pageable pagina);
    @Query(value="""
        select r.* from fipe.catalogo r where r.escopo='references'
        and exists (select 1 from fipe.catalogo b where b.escopo=r.codigo_externo||':'||:tipo)
        and exists (select 1 from fipe.sincronizacoes s
                    where s.chave=r.codigo_externo||':'||:tipo and s.status='CONCLUIDA')
        order by r.mes_referencia desc
        """,nativeQuery=true)
    List<FipeCatalogoEntity> edicoesComMarcas(String tipo,Pageable pagina);
}
