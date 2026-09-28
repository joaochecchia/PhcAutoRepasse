package repasse.phcauto.backend.anuncios.internal.infrastructure;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import repasse.phcauto.backend.anuncios.internal.core.CriarAnuncioCommand;
import repasse.phcauto.backend.anuncios.internal.core.CriarAnuncioGateway;
import repasse.phcauto.backend.domain.model.catalogo.*;
import repasse.phcauto.backend.infra.database.entity.catalogo.*;
import repasse.phcauto.backend.infra.database.repository.write.catalogo.*;

@Repository
@Transactional(transactionManager = "writeTransactionManager", propagation = Propagation.MANDATORY)
class JpaCriarAnuncioGateway implements CriarAnuncioGateway {
    private final VeiculoWriteRepository veiculos;
    private final CarroWriteRepository carros;
    private final MotoWriteRepository motos;
    private final CaminhaoWriteRepository caminhoes;
    private final CaminhoneteWriteRepository caminhonetes;
    private final BarcoWriteRepository barcos;
    private final MotorBarcoWriteRepository motores;
    private final LinhaAmarelaWriteRepository linhasAmarelas;
    private final EnderecoAnuncioWriteRepository enderecos;
    private final AnuncioWriteRepository anuncios;

    @PersistenceContext(unitName = "write")
    private EntityManager entityManager;

    JpaCriarAnuncioGateway(VeiculoWriteRepository veiculos, CarroWriteRepository carros,
            MotoWriteRepository motos, CaminhaoWriteRepository caminhoes,
            CaminhoneteWriteRepository caminhonetes, BarcoWriteRepository barcos,
            MotorBarcoWriteRepository motores, LinhaAmarelaWriteRepository linhasAmarelas,
            EnderecoAnuncioWriteRepository enderecos, AnuncioWriteRepository anuncios) {
        this.veiculos = veiculos;
        this.carros = carros;
        this.motos = motos;
        this.caminhoes = caminhoes;
        this.caminhonetes = caminhonetes;
        this.barcos = barcos;
        this.motores = motores;
        this.linhasAmarelas = linhasAmarelas;
        this.enderecos = enderecos;
        this.anuncios = anuncios;
    }

    @Override
    public boolean usuarioAtivo(UUID usuarioId) {
        Number count = (Number) entityManager.createNativeQuery(
                "select count(*) from identidade.usuarios where id = :id and ativo = true")
                .setParameter("id", usuarioId).getSingleResult();
        return count.longValue() == 1;
    }

    @Override
    public void salvar(UUID anuncioId, UUID veiculoId, UUID enderecoId, CriarAnuncioCommand c,
            StatusAnuncio status, Instant agora) {
        veiculos.save(VeiculoEntity.criar(new VeiculoDados(veiculoId, c, agora)));
        salvarEspecializacao(veiculoId, c);
        enderecos.save(EnderecoAnuncioEntity.criar(new EnderecoDados(enderecoId, c.endereco())));
        anuncios.save(AnuncioEntity.criar(new AnuncioDados(anuncioId, veiculoId, enderecoId, c, status, agora)));
    }

    private void salvarEspecializacao(UUID id, CriarAnuncioCommand c) {
        switch (c.tipoVeiculo()) {
            case CARRO -> carros.save(CarroEntity.criar(new CarroDados(id, (CriarAnuncioCommand.Carro) c.detalhes())));
            case MOTO -> motos.save(MotoEntity.criar(new MotoDados(id, (CriarAnuncioCommand.Moto) c.detalhes())));
            case CAMINHAO -> caminhoes.save(CaminhaoEntity.criar(new CaminhaoDados(id, (CriarAnuncioCommand.Caminhao) c.detalhes())));
            case CAMINHONETE -> caminhonetes.save(CaminhoneteEntity.criar(new CaminhoneteDados(id, (CriarAnuncioCommand.Caminhonete) c.detalhes())));
            case LINHA_AMARELA -> linhasAmarelas.save(LinhaAmarelaEntity.criar(new LinhaAmarelaDados(id, (CriarAnuncioCommand.LinhaAmarela) c.detalhes())));
            case BARCO -> {
                var dados = (CriarAnuncioCommand.Barco) c.detalhes();
                barcos.save(BarcoEntity.criar(new BarcoDados(id, dados)));
                dados.motores().forEach(m -> motores.save(MotorBarcoEntity.criar(new MotorDados(UUID.randomUUID(), id, m))));
            }
        }
    }

