package repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.persistence.repository;
import java.util.*;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
/** Fila técnica exclusiva do write. Não participa da projeção de entidades de negócio. */
@Repository
public class FilaSincronizacaoFipe {
    private final JdbcTemplate jdbc;
    public FilaSincronizacaoFipe(@Qualifier("writeDataSource")DataSource ds){jdbc=new JdbcTemplate(ds);}
    public UUID enviar(String chave,String comando) {
        return jdbc.queryForObject("""
            insert into fipe.sincronizacoes(id,chave,comando) values(?,?,?)
            on conflict(chave) do update set comando=excluded.comando,
            status=case when fipe.sincronizacoes.status='EXECUTANDO' then 'EXECUTANDO' else 'PENDENTE' end,
            tentativas=case when fipe.sincronizacoes.status='EXECUTANDO' then fipe.sincronizacoes.tentativas else 0 end,
            executar_em=now(),atualizado_em=now() returning id
            """,UUID.class,UUID.randomUUID(),chave,comando);
    }
    public void reagendarAtuais() {
        // Atualiza somente escopos já autorizados pelo administrador no mês corrente.
        // Nunca descobre/varre modelos nem percorre meses históricos automaticamente.
        jdbc.update("""
            update fipe.sincronizacoes s set status='PENDENTE',tentativas=0,executar_em=now()
            where s.status='CONCLUIDA' and s.atualizado_em<now()-interval '1 day'
            and (s.chave='references' or exists(select 1 from fipe.catalogo c where c.escopo='references'
                and c.mes_referencia=date_trunc('month',current_date)::date
                and (s.chave like c.codigo_externo||':%' or s.chave like '%:'||c.codigo_externo)))
            """);
    }
    public Optional<Tarefa> adquirir() {
        jdbc.update("update fipe.sincronizacoes set status='FALHOU',erro='TENTATIVAS_ESGOTADAS',atualizado_em=now() where status='EXECUTANDO' and lease_ate<now() and tentativas>=5");
        UUID lease=UUID.randomUUID();
        return jdbc.query("""
            update fipe.sincronizacoes set status='EXECUTANDO',lease=?,lease_ate=now()+interval '2 minutes',
            tentativas=tentativas+1,atualizado_em=now()
            where id=(select id from fipe.sincronizacoes where tentativas<5 and
            ((status='PENDENTE' and executar_em<=now()) or (status='EXECUTANDO' and lease_ate<now()))
            order by executar_em for update skip locked limit 1)
            returning id,comando,tentativas
            """,(rs,n)->new Tarefa(rs.getObject(1,UUID.class),lease,rs.getString(2),rs.getInt(3)),lease).stream().findFirst();
    }
    public void renovar(Tarefa t) {
        int atualizadas=jdbc.update("update fipe.sincronizacoes set lease_ate=now()+interval '2 minutes' where id=? and lease=? and status='EXECUTANDO' and lease_ate>now()",t.id(),t.lease());
        if(atualizadas!=1)throw new IllegalStateException("Lease de sincronização expirou");
    }
    public void concluir(Tarefa t){jdbc.update("update fipe.sincronizacoes set status='CONCLUIDA',erro=null,atualizado_em=now() where id=? and lease=?",t.id(),t.lease());}
    public void falhar(Tarefa t,String erro,boolean transitoria) {
        jdbc.update("""
            update fipe.sincronizacoes set status=?,erro=?,executar_em=now()+(? * interval '1 second'),atualizado_em=now()
            where id=? and lease=?
            """,transitoria&&t.tentativas()<5?"PENDENTE":"FALHOU",erro,Math.min(86400,60*(1<<t.tentativas())),t.id(),t.lease());
    }
    public Map<String,Object> estado(UUID id){
        var resultados=jdbc.queryForList("select id,status,tentativas,executar_em,erro,atualizado_em from fipe.sincronizacoes where id=?",id);
        if(resultados.isEmpty())throw new repasse.phcauto.backend.fipe.internal.core.exception.FipeException(
            repasse.phcauto.backend.fipe.internal.core.exception.FipeException.Motivo.NAO_ENCONTRADA,"Sincronização não encontrada");
        return resultados.get(0);
    }
    public record Tarefa(UUID id,UUID lease,String comando,int tentativas) {}
}
