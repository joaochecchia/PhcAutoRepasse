package repasse.phcauto.backend.infra.controller.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/** Monta somente o envelope HTTP provisório; não executa regras ou persistência. */
@Component
public class CrudHttpResponseFactory {
    private static final Set<String> CAMPOS_SENSIVEIS = Set.of(
            "senha", "senhahash", "senha_hash", "password", "passwordhash",
            "access_token", "accesstoken", "refresh_token", "refreshtoken", "secret");

    public ResponseEntity<HashMap<String, Object>> criado(String mensagem, Object body) {
        return responder(HttpStatus.CREATED, mensagem, body);
    }

    public ResponseEntity<HashMap<String, Object>> sucesso(String mensagem, Object body) {
        return responder(HttpStatus.OK, mensagem, body);
    }

    public HashMap<String, Object> identificador(String nome, Object valor) {
        var body = new HashMap<String, Object>();
        body.put(nome, valor);
        return body;
    }

    public HashMap<String, Object> identificadores(String primeiroNome, Object primeiroValor,
            String segundoNome, Object segundoValor) {
        var body = identificador(primeiroNome, primeiroValor);
        body.put(segundoNome, segundoValor);
        return body;
    }

    public HashMap<String, Object> pagina(int offset, int limite) {
        var body = new HashMap<String, Object>();
        body.put("offset", offset);
        body.put("limite", limite);
        body.put("itens", List.of());
        return body;
    }

    private ResponseEntity<HashMap<String, Object>> responder(HttpStatus status,
            String mensagem, Object body) {
        var response = new HashMap<String, Object>();
        response.put("menssage", mensagem);
        response.put("Body", sanitizar(body));
        return ResponseEntity.status(status).body(response);
    }

    private Object sanitizar(Object value) {
        if (value instanceof Map<?, ?> map) {
            var sanitized = new HashMap<String, Object>();
            map.forEach((key, item) -> {
                var nome = String.valueOf(key);
                if (!CAMPOS_SENSIVEIS.contains(nome.toLowerCase(Locale.ROOT))) {
                    sanitized.put(nome, sanitizar(item));
                }
            });
            return sanitized;
        }
        if (value instanceof Iterable<?> iterable) {
            var sanitized = new ArrayList<>();
            iterable.forEach(item -> sanitized.add(sanitizar(item)));
            return sanitized;
        }
        return value;
    }
}