    static final class VeiculoDados extends Veiculo {
        private final UUID id; private final CriarAnuncioCommand c; private final Instant agora;
        VeiculoDados(UUID id, CriarAnuncioCommand c, Instant agora) { this.id=id; this.c=c; this.agora=agora; }
        public UUID getId(){return id;} public UUID getProprietarioId(){return c.anuncianteId();}
        public TipoVeiculo getTipo(){return c.tipoVeiculo();} public String getFabricante(){return c.fabricante();}
        public String getModelo(){return c.modelo();} public String getVersao(){return c.versao();}
        public Integer getAnoFabricacao(){return c.anoFabricacao();} public Integer getAnoModelo(){return c.anoModelo();}
        public String getCor(){return c.cor();} public String getIdentificadorPublico(){return c.identificadorPublico();}
        public repasse.phcauto.backend.domain.model.catalogo.CondicaoVeiculo getCondicao(){return c.condicao();} public String getTipoFreio(){return c.tipoFreio();}
        public Instant getCriadoEm(){return agora;} public Instant getAtualizadoEm(){return agora;}
    }

    static final class EnderecoDados extends EnderecoAnuncio {
        private final UUID id; private final CriarAnuncioCommand.Endereco e;
        EnderecoDados(UUID id, CriarAnuncioCommand.Endereco e){this.id=id;this.e=e;}
        public UUID getId(){return id;} public String getCep(){return e.cep();} public String getCidade(){return e.cidade();}
        public String getBairro(){return e.bairro();} public String getRua(){return e.rua();} public String getNumero(){return e.numero();}
        public String getComplemento(){return e.complemento();} public String getUf(){return e.uf();}
    }

    static final class AnuncioDados extends Anuncio {
        private final UUID id, veiculoId, enderecoId; private final CriarAnuncioCommand c;
        private final StatusAnuncio status; private final Instant agora;
        AnuncioDados(UUID id, UUID veiculoId, UUID enderecoId, CriarAnuncioCommand c, StatusAnuncio status, Instant agora){
            this.id=id;this.veiculoId=veiculoId;this.enderecoId=enderecoId;this.c=c;this.status=status;this.agora=agora;}
        public UUID getId(){return id;} public UUID getVeiculoId(){return veiculoId;} public UUID getAnuncianteId(){return c.anuncianteId();}
        public String getTitulo(){return c.titulo();} public String getDescricao(){return c.descricao();} public TipoPreco getTipoPreco(){return c.tipoPreco();}
        public Long getPrecoCentavos(){return c.precoCentavos();} public Boolean getAceitaTroca(){return c.aceitaTroca();}
        public UUID getEnderecoId(){return enderecoId;} public StatusAnuncio getStatus(){return status;}
        public Instant getPublicadoEm(){return status==StatusAnuncio.PUBLICADO?agora:null;} public Instant getCriadoEm(){return agora;}
        public Instant getAtualizadoEm(){return agora;} public int getVersao(){return 0;}
    }

