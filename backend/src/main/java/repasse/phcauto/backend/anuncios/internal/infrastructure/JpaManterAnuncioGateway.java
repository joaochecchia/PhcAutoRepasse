package repasse.phcauto.backend.anuncios.internal.infrastructure;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import repasse.phcauto.backend.anuncios.internal.core.*;
import repasse.phcauto.backend.domain.model.catalogo.*;
import repasse.phcauto.backend.anuncios.internal.infrastructure.entity.*;
import repasse.phcauto.backend.anuncios.internal.infrastructure.repository.write.*;

@Repository
@Transactional(transactionManager = "writeTransactionManager", propagation = Propagation.MANDATORY)
class JpaManterAnuncioGateway implements AtualizarAnuncioGateway, ExcluirAnuncioGateway {
    private final AnuncioWriteRepository anuncios; private final VeiculoWriteRepository veiculos;
    private final EnderecoAnuncioWriteRepository enderecos; private final CarroWriteRepository carros;
    private final MotoWriteRepository motos; private final CaminhaoWriteRepository caminhoes;
    private final CaminhoneteWriteRepository caminhonetes; private final BarcoWriteRepository barcos;
    private final MotorBarcoWriteRepository motores; private final LinhaAmarelaWriteRepository linhas;
    private final FotoWriteRepository fotos; private final VeiculoCaracteristicaWriteRepository caracteristicas;
    @PersistenceContext(unitName="write") private EntityManager entityManager;

    JpaManterAnuncioGateway(AnuncioWriteRepository anuncios, VeiculoWriteRepository veiculos,
            EnderecoAnuncioWriteRepository enderecos, CarroWriteRepository carros, MotoWriteRepository motos,
            CaminhaoWriteRepository caminhoes, CaminhoneteWriteRepository caminhonetes,
            BarcoWriteRepository barcos, MotorBarcoWriteRepository motores,
            LinhaAmarelaWriteRepository linhas, FotoWriteRepository fotos,
            VeiculoCaracteristicaWriteRepository caracteristicas) {
        this.anuncios=anuncios; this.veiculos=veiculos; this.enderecos=enderecos; this.carros=carros;
        this.motos=motos; this.caminhoes=caminhoes; this.caminhonetes=caminhonetes;
        this.barcos=barcos; this.motores=motores; this.linhas=linhas; this.fotos=fotos;
        this.caracteristicas=caracteristicas;
    }

    @Override public AnuncioCriadoResultado atualizar(UUID id, AtualizarAnuncioCommand p, Instant agora) {
        var anuncio = anuncios.findById(id).orElseThrow(() -> new AnuncioNaoEncontradoException(id));
        var veiculo = veiculos.findById(anuncio.getVeiculoId()).orElseThrow(() -> new AnuncioNaoEncontradoException(id));
        var endereco = enderecos.findById(anuncio.getEnderecoId()).orElseThrow(() -> new AnuncioNaoEncontradoException(id));
        var atual = command(anuncio, veiculo, endereco);
        if (p.detalhes()!=null && !compativel(veiculo.getTipo(), p.detalhes()))
            throw new AnuncioInvalidoException("Dados específicos incompatíveis com o tipo do veículo");
        var enderecoNovo = merge(atual.endereco(), p.endereco());
        var detalhesNovos = mergeDetalhes(veiculo.getTipo(), atual.detalhes(), p.detalhes());
        var tipoPreco = nn(p.tipoPreco(), atual.tipoPreco());
        if (tipoPreco == TipoPreco.SOB_CONSULTA && p.precoCentavos() != null)
            throw new AnuncioInvalidoException("Preço deve ser omitido quando estiver sob consulta");
        Long preco = tipoPreco == TipoPreco.SOB_CONSULTA ? null : nn(p.precoCentavos(), atual.precoCentavos());
        if (tipoPreco == TipoPreco.FIXO && (preco == null || preco <= 0))
            throw new AnuncioInvalidoException("Preço positivo é obrigatório para preço fixo");
        var status = p.publicarAgora()==null ? anuncio.getStatus()
                : (p.publicarAgora() ? StatusAnuncio.PUBLICADO : StatusAnuncio.RASCUNHO);
        boolean publicar = status == StatusAnuncio.PUBLICADO;
        var c = new CriarAnuncioCommand(atual.anuncianteId(), atual.tipoVeiculo(), nn(p.fabricante(),atual.fabricante()),
                nn(p.modelo(),atual.modelo()), nn(p.versao(),atual.versao()), nn(p.anoFabricacao(),atual.anoFabricacao()),
                nn(p.anoModelo(),atual.anoModelo()), nn(p.cor(),atual.cor()), nn(p.identificadorPublico(),atual.identificadorPublico()),
                nn(p.condicao(),atual.condicao()), nn(p.tipoFreio(),atual.tipoFreio()), nn(p.titulo(),atual.titulo()), nn(p.descricao(),atual.descricao()), tipoPreco, preco,
                nn(p.aceitaTroca(),atual.aceitaTroca()), publicar, enderecoNovo, detalhesNovos);
        repasse.phcauto.backend.anuncios.internal.core.ValidarDadosTecnicosAnuncio.validar(c);
        if (c.fabricante()==null || c.fabricante().isBlank() || c.modelo()==null || c.modelo().isBlank()
                || c.titulo()==null || c.titulo().isBlank()) throw new AnuncioInvalidoException("Campos obrigatórios não podem ficar vazios");

        veiculo.atualizar(new JpaCriarAnuncioGateway.VeiculoDados(veiculo.getId(), c, agora));
        endereco.atualizar(new JpaCriarAnuncioGateway.EnderecoDados(endereco.getId(), enderecoNovo));
        atualizarDetalhes(veiculo.getTipo(), veiculo.getId(), detalhesNovos, p.detalhes()!=null);
        Instant publicadoEm = status==StatusAnuncio.PUBLICADO ? (anuncio.getPublicadoEm()==null?agora:anuncio.getPublicadoEm()) : null;
        anuncio.atualizar(new AnuncioAtualizadoDados(anuncio, c, status, publicadoEm, agora));
        return new AnuncioCriadoResultado(id, veiculo.getId(), c, status, anuncio.getCriadoEm(), publicadoEm);
    }

