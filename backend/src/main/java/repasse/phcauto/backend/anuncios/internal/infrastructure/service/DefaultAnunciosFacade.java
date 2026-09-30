package repasse.phcauto.backend.anuncios.internal.infrastructure.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repasse.phcauto.backend.anuncios.*;
import repasse.phcauto.backend.anuncios.internal.core.domain.AnuncioCriadoResultado;
import repasse.phcauto.backend.anuncios.internal.core.domain.AtualizarAnuncioCommand;
import repasse.phcauto.backend.anuncios.internal.core.domain.BuscarAnunciosFiltro;
import repasse.phcauto.backend.anuncios.internal.core.domain.CriarAnuncioCommand;
import repasse.phcauto.backend.anuncios.internal.core.domain.PaginaAnuncios;
import repasse.phcauto.backend.anuncios.internal.core.exception.AnuncioInvalidoException;
import repasse.phcauto.backend.anuncios.internal.core.usecase.AtualizarAnuncioUseCase;
import repasse.phcauto.backend.anuncios.internal.core.usecase.BuscarAnunciosUseCase;
import repasse.phcauto.backend.anuncios.internal.core.usecase.CriarAnuncioUseCase;
import repasse.phcauto.backend.anuncios.internal.core.usecase.ExcluirAnuncioUseCase;
import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.request.AtualizarAnuncioRequest;
import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.request.BuscarAnunciosRequest;
import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.request.CriarAnuncioRequest;
import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.response.AnuncioResponse;
import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.response.AnuncioBuscaResponse;
import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.response.PaginaAnunciosResponse;
@Service
public class DefaultAnunciosFacade implements AnunciosFacade {
    private final BuscarAnunciosUseCase buscarAnuncios;
    private final CriarAnuncioUseCase criarAnuncio;
    private final AtualizarAnuncioUseCase atualizarAnuncio;
    private final ExcluirAnuncioUseCase excluirAnuncio;
    public DefaultAnunciosFacade(BuscarAnunciosUseCase buscarAnuncios, CriarAnuncioUseCase criarAnuncio,
            AtualizarAnuncioUseCase atualizarAnuncio, ExcluirAnuncioUseCase excluirAnuncio) {
        this.buscarAnuncios = buscarAnuncios; this.criarAnuncio = criarAnuncio; this.atualizarAnuncio = atualizarAnuncio; this.excluirAnuncio = excluirAnuncio;
    }

    @Override
    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public PaginaAnunciosResponse buscar(BuscarAnunciosRequest r) {
        if (r == null) r = new BuscarAnunciosRequest(null,null,null,null,null,null,null,null,null,null,
                null,null,null,null,null,null,null,null,null,null,null,null,null,null);
        var filtro = new BuscarAnunciosFiltro(r.tipoVeiculo(), r.cidade(), r.uf(), r.marca(), r.tipoPessoa(),
                r.perfil(), r.precoMinimoCentavos(), r.precoMaximoCentavos(), r.anoMinimo(), r.anoMaximo(),
                r.cambio(), r.combustivel(), r.motorizacao(), r.condicao(), r.tipoDirecao(), r.tracao(),
                r.ipvaPago(), r.blindado(), r.numeroPortas(), r.cilindradaLitros(), r.tipoFreio(),
                r.carroceria(), r.pagina() == null ? 0 : r.pagina(), r.tamanho() == null ? 20 : r.tamanho());
        var pagina = buscarAnuncios.execute(filtro);
        var itens = pagina.anuncios().stream().map(item -> new AnuncioBuscaResponse(
                item.anuncioId(), item.veiculoId(), item.anuncianteId(), item.tipoVeiculo(),
                item.fabricante(), item.modelo(), item.anoFabricacao(), item.anoModelo(), item.condicao(),
                item.titulo(), item.tipoPreco(), item.precoCentavos(), item.cidade(), item.uf(),
                item.tipoPessoa(), item.nomePerfil(), item.cambio(), item.combustivel(),
                item.motorizacao(), item.tipoDirecao(), item.tracao(), item.ipvaPago(),
                item.blindado(), item.numeroPortas(), item.cilindradaLitros(), item.cilindradas(),
                item.tipoFreio(), item.carroceria())).toList();
        return new PaginaAnunciosResponse(itens, pagina.total(), pagina.pagina(), pagina.tamanho());
    }

