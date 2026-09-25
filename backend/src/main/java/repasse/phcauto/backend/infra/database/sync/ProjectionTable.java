package repasse.phcauto.backend.infra.database.sync;

import java.util.List;
import repasse.phcauto.backend.infra.database.entity.identidade.*;
import repasse.phcauto.backend.usuarios.internal.infrastructure.entity.EnderecoUsuarioEntity;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import repasse.phcauto.backend.infra.database.entity.catalogo.VeiculoCaracteristicaJpaId;
import repasse.phcauto.backend.infra.database.entity.assinaturas.AssinaturaEntity;
import repasse.phcauto.backend.infra.database.entity.assinaturas.PlanoEntity;
import repasse.phcauto.backend.infra.database.entity.catalogo.AnuncioEntity;
import repasse.phcauto.backend.infra.database.entity.catalogo.BarcoEntity;
import repasse.phcauto.backend.infra.database.entity.catalogo.CaminhaoEntity;
import repasse.phcauto.backend.infra.database.entity.catalogo.CaminhoneteEntity;
import repasse.phcauto.backend.infra.database.entity.catalogo.CaracteristicaEntity;
import repasse.phcauto.backend.infra.database.entity.catalogo.CarroEntity;
import repasse.phcauto.backend.infra.database.entity.catalogo.FotoEntity;
import repasse.phcauto.backend.infra.database.entity.catalogo.LinhaAmarelaEntity;
import repasse.phcauto.backend.infra.database.entity.catalogo.MotoEntity;
import repasse.phcauto.backend.infra.database.entity.catalogo.MotorBarcoEntity;
import repasse.phcauto.backend.infra.database.entity.catalogo.VeiculoCaracteristicaEntity;
import repasse.phcauto.backend.infra.database.entity.catalogo.VeiculoEntity;
import repasse.phcauto.backend.usuarios.internal.infrastructure.entity.UsuarioEntity;
import repasse.phcauto.backend.usuarios.internal.infrastructure.entity.UsuarioPfEntity;
import repasse.phcauto.backend.usuarios.internal.infrastructure.entity.UsuarioPjEntity;
import repasse.phcauto.backend.infra.database.entity.vendas.CompraEntity;
import repasse.phcauto.backend.infra.database.entity.vendas.EventoGatewayEntity;
import repasse.phcauto.backend.infra.database.entity.vendas.PagamentoEntity;