    @Override public UUID excluir(UUID id) {
        var anuncio=anuncios.findById(id).orElseThrow(() -> new AnuncioNaoEncontradoException(id));
        Number compras=(Number)entityManager.createNativeQuery("select count(*) from vendas.compras where anuncio_id=:id")
                .setParameter("id", id).getSingleResult();
        if (compras.longValue()>0) throw new ExclusaoAnuncioBloqueadaException();
        UUID veiculoId=anuncio.getVeiculoId(), enderecoId=anuncio.getEnderecoId();
        fotos.findByAnuncioId(id).forEach(fotos::delete);
        anuncios.delete(anuncio);
        caracteristicas.findByIdVeiculoId(veiculoId).forEach(caracteristicas::delete);
        var tipo=veiculos.findById(veiculoId).orElseThrow(() -> new AnuncioNaoEncontradoException(id)).getTipo();
        switch(tipo) {
            case CARRO -> carros.findById(veiculoId).ifPresent(carros::delete);
            case MOTO -> motos.findById(veiculoId).ifPresent(motos::delete);
            case CAMINHAO -> caminhoes.findById(veiculoId).ifPresent(caminhoes::delete);
            case CAMINHONETE -> caminhonetes.findById(veiculoId).ifPresent(caminhonetes::delete);
            case LINHA_AMARELA -> linhas.findById(veiculoId).ifPresent(linhas::delete);
            case BARCO -> { motores.findByBarcoId(veiculoId).forEach(motores::delete); barcos.findById(veiculoId).ifPresent(barcos::delete); }
        }
        veiculos.findById(veiculoId).ifPresent(veiculos::delete);
        enderecos.findById(enderecoId).ifPresent(enderecos::delete);
        return veiculoId;
    }

