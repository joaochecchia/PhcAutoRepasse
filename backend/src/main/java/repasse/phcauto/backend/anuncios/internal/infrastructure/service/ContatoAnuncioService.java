package repasse.phcauto.backend.anuncios.internal.infrastructure.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.*;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.request.CriarAnuncioRequest.ContatoAnuncianteRequest;

@Service
public class ContatoAnuncioService {
    public static final String VERSAO_TEXTO = "1.0";
    private static final int LIMITE_POR_HORA = 10;
    private final JdbcTemplate jdbc;
    private final Clock clock;

    public ContatoAnuncioService(@Qualifier("writeDataSource") DataSource source, Clock applicationClock) {
        this.jdbc = new JdbcTemplate(source);
        this.clock = applicationClock;
    }

    @Transactional(transactionManager = "writeTransactionManager")
    public void salvarPreferencias(UUID anuncioId, UUID anuncianteId, ContatoAnuncianteRequest contato) {
        boolean whatsapp = contato != null && contato.whatsapp() && !contato.naoDivulgar();
        boolean ligacao = contato != null && contato.ligacao() && !contato.naoDivulgar();
        if ((whatsapp || ligacao) && !VERSAO_TEXTO.equals(contato.versaoTexto())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Versão do consentimento de contato inválida");
        }
        int alterados = jdbc.update("""
                update catalogo.anuncios set contato_whatsapp_autorizado=?, contato_ligacao_autorizado=?,
                    contato_consentido_em=?, contato_texto_versao=?
                where id=? and anunciante_id=?
                """, whatsapp, ligacao, whatsapp || ligacao ? Timestamp.from(clock.instant()) : null,
                whatsapp || ligacao ? VERSAO_TEXTO : null, anuncioId, anuncianteId);
        if (alterados != 1) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Anúncio não encontrado");
    }

    @Transactional(transactionManager = "writeTransactionManager", noRollbackFor = ResponseStatusException.class)
    public ContatoLiberado liberarWhatsapp(UUID anuncioId, UUID interessadoId, String enderecoRede, String userAgent) {
        jdbc.query("select pg_advisory_xact_lock(hashtext(?))", rs -> null, interessadoId + ":" + anuncioId);
        var dados = jdbc.query("""
                select a.status, a.contato_whatsapp_autorizado, u.telefone, v.fabricante, v.modelo
                from catalogo.anuncios a
                join identidade.usuarios u on u.id = a.anunciante_id and u.ativo = true
                join catalogo.veiculos v on v.id = a.veiculo_id
                where a.id = ?
                """, rs -> rs.next() ? new DadosContato(rs.getString(1), rs.getBoolean(2),
                        rs.getString(3), rs.getString(4), rs.getString(5)) : null, anuncioId);
        if (dados == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Anúncio não encontrado");
        if (!"PUBLICADO".equals(dados.status)) return negar(anuncioId, interessadoId, "ANUNCIO_INATIVO", enderecoRede,
                userAgent, HttpStatus.CONFLICT, "Este anúncio não está disponível para contato");
        if (!dados.whatsapp) return negar(anuncioId, interessadoId, "SEM_CONSENTIMENTO", enderecoRede,
                userAgent, HttpStatus.CONFLICT, "O anunciante não disponibilizou contato por WhatsApp");
        String telefone = normalizarTelefone(dados.telefone);
        if (telefone == null) return negar(anuncioId, interessadoId, "TELEFONE_INVALIDO", enderecoRede,
                userAgent, HttpStatus.CONFLICT, "O contato do anunciante não está disponível");
        Integer acessos = jdbc.queryForObject("""
                select count(*) from catalogo.acessos_contato_anuncio
                where interessado_id=? and anuncio_id=? and autorizado=true and ocorrido_em>=?
                """, Integer.class, interessadoId, anuncioId, Timestamp.from(clock.instant().minus(Duration.ofHours(1))));
        if (acessos != null && acessos >= LIMITE_POR_HORA) return negar(anuncioId, interessadoId, "LIMITE_EXCEDIDO",
                enderecoRede, userAgent, HttpStatus.TOO_MANY_REQUESTS, "Muitas tentativas de contato. Tente novamente mais tarde");
        auditar(anuncioId, interessadoId, true, "WHATSAPP_LIBERADO", enderecoRede, userAgent);
        String mensagem = URLEncoder.encode("Olá! Tenho interesse no " + dados.fabricante + " " + dados.modelo
                + " anunciado na PHC Auto.", StandardCharsets.UTF_8).replace("+", "%20");
        return new ContatoLiberado("https://wa.me/" + telefone + "?text=" + mensagem);
    }

    private <T> T negar(UUID anuncio, UUID interessado, String motivo, String rede, String agente,
            HttpStatus status, String mensagem) {
        auditar(anuncio, interessado, false, motivo, rede, agente);
        throw new ResponseStatusException(status, mensagem);
    }

    private void auditar(UUID anuncio, UUID interessado, boolean autorizado, String motivo, String rede, String agente) {
        jdbc.update("""
                insert into catalogo.acessos_contato_anuncio
                    (id, anuncio_id, interessado_id, autorizado, motivo, endereco_rede, user_agent, ocorrido_em)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                """, UUID.randomUUID(), anuncio, interessado, autorizado, motivo, limitar(rede, 64),
                limitar(agente, 512), Timestamp.from(clock.instant()));
    }

    private static String normalizarTelefone(String valor) {
        if (valor == null) return null;
        String digitos = valor.replaceAll("\\D", "");
        if (digitos.length() == 10 || digitos.length() == 11) digitos = "55" + digitos;
        return digitos.matches("[1-9][0-9]{11,14}") ? digitos : null;
    }
    private static String limitar(String valor, int tamanho) {
        return valor == null ? null : valor.substring(0, Math.min(valor.length(), tamanho));
    }
    private record DadosContato(String status, boolean whatsapp, String telefone, String fabricante, String modelo) { }
    public record ContatoLiberado(String url) { }
}