/** Allowlist dos mapeamentos que compõem a projeção. */
public enum ProjectionTable {
    ENDERECOS_USUARIO("identidade.enderecos_usuario", EnderecoUsuarioEntity.class, List.of("usuario_id"), List.of("uuid"), List.of("usuario_id", "cep", "cidade", "bairro", "rua", "numero", "complemento", "uf", "lock_version")),
    DADOS_COMPRA_PF("identidade.dados_compra_pf", DadosCompraPfEntity.class, List.of("usuario_id"), List.of("uuid"), List.of("usuario_id", "rg", "nome_pai", "nome_mae", "naturalidade", "genero", "lock_version")),
    DADOS_COMPRA_PJ("identidade.dados_compra_pj", DadosCompraPjEntity.class, List.of("usuario_id"), List.of("uuid"), List.of("usuario_id", "inscricao_estadual", "regime_tributario", "lock_version")),
    IDENTIDADES_EXTERNAS("identidade.identidades_externas", IdentidadeExternaEntity.class, List.of("id"), List.of("uuid"), List.of("id", "usuario_id", "provedor", "identificador_externo", "lock_version")),
    ASSINATURAS("assinaturas.assinaturas", AssinaturaEntity.class, List.of("id"), List.of("uuid"), List.of("id", "usuario_id", "plano_id", "valor_contratado_centavos", "status", "inicio_em", "fim_em", "criado_em", "lock_version")),
    PLANOS("assinaturas.planos", PlanoEntity.class, List.of("id"), List.of("uuid"), List.of("id", "nome", "valor_centavos", "periodo_meses", "limite_anuncios", "ativo", "criado_em", "lock_version")),
    ANUNCIOS("catalogo.anuncios", AnuncioEntity.class, List.of("id"), List.of("uuid"), List.of("id", "veiculo_id", "anunciante_id", "titulo", "descricao", "tipo_preco", "preco_centavos", "aceita_troca", "cidade", "uf", "status", "publicado_em", "criado_em", "atualizado_em", "versao")),
    BARCOS("catalogo.barcos", BarcoEntity.class, List.of("veiculo_id"), List.of("uuid"), List.of("veiculo_id", "tamanho_pes", "estilo", "material_casco", "capacidade_pessoas", "numero_cabines", "horas_uso", "registro_maritimo", "lock_version")),
    CAMINHOES("catalogo.caminhoes", CaminhaoEntity.class, List.of("veiculo_id"), List.of("uuid"), List.of("veiculo_id", "quilometragem", "configuracao", "carroceria", "cambio", "combustivel", "tracao", "numero_eixos", "capacidade_carga_kg", "peso_bruto_total_kg", "implemento", "final_placa", "ipva_pago", "licenciado", "lock_version")),
    CAMINHONETES("catalogo.caminhonetes", CaminhoneteEntity.class, List.of("veiculo_id"), List.of("uuid"), List.of("veiculo_id", "quilometragem", "tipo_cabine", "carroceria", "cambio", "combustivel", "tracao", "motorizacao", "capacidade_carga_kg", "numero_portas", "final_placa", "unico_dono", "ipva_pago", "licenciado", "lock_version")),
    CARACTERISTICAS("catalogo.caracteristicas", CaracteristicaEntity.class, List.of("id"), List.of("uuid"), List.of("id", "tipo_veiculo", "nome", "grupo", "lock_version")),
    CARROS("catalogo.carros", CarroEntity.class, List.of("veiculo_id"), List.of("uuid"), List.of("veiculo_id", "quilometragem", "carroceria", "cambio", "combustivel", "tracao", "motorizacao", "numero_portas", "numero_lugares", "final_placa", "unico_dono", "ipva_pago", "licenciado", "blindado", "lock_version")),
    FOTOS("catalogo.fotos", FotoEntity.class, List.of("id"), List.of("uuid"), List.of("id", "anuncio_id", "chave_arquivo", "posicao", "texto_alternativo", "lock_version")),
    LINHA_AMARELA("catalogo.linha_amarela", LinhaAmarelaEntity.class, List.of("veiculo_id"), List.of("uuid"), List.of("veiculo_id", "tipo_maquina", "horimetro", "peso_operacional_kg", "potencia_hp", "tipo_esteira_ou_pneu", "capacidade_cacamba_m3", "numero_serie", "lock_version")),
    MOTOS("catalogo.motos", MotoEntity.class, List.of("veiculo_id"), List.of("uuid"), List.of("veiculo_id", "quilometragem", "cilindradas", "categoria", "partida", "refrigeracao", "cambio", "combustivel", "final_placa", "ipva_pago", "licenciado", "lock_version")),
    MOTORES_BARCO("catalogo.motores_barco", MotorBarcoEntity.class, List.of("id"), List.of("uuid"), List.of("id", "barco_id", "posicao", "fabricante", "modelo", "potencia_hp", "ano", "horas_uso", "horas_desde_revisao", "combustivel", "lock_version")),
    VEICULO_CARACTERISTICAS("catalogo.veiculo_caracteristicas", VeiculoCaracteristicaEntity.class, List.of("veiculo_id", "caracteristica_id"), List.of("uuid", "uuid"), List.of("veiculo_id", "caracteristica_id", "observacao", "lock_version")),
    VEICULOS("catalogo.veiculos", VeiculoEntity.class, List.of("id"), List.of("uuid"), List.of("id", "proprietario_id", "tipo", "fabricante", "modelo", "versao", "ano_fabricacao", "ano_modelo", "cor", "identificador_publico", "criado_em", "atualizado_em", "lock_version")),
    USUARIOS("identidade.usuarios", UsuarioEntity.class, List.of("id"), List.of("uuid"), List.of("id", "nome", "email", "senha_hash", "telefone", "tipo_pessoa", "papel", "ativo", "criado_em", "atualizado_em", "lock_version")),
    USUARIOS_PF("identidade.usuarios_pf", UsuarioPfEntity.class, List.of("usuario_id"), List.of("uuid"), List.of("usuario_id", "cpf", "data_nascimento", "lock_version")),
    USUARIOS_PJ("identidade.usuarios_pj", UsuarioPjEntity.class, List.of("usuario_id"), List.of("uuid"), List.of("usuario_id", "cnpj", "razao_social", "nome_fantasia", "lock_version")),
    COMPRAS("vendas.compras", CompraEntity.class, List.of("id"), List.of("uuid"), List.of("id", "anuncio_id", "comprador_id", "vendedor_id", "valor_centavos", "titulo_veiculo_snapshot", "status", "criado_em", "pago_em", "lock_version")),
    EVENTOS_GATEWAY("vendas.eventos_gateway", EventoGatewayEntity.class, List.of("id"), List.of("bigint"), List.of("id", "provedor", "evento_externo_id", "pagamento_id", "recebido_em", "processado_em", "lock_version")),
    PAGAMENTOS("vendas.pagamentos", PagamentoEntity.class, List.of("id"), List.of("uuid"), List.of("id", "compra_id", "provedor", "referencia_externa", "metodo", "status", "valor_centavos", "vence_em", "confirmado_em", "criado_em", "lock_version"));

    public final String sqlName;
    public final Class<?> entityClass;
    public final List<String> keys;
    public final List<String> keyTypes;
    public final List<String> columns;

    ProjectionTable(String sqlName, Class<?> entityClass, List<String> keys,
                    List<String> keyTypes, List<String> columns) {
        this.sqlName = sqlName;
        this.entityClass = entityClass;
        this.keys = keys;
        this.keyTypes = keyTypes;
        this.columns = columns;
    }
    public static ProjectionTable forEntity(Class<?> type) {
        return Arrays.stream(values()).filter(t -> t.entityClass.equals(type)).findFirst().orElseThrow();
    }
    public List<String> key(Object id) {
        if (id instanceof VeiculoCaracteristicaJpaId composite) {
            return List.of(composite.veiculoId().toString(), composite.caracteristicaId().toString());
        }
        return List.of(id.toString());
    }
    public String predicate() {
        return IntStream.range(0, keys.size()).mapToObj(i -> keys.get(i) + " = cast(? as " + keyTypes.get(i) + ")")
                .collect(Collectors.joining(" and "));
    }
    public String upsert() {
        String assignments = columns.stream().filter(c -> !keys.contains(c))
                .map(c -> c + " = excluded." + c).collect(Collectors.joining(", "));
        return "insert into " + sqlName + " select (json_populate_record(null::" + sqlName
                + ", cast(? as json))).* on conflict (" + String.join(",", keys)
                + ") do update set " + assignments;
    }
}
