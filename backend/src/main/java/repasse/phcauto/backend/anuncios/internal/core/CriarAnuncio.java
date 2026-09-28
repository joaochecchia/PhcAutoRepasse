package repasse.phcauto.backend.anuncios.internal.core;

import java.time.Clock;
import java.time.Year;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;
import repasse.phcauto.backend.anuncios.AnuncioCriado;
import repasse.phcauto.backend.domain.model.catalogo.StatusAnuncio;
import repasse.phcauto.backend.domain.model.catalogo.TipoPreco;
import repasse.phcauto.backend.domain.model.catalogo.TipoVeiculo;

public final class CriarAnuncio implements CriarAnuncioUseCase {
    private final CriarAnuncioGateway gateway;
    private final PublicarAnuncioCriadoGateway eventos;
    private final Clock clock;

    public CriarAnuncio(CriarAnuncioGateway gateway, PublicarAnuncioCriadoGateway eventos, Clock clock) {
        this.gateway = Objects.requireNonNull(gateway);
        this.eventos = Objects.requireNonNull(eventos);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public AnuncioCriadoResultado execute(CriarAnuncioCommand c) {
        validar(c);
        if (!gateway.usuarioAtivo(c.anuncianteId())) throw new AnuncianteInvalidoException();
        var agora = clock.instant();
        var anuncioId = UUID.randomUUID();
        var veiculoId = UUID.randomUUID();
        var enderecoId = UUID.randomUUID();
        var status = c.publicarAgora() ? StatusAnuncio.PUBLICADO : StatusAnuncio.RASCUNHO;
        gateway.salvar(anuncioId, veiculoId, enderecoId, c, status, agora);
        eventos.publicar(new AnuncioCriado(UUID.randomUUID(), anuncioId, veiculoId,
                c.anuncianteId(), c.tipoVeiculo(), status, c.endereco().cidade(), agora));
        return new AnuncioCriadoResultado(anuncioId, veiculoId, c, status, agora,
                status == StatusAnuncio.PUBLICADO ? agora : null);
    }

    private void validar(CriarAnuncioCommand c) {
        if (c == null) throw new AnuncioInvalidoException("Dados do anúncio são obrigatórios");
        Objects.requireNonNull(c.anuncianteId(), "anuncianteId");
        Objects.requireNonNull(c.tipoVeiculo(), "tipoVeiculo");
        obrigatorio(c.fabricante(), "fabricante");
        obrigatorio(c.modelo(), "modelo");
        obrigatorio(c.titulo(), "titulo");
        if (c.endereco() == null) throw new AnuncioInvalidoException("Endereço é obrigatório");
        obrigatorio(c.endereco().cep(), "CEP");
        if (!c.endereco().cep().matches("[0-9]{8}")) throw new AnuncioInvalidoException("CEP deve conter 8 dígitos");
        obrigatorio(c.endereco().cidade(), "cidade");
        obrigatorio(c.endereco().bairro(), "bairro");
        obrigatorio(c.endereco().rua(), "rua");
        obrigatorio(c.endereco().uf(), "UF");
        if (!c.endereco().uf().matches("(?i)[a-z]{2}")) throw new AnuncioInvalidoException("UF inválida");
        if (c.tipoPreco() == null) throw new AnuncioInvalidoException("Tipo de preço é obrigatório");
        if (c.tipoPreco() == TipoPreco.FIXO && (c.precoCentavos() == null || c.precoCentavos() <= 0))
            throw new AnuncioInvalidoException("Preço positivo é obrigatório para preço fixo");
        if (c.tipoPreco() == TipoPreco.SOB_CONSULTA && c.precoCentavos() != null)
            throw new AnuncioInvalidoException("Preço deve ser omitido quando estiver sob consulta");
        ValidarDadosTecnicosAnuncio.validar(c);
        int limiteAno = Year.now(clock).getValue() + 1;
        validarAno(c.anoFabricacao(), limiteAno, "anoFabricacao");
        validarAno(c.anoModelo(), limiteAno, "anoModelo");
        validarDetalhes(c.tipoVeiculo(), c.condicao(), c.detalhes());
    }

    private void validarDetalhes(TipoVeiculo tipo, repasse.phcauto.backend.domain.model.catalogo.CondicaoVeiculo condicao, CriarAnuncioCommand.DetalhesVeiculo detalhes) {
        boolean compativel = switch (tipo) {
            case CARRO -> detalhes instanceof CriarAnuncioCommand.Carro;
            case MOTO -> detalhes instanceof CriarAnuncioCommand.Moto;
            case CAMINHAO -> detalhes instanceof CriarAnuncioCommand.Caminhao;
            case CAMINHONETE -> detalhes instanceof CriarAnuncioCommand.Caminhonete;
            case BARCO -> detalhes instanceof CriarAnuncioCommand.Barco;
            case LINHA_AMARELA -> detalhes instanceof CriarAnuncioCommand.LinhaAmarela;
        };
        if (!compativel) throw new AnuncioInvalidoException("Dados específicos incompatíveis com o tipo do veículo");
        if (detalhes instanceof CriarAnuncioCommand.Barco barco) {
            var posicoes = new HashSet<Integer>();
            for (var motor : barco.motores()) {
                if (motor.posicao() == null || motor.posicao() <= 0 || !posicoes.add(motor.posicao()))
                    throw new AnuncioInvalidoException("Posições dos motores devem ser positivas e únicas");
            }
        }
    }

    private void validarAno(Integer ano, int limite, String campo) {
        if (ano != null && (ano < 1886 || ano > limite))
            throw new AnuncioInvalidoException(campo + " inválido");
    }

    private void obrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) throw new AnuncioInvalidoException(campo + " é obrigatório");
    }

    private void obrigatorio(Object valor, String campo) {
        if (valor == null) throw new AnuncioInvalidoException(campo + " é obrigatório");
    }

}
