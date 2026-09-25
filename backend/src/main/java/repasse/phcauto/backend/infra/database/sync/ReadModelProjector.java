package repasse.phcauto.backend.infra.database.sync;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class ReadModelProjector {
    private final JdbcTemplate source;
    private final JdbcTemplate target;
    private final TransactionTemplate projectionTransaction;

    public ReadModelProjector(@Qualifier("writeDataSource") DataSource source,
            @Qualifier("projectionDataSource") DataSource target,
            @Qualifier("projectionTransactionManager") PlatformTransactionManager transactionManager) {
        this.source = new JdbcTemplate(source);
        this.target = new JdbcTemplate(target);
        this.projectionTransaction = new TransactionTemplate(transactionManager);
        this.projectionTransaction.setTimeout(10);
    }

    public void synchronize(RowChanged event) {
        var table = event.table();
        projectionTransaction.executeWithoutResult(status -> {
            target.execute("set local lock_timeout = '5s'");
            target.execute("set local statement_timeout = '10s'");
            // O lock também funciona entre instâncias e é liberado junto com o commit/rollback.
            target.query("select pg_advisory_xact_lock(hashtextextended(?, 0))",
                    rs -> {}, table.sqlName + ":" + String.join(":", event.key()));
            // Consultar o estado atual depois do lock impede regressão por evento atrasado.
            var rows = source.queryForList("select row_to_json(t)::text from " + table.sqlName
                    + " t where " + table.predicate(), String.class, event.key().toArray());
            if (rows.isEmpty()) {
                target.update("delete from " + table.sqlName + " where " + table.predicate(), event.key().toArray());
            } else {
                target.update(table.upsert(), rows.get(0));
            }
        });
    }
}