    @Override
    @Transactional(transactionManager = "writeTransactionManager")
    public AnuncioResponse criar(CriarAnuncioRequest request) {
        return response(criarAnuncio.execute(command(request)));
    }

    @Override
    @Transactional(transactionManager = "writeTransactionManager")
    public AnuncioResponse atualizar(java.util.UUID anuncioId, AtualizarAnuncioRequest request) {
        if (request == null) throw new AnuncioInvalidoException("Alterações são obrigatórias");
        int blocos=(request.carro()!=null?1:0)+(request.moto()!=null?1:0)+(request.caminhao()!=null?1:0)
                +(request.caminhonete()!=null?1:0)+(request.barco()!=null?1:0)+(request.linhaAmarela()!=null?1:0);
        if (blocos > 1) throw new AnuncioInvalidoException("Informe no máximo um bloco de dados específicos");
        var e=request.endereco();
        var endereco=e==null?null:new CriarAnuncioCommand.Endereco(e.cep(),e.cidade(),e.bairro(),e.rua(),e.numero(),e.complemento(),e.uf());
        var command=new AtualizarAnuncioCommand(request.fabricante(),request.modelo(),request.versao(),request.anoFabricacao(),
                request.anoModelo(),request.cor(),request.identificadorPublico(),request.condicao(),request.tipoFreio(),request.titulo(),request.descricao(),
                request.tipoPreco(),request.precoCentavos(),request.aceitaTroca(),request.publicarAgora(),endereco,detalhes(request));
        return response(atualizarAnuncio.execute(anuncioId, command));
    }

    @Override
    @Transactional(transactionManager = "writeTransactionManager")
    public void excluir(java.util.UUID anuncioId) { excluirAnuncio.execute(anuncioId); }

    private AnuncioResponse response(AnuncioCriadoResultado resultado) {
        var c=resultado.dados();
        return new AnuncioResponse(resultado.anuncioId(), resultado.veiculoId(), c.anuncianteId(),
                c.tipoVeiculo(), c.fabricante(), c.modelo(), c.versao(), c.anoFabricacao(), c.anoModelo(),
                c.cor(), c.condicao(), c.tipoFreio(), c.titulo(), c.descricao(), c.tipoPreco(), c.precoCentavos(), c.aceitaTroca(),
                c.endereco().cidade(), resultado.status(), response(c.detalhes()), resultado.criadoEm(), resultado.publicadoEm());
    }

    private CriarAnuncioCommand.DetalhesVeiculo detalhes(AtualizarAnuncioRequest r) {
        if(r.carro()!=null){var d=r.carro();return new CriarAnuncioCommand.Carro(d.quilometragem(),d.carroceria(),d.cambio(),d.combustivel(),d.tracao(),d.motorizacao(),d.tipoDirecao(),d.cilindradaLitros(),d.numeroPortas(),d.numeroLugares(),normalizarPlaca(d.placa()),d.exibirPlacaCompleta(),d.unicoDono(),d.ipvaPago(),d.licenciado(),d.blindado());}
        if(r.moto()!=null){var d=r.moto();return new CriarAnuncioCommand.Moto(d.quilometragem(),d.cilindradas(),d.categoria(),d.partida(),d.refrigeracao(),d.cambio(),d.combustivel(),normalizarPlaca(d.placa()),d.exibirPlacaCompleta(),d.ipvaPago(),d.licenciado());}
        if(r.caminhao()!=null){var d=r.caminhao();return new CriarAnuncioCommand.Caminhao(d.quilometragem(),d.configuracao(),d.carroceria(),d.cambio(),d.combustivel(),d.tracao(),d.tipoDirecao(),d.numeroEixos(),d.capacidadeCargaKg(),d.pesoBrutoTotalKg(),d.implemento(),normalizarPlaca(d.placa()),d.exibirPlacaCompleta(),d.ipvaPago(),d.licenciado());}
        if(r.caminhonete()!=null){var d=r.caminhonete();return new CriarAnuncioCommand.Caminhonete(d.quilometragem(),d.tipoCabine(),d.carroceria(),d.cambio(),d.combustivel(),d.tracao(),d.motorizacao(),d.tipoDirecao(),d.cilindradaLitros(),d.capacidadeCargaKg(),d.numeroPortas(),normalizarPlaca(d.placa()),d.exibirPlacaCompleta(),d.unicoDono(),d.ipvaPago(),d.licenciado(),d.blindado());}
        if(r.barco()!=null){var d=r.barco();return new CriarAnuncioCommand.Barco(d.tamanhoPes(),d.estilo(),d.materialCasco(),d.capacidadePessoas(),d.numeroCabines(),d.horasUso(),d.registroMaritimo(),d.motores().stream().map(m->new CriarAnuncioCommand.Motor(m.posicao(),m.fabricante(),m.modelo(),m.potenciaHp(),m.ano(),m.horasUso(),m.horasDesdeRevisao(),m.combustivel())).toList());}
        if(r.linhaAmarela()!=null){var d=r.linhaAmarela();return new CriarAnuncioCommand.LinhaAmarela(d.tipoMaquina(),d.horimetro(),d.pesoOperacionalKg(),d.potenciaHp(),d.tipoEsteiraOuPneu(),d.capacidadeCacambaM3(),d.numeroSerie());}
        return null;
    }

