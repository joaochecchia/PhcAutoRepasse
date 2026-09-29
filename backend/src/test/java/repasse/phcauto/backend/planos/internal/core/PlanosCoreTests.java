package repasse.phcauto.backend.planos.internal.core;

import static org.assertj.core.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;
import org.junit.jupiter.api.Test;

class PlanosCoreTests {
    private final Instant agora = Instant.parse("2026-09-29T12:00:00Z");

    @Test void criaPlanoNormalizado() {
        var gateway = new FakeGateway();
        var useCase = new CriarPlano(gateway, Clock.fixed(agora, ZoneOffset.UTC));
        var criado = useCase.execute(new NovoPlano("  Premium  ", 9990L, 1, 20, 1, true));
        assertThat(criado.nome()).isEqualTo("Premium");
        assertThat(criado.criadoEm()).isEqualTo(agora);
        assertThat(gateway.dados).containsKey(criado.id());
    }

    @Test void rejeitaNomeDuplicadoEValoresInvalidos() {
        var gateway = new FakeGateway();
        var useCase = new CriarPlano(gateway, Clock.systemUTC());
        useCase.execute(new NovoPlano("Premium", 100L, 1, 1, 1, true));
        assertThatThrownBy(() -> useCase.execute(new NovoPlano("Premium", 200L, 2, 2, 2, true)))
                .isInstanceOf(PlanoInvalidoException.class);
        assertThatThrownBy(() -> useCase.execute(new NovoPlano("Inválido", -1L, 0, -1, -1, false)))
                .isInstanceOf(PlanoInvalidoException.class);
    }

    @Test void patchAlteraSomenteCamposEnviados() {
        var gateway = new FakeGateway();
        var atual = gateway.inserir("Básico", 5000L, 3, 5, false, agora);
        var alterado = new AtualizarPlano(gateway).execute(atual.id(),
                new PlanoPatch(null, 6500L, null, null, 2, true));
        assertThat(alterado.nome()).isEqualTo("Básico");
        assertThat(alterado.valorCentavos()).isEqualTo(6500L);
        assertThat(alterado.periodoMeses()).isEqualTo(3);
        assertThat(alterado.limiteAnuncios()).isEqualTo(5);
        assertThat(alterado.limiteVistoriasCautelares()).isEqualTo(2);
        assertThat(alterado.ativo()).isTrue();
        assertThat(alterado.criadoEm()).isEqualTo(agora);
    }

    @Test void rejeitaPatchVazio() {
        var gateway = new FakeGateway();
        var atual = gateway.inserir("Básico", null, null, null, false, agora);
        assertThatThrownBy(() -> new AtualizarPlano(gateway).execute(atual.id(),
                new PlanoPatch(null, null, null, null, null, null)))
                .isInstanceOf(PlanoInvalidoException.class);
    }

    @Test void listaComPaginacaoEExclui() {
        var gateway = new FakeGateway();
        gateway.inserir("A", 1L, 1, 1, true, agora);
        gateway.inserir("B", 2L, 1, 2, true, agora);
        var pagina = new ListarPlanos(gateway).execute(1, 1);
        assertThat(pagina.planos()).hasSize(1);
        assertThat(pagina.total()).isEqualTo(2);
        var id = pagina.planos().get(0).id();
        new ExcluirPlano(gateway).execute(id);
        assertThat(gateway.dados).doesNotContainKey(id);
    }

    @Test void buscarEExcluirInexistenteRetornamErroDeNegocio() {
        var gateway = new FakeGateway();
        var id = UUID.randomUUID();
        assertThatThrownBy(() -> new BuscarPlano(gateway).execute(id)).isInstanceOf(PlanoNaoEncontradoException.class);
        assertThatThrownBy(() -> new ExcluirPlano(gateway).execute(id)).isInstanceOf(PlanoNaoEncontradoException.class);
    }

    private static final class FakeGateway implements PlanoGateway {
        private final LinkedHashMap<UUID, PlanoDados> dados = new LinkedHashMap<>();
        PlanoDados inserir(String nome, Long valor, Integer periodo, Integer limite, boolean ativo, Instant criadoEm) {
            var plano = new PlanoDados(UUID.randomUUID(), nome, valor, periodo, limite, null, ativo, criadoEm);
            dados.put(plano.id(), plano); return plano;
        }
        @Override public PlanoDados criar(UUID id, NovoPlano p, Instant criadoEm) {
            var plano = new PlanoDados(id, p.nome(), p.valorCentavos(), p.periodoMeses(), p.limiteAnuncios(),
                    p.limiteVistoriasCautelares(), p.ativo(), criadoEm);
            dados.put(id, plano); return plano;
        }
        @Override public Optional<PlanoDados> buscar(UUID id) { return Optional.ofNullable(dados.get(id)); }
        @Override public List<PlanoDados> listar(int offset, int limite) { return dados.values().stream().skip(offset).limit(limite).toList(); }
        @Override public long contar() { return dados.size(); }
        @Override public PlanoDados atualizar(UUID id, PlanoDados plano) { dados.put(id, plano); return plano; }
        @Override public boolean excluir(UUID id) { return dados.remove(id) != null; }
        @Override public boolean existeNome(String nome, UUID ignorarId) {
            return dados.values().stream().anyMatch(p -> p.nome().equals(nome) && !p.id().equals(ignorarId));
        }
    }
}
