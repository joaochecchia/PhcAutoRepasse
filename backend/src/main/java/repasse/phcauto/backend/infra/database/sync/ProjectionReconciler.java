package repasse.phcauto.backend.infra.database.sync;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Enfileira reconciliação paginada de dados existentes e de linhas removidas da origem. */
@Component
public class ProjectionReconciler implements ApplicationRunner {
    private final JdbcTemplate source;
    private final JdbcTemplate target;
    private final ApplicationEventPublisher events;
    private final TransactionTemplate transaction;
    private final boolean bootstrap;

    public ProjectionReconciler(@Qualifier("writeDataSource") DataSource source,
            @Qualifier("readDataSource") DataSource target, ApplicationEventPublisher events,
            @Qualifier("writeTransactionManager") PlatformTransactionManager manager,
            @Value("${app.projection.bootstrap:true}") boolean bootstrap) {
        this.source = new JdbcTemplate(source);
        this.target = new JdbcTemplate(target);
        this.events = events;
        this.transaction = new TransactionTemplate(manager);
        this.bootstrap = bootstrap;
    }
    @Override
    public void run(ApplicationArguments args) { if (bootstrap) reconcile(); }

    public void reconcile() {
        for (var table : ProjectionTable.values()) {
            enqueue(table, source, false);
            enqueue(table, target, true);
        }
    }

    private void enqueue(ProjectionTable table, JdbcTemplate database, boolean onlyMissingAtSource) {
        List<String> last = null;
        String keys = table.keys.stream().map(k -> k + "::text").collect(Collectors.joining(","));
        while (true) {
            String where = last == null ? "" : " where row(" + keys + ") > row("
                    + table.keys.stream().map(k -> "cast(? as text)").collect(Collectors.joining(",")) + ")";
            var page = database.query("select " + keys + " from " + table.sqlName + where
                    + " order by " + keys + " limit 250", (rs, n) -> {
                var key = new ArrayList<String>();
                for (int i = 1; i <= table.keys.size(); i++) key.add(rs.getString(i));
                return List.copyOf(key);
            }, last == null ? new Object[0] : last.toArray());
            if (page.isEmpty()) return;
            var eventsToPublish = onlyMissingAtSource ? missingAtSource(table, page) : page;
            transaction.executeWithoutResult(status ->
                    eventsToPublish.forEach(key -> events.publishEvent(RowChanged.of(table, key))));
            last = page.get(page.size() - 1);
        }
    }

    /** No segundo passe, publica somente órfãos da projeção; linhas presentes nas duas bases já foram enfileiradas. */
    private List<List<String>> missingAtSource(ProjectionTable table, List<List<String>> candidates) {
        String conditions = candidates.stream().map(ignored -> "(" + table.predicate() + ")")
                .collect(Collectors.joining(" or "));
        String keys = table.keys.stream().map(k -> k + "::text").collect(Collectors.joining(","));
        Object[] parameters = candidates.stream().flatMap(List::stream).toArray();
        Set<List<String>> existing = new HashSet<>(source.query(
                "select " + keys + " from " + table.sqlName + " where " + conditions,
                (rs, n) -> {
                    var key = new ArrayList<String>();
                    for (int i = 1; i <= table.keys.size(); i++) key.add(rs.getString(i));
                    return List.copyOf(key);
                }, parameters));
        return candidates.stream().filter(key -> !existing.contains(key)).toList();
    }
}