    private CriarAnuncioCommand command(CriarAnuncioRequest r) {
        if (r == null) throw new AnuncioInvalidoException("Dados do anúncio são obrigatórios");
        int blocos = (r.carro()!=null?1:0)+(r.moto()!=null?1:0)+(r.caminhao()!=null?1:0)
                +(r.caminhonete()!=null?1:0)+(r.barco()!=null?1:0)+(r.linhaAmarela()!=null?1:0);
        if (blocos != 1) throw new AnuncioInvalidoException("Informe exatamente um bloco de dados específicos");
        var e = r.endereco();
        var endereco = e == null ? null : new CriarAnuncioCommand.Endereco(e.cep(), e.cidade(), e.bairro(),
                e.rua(), e.numero(), e.complemento(), e.uf());
        return new CriarAnuncioCommand(r.anuncianteId(), r.tipoVeiculo(), r.fabricante(), r.modelo(),
                r.versao(), r.anoFabricacao(), r.anoModelo(), r.cor(), r.identificadorPublico(),
                r.condicao(), r.tipoFreio(), r.titulo(), r.descricao(), r.tipoPreco(), r.precoCentavos(), r.aceitaTroca(),
                Boolean.TRUE.equals(r.publicarAgora()), endereco, detalhes(r));
    }

    private CriarAnuncioCommand.DetalhesVeiculo detalhes(CriarAnuncioRequest r) {
        if (r.carro()!=null) { var d=r.carro(); return new CriarAnuncioCommand.Carro(d.quilometragem(),d.carroceria(),d.cambio(),d.combustivel(),d.tracao(),d.motorizacao(),d.tipoDirecao(),d.cilindradaLitros(),d.numeroPortas(),d.numeroLugares(),normalizarPlaca(d.placa()),d.exibirPlacaCompleta(),d.unicoDono(),d.ipvaPago(),d.licenciado(),d.blindado()); }
        if (r.moto()!=null) { var d=r.moto(); return new CriarAnuncioCommand.Moto(d.quilometragem(),d.cilindradas(),d.categoria(),d.partida(),d.refrigeracao(),d.cambio(),d.combustivel(),normalizarPlaca(d.placa()),d.exibirPlacaCompleta(),d.ipvaPago(),d.licenciado()); }
        if (r.caminhao()!=null) { var d=r.caminhao(); return new CriarAnuncioCommand.Caminhao(d.quilometragem(),d.configuracao(),d.carroceria(),d.cambio(),d.combustivel(),d.tracao(),d.tipoDirecao(),d.numeroEixos(),d.capacidadeCargaKg(),d.pesoBrutoTotalKg(),d.implemento(),normalizarPlaca(d.placa()),d.exibirPlacaCompleta(),d.ipvaPago(),d.licenciado()); }
        if (r.caminhonete()!=null) { var d=r.caminhonete(); return new CriarAnuncioCommand.Caminhonete(d.quilometragem(),d.tipoCabine(),d.carroceria(),d.cambio(),d.combustivel(),d.tracao(),d.motorizacao(),d.tipoDirecao(),d.cilindradaLitros(),d.capacidadeCargaKg(),d.numeroPortas(),normalizarPlaca(d.placa()),d.exibirPlacaCompleta(),d.unicoDono(),d.ipvaPago(),d.licenciado(),d.blindado()); }
        if (r.barco()!=null) { var d=r.barco(); return new CriarAnuncioCommand.Barco(d.tamanhoPes(),d.estilo(),d.materialCasco(),d.capacidadePessoas(),d.numeroCabines(),d.horasUso(),d.registroMaritimo(),d.motores().stream().map(m->new CriarAnuncioCommand.Motor(m.posicao(),m.fabricante(),m.modelo(),m.potenciaHp(),m.ano(),m.horasUso(),m.horasDesdeRevisao(),m.combustivel())).toList()); }
        if (r.linhaAmarela()!=null) { var d=r.linhaAmarela(); return new CriarAnuncioCommand.LinhaAmarela(d.tipoMaquina(),d.horimetro(),d.pesoOperacionalKg(),d.potenciaHp(),d.tipoEsteiraOuPneu(),d.capacidadeCacambaM3(),d.numeroSerie()); }
        throw new AnuncioInvalidoException("Dados específicos são obrigatórios");
    }