    static final class CarroDados extends Carro {
        private final UUID id; private final CriarAnuncioCommand.Carro d; CarroDados(UUID id,CriarAnuncioCommand.Carro d){this.id=id;this.d=d;}
        public UUID getVeiculoId(){return id;} public Integer getQuilometragem(){return d.quilometragem();} public String getCarroceria(){return d.carroceria();}
        public String getCambio(){return d.cambio();} public String getCombustivel(){return d.combustivel();} public String getTracao(){return d.tracao();}
        public String getMotorizacao(){return d.motorizacao();} public String getTipoDirecao(){return d.tipoDirecao();}
        public BigDecimal getCilindradaLitros(){return d.cilindradaLitros();} public Integer getNumeroPortas(){return d.numeroPortas();}
        public Integer getNumeroLugares(){return d.numeroLugares();} public String getFinalPlaca(){return upper(d.finalPlaca());}
        public Boolean getUnicoDono(){return d.unicoDono();} public Boolean getIpvaPago(){return d.ipvaPago();}
        public Boolean getLicenciado(){return d.licenciado();} public Boolean getBlindado(){return d.blindado();}
    }
    static final class MotoDados extends Moto {
        private final UUID id; private final CriarAnuncioCommand.Moto d; MotoDados(UUID id,CriarAnuncioCommand.Moto d){this.id=id;this.d=d;}
        public UUID getVeiculoId(){return id;} public Integer getQuilometragem(){return d.quilometragem();} public Integer getCilindradas(){return d.cilindradas();}
        public String getCategoria(){return d.categoria();} public String getPartida(){return d.partida();} public String getRefrigeracao(){return d.refrigeracao();}
        public String getCambio(){return d.cambio();} public String getCombustivel(){return d.combustivel();} public String getFinalPlaca(){return upper(d.finalPlaca());}
        public Boolean getIpvaPago(){return d.ipvaPago();} public Boolean getLicenciado(){return d.licenciado();}
    }
    static final class CaminhaoDados extends Caminhao {
        private final UUID id; private final CriarAnuncioCommand.Caminhao d; CaminhaoDados(UUID id,CriarAnuncioCommand.Caminhao d){this.id=id;this.d=d;}
        public UUID getVeiculoId(){return id;} public Integer getQuilometragem(){return d.quilometragem();} public String getConfiguracao(){return d.configuracao();}
        public String getCarroceria(){return d.carroceria();} public String getCambio(){return d.cambio();} public String getCombustivel(){return d.combustivel();}
        public String getTracao(){return d.tracao();} public String getTipoDirecao(){return d.tipoDirecao();} public Integer getNumeroEixos(){return d.numeroEixos();} public Integer getCapacidadeCargaKg(){return d.capacidadeCargaKg();}
        public Integer getPesoBrutoTotalKg(){return d.pesoBrutoTotalKg();} public String getImplemento(){return d.implemento();} public String getFinalPlaca(){return upper(d.finalPlaca());}
        public Boolean getIpvaPago(){return d.ipvaPago();} public Boolean getLicenciado(){return d.licenciado();}
    }
    static final class CaminhoneteDados extends Caminhonete {
        private final UUID id; private final CriarAnuncioCommand.Caminhonete d; CaminhoneteDados(UUID id,CriarAnuncioCommand.Caminhonete d){this.id=id;this.d=d;}
        public UUID getVeiculoId(){return id;} public Integer getQuilometragem(){return d.quilometragem();} public String getTipoCabine(){return d.tipoCabine();}
        public String getCarroceria(){return d.carroceria();} public String getCambio(){return d.cambio();} public String getCombustivel(){return d.combustivel();}
        public String getTracao(){return d.tracao();} public String getMotorizacao(){return d.motorizacao();} public String getTipoDirecao(){return d.tipoDirecao();}
        public BigDecimal getCilindradaLitros(){return d.cilindradaLitros();} public Integer getCapacidadeCargaKg(){return d.capacidadeCargaKg();}
        public Integer getNumeroPortas(){return d.numeroPortas();} public String getFinalPlaca(){return upper(d.finalPlaca());} public Boolean getUnicoDono(){return d.unicoDono();}
        public Boolean getIpvaPago(){return d.ipvaPago();} public Boolean getLicenciado(){return d.licenciado();} public Boolean getBlindado(){return d.blindado();}
    }
    static final class BarcoDados extends Barco {
        private final UUID id; private final CriarAnuncioCommand.Barco d; BarcoDados(UUID id,CriarAnuncioCommand.Barco d){this.id=id;this.d=d;}
        public UUID getVeiculoId(){return id;} public BigDecimal getTamanhoPes(){return d.tamanhoPes();} public String getEstilo(){return d.estilo();}
        public String getMaterialCasco(){return d.materialCasco();} public Integer getCapacidadePessoas(){return d.capacidadePessoas();}
        public Integer getNumeroCabines(){return d.numeroCabines();} public Integer getHorasUso(){return d.horasUso();} public String getRegistroMaritimo(){return d.registroMaritimo();}
    }
    static final class MotorDados extends MotorBarco {
        private final UUID id,barcoId; private final CriarAnuncioCommand.Motor d; MotorDados(UUID id,UUID barcoId,CriarAnuncioCommand.Motor d){this.id=id;this.barcoId=barcoId;this.d=d;}
        public UUID getId(){return id;} public UUID getBarcoId(){return barcoId;} public int getPosicao(){return d.posicao();}
        public String getFabricante(){return d.fabricante();} public String getModelo(){return d.modelo();} public BigDecimal getPotenciaHp(){return d.potenciaHp();}
        public Integer getAno(){return d.ano();} public Integer getHorasUso(){return d.horasUso();} public Integer getHorasDesdeRevisao(){return d.horasDesdeRevisao();}
        public String getCombustivel(){return d.combustivel();}
    }
    static final class LinhaAmarelaDados extends LinhaAmarela {
        private final UUID id; private final CriarAnuncioCommand.LinhaAmarela d; LinhaAmarelaDados(UUID id,CriarAnuncioCommand.LinhaAmarela d){this.id=id;this.d=d;}
        public UUID getVeiculoId(){return id;} public String getTipoMaquina(){return d.tipoMaquina();} public Integer getHorimetro(){return d.horimetro();}
        public Integer getPesoOperacionalKg(){return d.pesoOperacionalKg();} public BigDecimal getPotenciaHp(){return d.potenciaHp();}
        public String getTipoEsteiraOuPneu(){return d.tipoEsteiraOuPneu();} public BigDecimal getCapacidadeCacambaM3(){return d.capacidadeCacambaM3();}
        public String getNumeroSerie(){return d.numeroSerie();}
    }

    private static String upper(String value) { return value == null ? null : value.toUpperCase(); }
}
