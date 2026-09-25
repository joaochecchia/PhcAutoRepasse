package repasse.phcauto.backend.infra.gateway.identidade;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;
import repasse.phcauto.backend.domain.event.identidade.UsuarioAlterado;
import repasse.phcauto.backend.domain.gateway.identidade.PublicarEventoUsuarioGateway;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;

class UsuarioEventosTests {
    @Configuration(proxyBeanMethods = false)
    @EnableTransactionManagement
    @Import(SpringPublicarEventoUsuarioGateway.class)
    static class Config {
        @Bean PlatformTransactionManager writeTransactionManager() throws Exception {
            var dataSource = mock(DataSource.class);
            var connection = mock(Connection.class);
            when(connection.getAutoCommit()).thenReturn(true);
            when(dataSource.getConnection()).thenReturn(connection);
            return new DataSourceTransactionManager(dataSource);
        }
        @Bean Receptor receptor() { return new Receptor(); }
    }

    static class Receptor {
        final List<UsuarioAlterado> recebidos = new ArrayList<>();
        @TransactionalEventListener
        public void receber(UsuarioAlterado evento) { recebidos.add(evento); }
    }

    private UsuarioAlterado evento(TipoPessoa tipo, UsuarioAlterado.Operacao operacao) {
        return new UsuarioAlterado(UUID.randomUUID(), UUID.randomUUID(), tipo, operacao, Instant.now());
    }

    @Test void entregaSomenteDepoisDoCommitParaPfEPjEmTodasAsOperacoes() {
        try (var context = new AnnotationConfigApplicationContext(Config.class)) {
            var gateway = context.getBean(PublicarEventoUsuarioGateway.class);
            var receptor = context.getBean(Receptor.class);
            var tx = new TransactionTemplate(context.getBean(PlatformTransactionManager.class));
            for (var tipo : TipoPessoa.values()) {
                for (var operacao : UsuarioAlterado.Operacao.values()) {
                    var evento = evento(tipo, operacao);
                    int antes = receptor.recebidos.size();
                    tx.executeWithoutResult(status -> {
                        gateway.publicar(evento);
                        assertEquals(antes, receptor.recebidos.size());
                    });
                    assertEquals(evento, receptor.recebidos.get(antes));
                }
            }
        }
    }

    @Test void rollbackNaoEntregaEvento() {
        try (var context = new AnnotationConfigApplicationContext(Config.class)) {
            var tx = new TransactionTemplate(context.getBean(PlatformTransactionManager.class));
            tx.executeWithoutResult(status -> {
                context.getBean(PublicarEventoUsuarioGateway.class).publicar(
                        evento(TipoPessoa.PF, UsuarioAlterado.Operacao.CADASTRADO));
                status.setRollbackOnly();
            });
            assertTrue(context.getBean(Receptor.class).recebidos.isEmpty());
        }
    }

    @Test void rejeitaPublicacaoSemTransacao() {
        try (var context = new AnnotationConfigApplicationContext(Config.class)) {
            assertThrows(IllegalTransactionStateException.class, () ->
                    context.getBean(PublicarEventoUsuarioGateway.class).publicar(
                            evento(TipoPessoa.PJ, UsuarioAlterado.Operacao.CADASTRADO)));
        }
    }

    @Test void rejeitaTransacaoSomenteLeitura() {
        try (var context = new AnnotationConfigApplicationContext(Config.class)) {
            var tx = new TransactionTemplate(context.getBean(PlatformTransactionManager.class));
            tx.setReadOnly(true);
            assertThrows(IllegalStateException.class, () -> tx.executeWithoutResult(status ->
                    context.getBean(PublicarEventoUsuarioGateway.class).publicar(
                            evento(TipoPessoa.PF, UsuarioAlterado.Operacao.ATUALIZADO))));
            assertTrue(context.getBean(Receptor.class).recebidos.isEmpty());
        }
    }
}