    private AnuncioResponse.DetalhesVeiculoResponse response(CriarAnuncioCommand.DetalhesVeiculo detalhes) {
        if (detalhes instanceof CriarAnuncioCommand.Carro d) return new AnuncioResponse.CarroResponse(d.quilometragem(),d.carroceria(),d.cambio(),d.combustivel(),d.tracao(),d.motorizacao(),d.tipoDirecao(),d.cilindradaLitros(),d.numeroPortas(),d.numeroLugares(),placaPublica(d.placa(),d.exibirPlacaCompleta()),d.exibirPlacaCompleta(),d.unicoDono(),d.ipvaPago(),d.licenciado(),d.blindado());
        if (detalhes instanceof CriarAnuncioCommand.Moto d) return new AnuncioResponse.MotoResponse(d.quilometragem(),d.cilindradas(),d.categoria(),d.partida(),d.refrigeracao(),d.cambio(),d.combustivel(),placaPublica(d.placa(),d.exibirPlacaCompleta()),d.exibirPlacaCompleta(),d.ipvaPago(),d.licenciado());
        if (detalhes instanceof CriarAnuncioCommand.Caminhao d) return new AnuncioResponse.CaminhaoResponse(d.quilometragem(),d.configuracao(),d.carroceria(),d.cambio(),d.combustivel(),d.tracao(),d.tipoDirecao(),d.numeroEixos(),d.capacidadeCargaKg(),d.pesoBrutoTotalKg(),d.implemento(),placaPublica(d.placa(),d.exibirPlacaCompleta()),d.exibirPlacaCompleta(),d.ipvaPago(),d.licenciado());
        if (detalhes instanceof CriarAnuncioCommand.Caminhonete d) return new AnuncioResponse.CaminhoneteResponse(d.quilometragem(),d.tipoCabine(),d.carroceria(),d.cambio(),d.combustivel(),d.tracao(),d.motorizacao(),d.tipoDirecao(),d.cilindradaLitros(),d.capacidadeCargaKg(),d.numeroPortas(),placaPublica(d.placa(),d.exibirPlacaCompleta()),d.exibirPlacaCompleta(),d.unicoDono(),d.ipvaPago(),d.licenciado(),d.blindado());
        if (detalhes instanceof CriarAnuncioCommand.Barco d) return new AnuncioResponse.BarcoResponse(d.tamanhoPes(),d.estilo(),d.materialCasco(),d.capacidadePessoas(),d.numeroCabines(),d.horasUso(),d.registroMaritimo(),d.motores().stream().map(m->new AnuncioResponse.MotorBarcoResponse(m.posicao(),m.fabricante(),m.modelo(),m.potenciaHp(),m.ano(),m.horasUso(),m.horasDesdeRevisao(),m.combustivel())).toList());
        var d=(CriarAnuncioCommand.LinhaAmarela)detalhes;
        return new AnuncioResponse.LinhaAmarelaResponse(d.tipoMaquina(),d.horimetro(),d.pesoOperacionalKg(),d.potenciaHp(),d.tipoEsteiraOuPneu(),d.capacidadeCacambaM3(),d.numeroSerie());
    }
    private static String normalizarPlaca(String placa) { return placa == null ? null : placa.trim().toUpperCase(java.util.Locale.ROOT); }
    private static String placaPublica(String placa, Boolean completa) {
        if (placa == null) return null;
        return Boolean.TRUE.equals(completa) ? placa : placa.substring(placa.length() - 1);
    }
}