    private CriarAnuncioCommand command(AnuncioEntity a, VeiculoEntity v, EnderecoAnuncioEntity e) {
        return new CriarAnuncioCommand(a.getAnuncianteId(),v.getTipo(),v.getFabricante(),v.getModelo(),v.getVersao(),
                v.getAnoFabricacao(),v.getAnoModelo(),v.getCor(),v.getIdentificadorPublico(),v.getCondicao(),v.getTipoFreio(),a.getTitulo(),a.getDescricao(),
                a.getTipoPreco(),a.getPrecoCentavos(),a.getAceitaTroca(),a.getStatus()==StatusAnuncio.PUBLICADO,
                new CriarAnuncioCommand.Endereco(e.getCep(),e.getCidade(),e.getBairro(),e.getRua(),e.getNumero(),e.getComplemento(),e.getUf()), detalhes(v));
    }
    private CriarAnuncioCommand.DetalhesVeiculo detalhes(VeiculoEntity v) { UUID id=v.getId(); return switch(v.getTipo()) {
        case CARRO -> { var d=carros.findById(id).orElseThrow(); yield new CriarAnuncioCommand.Carro(d.getQuilometragem(),d.getCarroceria(),d.getCambio(),d.getCombustivel(),d.getTracao(),d.getMotorizacao(),d.getTipoDirecao(),d.getCilindradaLitros(),d.getNumeroPortas(),d.getNumeroLugares(),d.getPlaca(),d.getExibirPlacaCompleta(),d.getUnicoDono(),d.getIpvaPago(),d.getLicenciado(),d.getBlindado()); }
        case MOTO -> { var d=motos.findById(id).orElseThrow(); yield new CriarAnuncioCommand.Moto(d.getQuilometragem(),d.getCilindradas(),d.getCategoria(),d.getPartida(),d.getRefrigeracao(),d.getCambio(),d.getCombustivel(),d.getPlaca(),d.getExibirPlacaCompleta(),d.getIpvaPago(),d.getLicenciado()); }
        case CAMINHAO -> { var d=caminhoes.findById(id).orElseThrow(); yield new CriarAnuncioCommand.Caminhao(d.getQuilometragem(),d.getConfiguracao(),d.getCarroceria(),d.getCambio(),d.getCombustivel(),d.getTracao(),d.getTipoDirecao(),d.getNumeroEixos(),d.getCapacidadeCargaKg(),d.getPesoBrutoTotalKg(),d.getImplemento(),d.getPlaca(),d.getExibirPlacaCompleta(),d.getIpvaPago(),d.getLicenciado()); }
        case CAMINHONETE -> { var d=caminhonetes.findById(id).orElseThrow(); yield new CriarAnuncioCommand.Caminhonete(d.getQuilometragem(),d.getTipoCabine(),d.getCarroceria(),d.getCambio(),d.getCombustivel(),d.getTracao(),d.getMotorizacao(),d.getTipoDirecao(),d.getCilindradaLitros(),d.getCapacidadeCargaKg(),d.getNumeroPortas(),d.getPlaca(),d.getExibirPlacaCompleta(),d.getUnicoDono(),d.getIpvaPago(),d.getLicenciado(),d.getBlindado()); }
        case BARCO -> { var d=barcos.findById(id).orElseThrow(); var ms=motores.findByBarcoId(id).stream().map(m->new CriarAnuncioCommand.Motor(m.getPosicao(),m.getFabricante(),m.getModelo(),m.getPotenciaHp(),m.getAno(),m.getHorasUso(),m.getHorasDesdeRevisao(),m.getCombustivel())).toList(); yield new CriarAnuncioCommand.Barco(d.getTamanhoPes(),d.getEstilo(),d.getMaterialCasco(),d.getCapacidadePessoas(),d.getNumeroCabines(),d.getHorasUso(),d.getRegistroMaritimo(),ms); }
        case LINHA_AMARELA -> { var d=linhas.findById(id).orElseThrow(); yield new CriarAnuncioCommand.LinhaAmarela(d.getTipoMaquina(),d.getHorimetro(),d.getPesoOperacionalKg(),d.getPotenciaHp(),d.getTipoEsteiraOuPneu(),d.getCapacidadeCacambaM3(),d.getNumeroSerie()); }
    }; }

    private void atualizarDetalhes(TipoVeiculo tipo, UUID id, CriarAnuncioCommand.DetalhesVeiculo d, boolean alterado) {
        if (!alterado) return;
        switch(tipo) {
            case CARRO -> carros.findById(id).orElseThrow().atualizar(new JpaCriarAnuncioGateway.CarroDados(id,(CriarAnuncioCommand.Carro)d));
            case MOTO -> motos.findById(id).orElseThrow().atualizar(new JpaCriarAnuncioGateway.MotoDados(id,(CriarAnuncioCommand.Moto)d));
            case CAMINHAO -> caminhoes.findById(id).orElseThrow().atualizar(new JpaCriarAnuncioGateway.CaminhaoDados(id,(CriarAnuncioCommand.Caminhao)d));
            case CAMINHONETE -> caminhonetes.findById(id).orElseThrow().atualizar(new JpaCriarAnuncioGateway.CaminhoneteDados(id,(CriarAnuncioCommand.Caminhonete)d));
            case LINHA_AMARELA -> linhas.findById(id).orElseThrow().atualizar(new JpaCriarAnuncioGateway.LinhaAmarelaDados(id,(CriarAnuncioCommand.LinhaAmarela)d));
            case BARCO -> { var b=(CriarAnuncioCommand.Barco)d; barcos.findById(id).orElseThrow().atualizar(new JpaCriarAnuncioGateway.BarcoDados(id,b)); if(!b.motores().isEmpty()){ motores.findByBarcoId(id).forEach(motores::delete); b.motores().forEach(m->motores.save(MotorBarcoEntity.criar(new JpaCriarAnuncioGateway.MotorDados(UUID.randomUUID(),id,m)))); } }
        }
    }
    private CriarAnuncioCommand.Endereco merge(CriarAnuncioCommand.Endereco a, CriarAnuncioCommand.Endereco p) { return p==null?a:new CriarAnuncioCommand.Endereco(nn(p.cep(),a.cep()),nn(p.cidade(),a.cidade()),nn(p.bairro(),a.bairro()),nn(p.rua(),a.rua()),nn(p.numero(),a.numero()),nn(p.complemento(),a.complemento()),nn(p.uf(),a.uf())); }
    private CriarAnuncioCommand.DetalhesVeiculo mergeDetalhes(TipoVeiculo t, CriarAnuncioCommand.DetalhesVeiculo a, CriarAnuncioCommand.DetalhesVeiculo p) { if(p==null)return a; return switch(t){
        case CARRO -> {var x=(CriarAnuncioCommand.Carro)a;var y=(CriarAnuncioCommand.Carro)p;yield new CriarAnuncioCommand.Carro(nn(y.quilometragem(),x.quilometragem()),nn(y.carroceria(),x.carroceria()),nn(y.cambio(),x.cambio()),nn(y.combustivel(),x.combustivel()),nn(y.tracao(),x.tracao()),nn(y.motorizacao(),x.motorizacao()),nn(y.tipoDirecao(),x.tipoDirecao()),nn(y.cilindradaLitros(),x.cilindradaLitros()),nn(y.numeroPortas(),x.numeroPortas()),nn(y.numeroLugares(),x.numeroLugares()),nn(y.placa(),x.placa()),nn(y.exibirPlacaCompleta(),x.exibirPlacaCompleta()),nn(y.unicoDono(),x.unicoDono()),nn(y.ipvaPago(),x.ipvaPago()),nn(y.licenciado(),x.licenciado()),nn(y.blindado(),x.blindado()));}
        case MOTO -> {var x=(CriarAnuncioCommand.Moto)a;var y=(CriarAnuncioCommand.Moto)p;yield new CriarAnuncioCommand.Moto(nn(y.quilometragem(),x.quilometragem()),nn(y.cilindradas(),x.cilindradas()),nn(y.categoria(),x.categoria()),nn(y.partida(),x.partida()),nn(y.refrigeracao(),x.refrigeracao()),nn(y.cambio(),x.cambio()),nn(y.combustivel(),x.combustivel()),nn(y.placa(),x.placa()),nn(y.exibirPlacaCompleta(),x.exibirPlacaCompleta()),nn(y.ipvaPago(),x.ipvaPago()),nn(y.licenciado(),x.licenciado()));}
        case CAMINHAO -> {var x=(CriarAnuncioCommand.Caminhao)a;var y=(CriarAnuncioCommand.Caminhao)p;yield new CriarAnuncioCommand.Caminhao(nn(y.quilometragem(),x.quilometragem()),nn(y.configuracao(),x.configuracao()),nn(y.carroceria(),x.carroceria()),nn(y.cambio(),x.cambio()),nn(y.combustivel(),x.combustivel()),nn(y.tracao(),x.tracao()),nn(y.tipoDirecao(),x.tipoDirecao()),nn(y.numeroEixos(),x.numeroEixos()),nn(y.capacidadeCargaKg(),x.capacidadeCargaKg()),nn(y.pesoBrutoTotalKg(),x.pesoBrutoTotalKg()),nn(y.implemento(),x.implemento()),nn(y.placa(),x.placa()),nn(y.exibirPlacaCompleta(),x.exibirPlacaCompleta()),nn(y.ipvaPago(),x.ipvaPago()),nn(y.licenciado(),x.licenciado()));}
        case CAMINHONETE -> {var x=(CriarAnuncioCommand.Caminhonete)a;var y=(CriarAnuncioCommand.Caminhonete)p;yield new CriarAnuncioCommand.Caminhonete(nn(y.quilometragem(),x.quilometragem()),nn(y.tipoCabine(),x.tipoCabine()),nn(y.carroceria(),x.carroceria()),nn(y.cambio(),x.cambio()),nn(y.combustivel(),x.combustivel()),nn(y.tracao(),x.tracao()),nn(y.motorizacao(),x.motorizacao()),nn(y.tipoDirecao(),x.tipoDirecao()),nn(y.cilindradaLitros(),x.cilindradaLitros()),nn(y.capacidadeCargaKg(),x.capacidadeCargaKg()),nn(y.numeroPortas(),x.numeroPortas()),nn(y.placa(),x.placa()),nn(y.exibirPlacaCompleta(),x.exibirPlacaCompleta()),nn(y.unicoDono(),x.unicoDono()),nn(y.ipvaPago(),x.ipvaPago()),nn(y.licenciado(),x.licenciado()),nn(y.blindado(),x.blindado()));}
        case BARCO -> {var x=(CriarAnuncioCommand.Barco)a;var y=(CriarAnuncioCommand.Barco)p;yield new CriarAnuncioCommand.Barco(nn(y.tamanhoPes(),x.tamanhoPes()),nn(y.estilo(),x.estilo()),nn(y.materialCasco(),x.materialCasco()),nn(y.capacidadePessoas(),x.capacidadePessoas()),nn(y.numeroCabines(),x.numeroCabines()),nn(y.horasUso(),x.horasUso()),nn(y.registroMaritimo(),x.registroMaritimo()),y.motores().isEmpty()?x.motores():y.motores());}
        case LINHA_AMARELA -> {var x=(CriarAnuncioCommand.LinhaAmarela)a;var y=(CriarAnuncioCommand.LinhaAmarela)p;yield new CriarAnuncioCommand.LinhaAmarela(nn(y.tipoMaquina(),x.tipoMaquina()),nn(y.horimetro(),x.horimetro()),nn(y.pesoOperacionalKg(),x.pesoOperacionalKg()),nn(y.potenciaHp(),x.potenciaHp()),nn(y.tipoEsteiraOuPneu(),x.tipoEsteiraOuPneu()),nn(y.capacidadeCacambaM3(),x.capacidadeCacambaM3()),nn(y.numeroSerie(),x.numeroSerie()));}
    }; }
    private boolean compativel(TipoVeiculo t,Object d){return switch(t){case CARRO->d instanceof CriarAnuncioCommand.Carro;case MOTO->d instanceof CriarAnuncioCommand.Moto;case CAMINHAO->d instanceof CriarAnuncioCommand.Caminhao;case CAMINHONETE->d instanceof CriarAnuncioCommand.Caminhonete;case BARCO->d instanceof CriarAnuncioCommand.Barco;case LINHA_AMARELA->d instanceof CriarAnuncioCommand.LinhaAmarela;};}
    private static <T>T nn(T novo,T atual){return novo==null?atual:novo;}

    private static final class AnuncioAtualizadoDados extends Anuncio {
        private final AnuncioEntity atual; private final CriarAnuncioCommand c; private final StatusAnuncio status; private final Instant publicado,agora;
        AnuncioAtualizadoDados(AnuncioEntity atual,CriarAnuncioCommand c,StatusAnuncio status,Instant publicado,Instant agora){this.atual=atual;this.c=c;this.status=status;this.publicado=publicado;this.agora=agora;}
        public UUID getId(){return atual.getId();} public UUID getVeiculoId(){return atual.getVeiculoId();} public UUID getAnuncianteId(){return atual.getAnuncianteId();}
        public String getTitulo(){return c.titulo();} public String getDescricao(){return c.descricao();} public TipoPreco getTipoPreco(){return c.tipoPreco();} public Long getPrecoCentavos(){return c.precoCentavos();} public Boolean getAceitaTroca(){return c.aceitaTroca();}
        public UUID getEnderecoId(){return atual.getEnderecoId();} public StatusAnuncio getStatus(){return status;} public Instant getPublicadoEm(){return publicado;} public Instant getCriadoEm(){return atual.getCriadoEm();} public Instant getAtualizadoEm(){return agora;} public int getVersao(){return atual.getVersao();}
    }
}
