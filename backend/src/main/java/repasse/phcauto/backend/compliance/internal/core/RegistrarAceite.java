package repasse.phcauto.backend.compliance.internal.core;

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import repasse.phcauto.backend.compliance.AceiteInvalidoException;
import repasse.phcauto.backend.compliance.RegistrarAceiteCadastroRequest;

public final class RegistrarAceite implements RegistrarAceiteUseCase {
    private static final Duration TOLERANCIA_RELOGIO = Duration.ofMinutes(5);
    private static final Duration IDADE_MAXIMA_DO_CLIQUE = Duration.ofHours(24);

    private final AceiteTermosGateway gateway;
    private final VersoesDocumentosVigentes versoes;
    private final Clock clock;

    public RegistrarAceite(AceiteTermosGateway gateway,
            VersoesDocumentosVigentes versoes, Clock clock) {
        this.gateway = Objects.requireNonNull(gateway);
        this.versoes = Objects.requireNonNull(versoes);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public void execute(RegistrarAceiteCadastroRequest request) {
        if (request == null || request.usuarioId() == null) {
            throw new AceiteInvalidoException("Identificador do usuário é obrigatório");
        }
        if (!Boolean.TRUE.equals(request.aceitouTermos())) {
            throw new AceiteInvalidoException("O aceite dos Termos de Uso e da Política de Privacidade é obrigatório");
        }
        var agora = clock.instant();
        if (request.aceiteEm() == null) {
            throw new AceiteInvalidoException("Data e horário do aceite são obrigatórios");
        }
        if (request.aceiteEm().isAfter(agora.plus(TOLERANCIA_RELOGIO))
                || request.aceiteEm().isBefore(agora.minus(IDADE_MAXIMA_DO_CLIQUE))) {
            throw new AceiteInvalidoException("Data e horário do aceite são incompatíveis com o cadastro");
        }
        if (!versoes.termosUso().equals(normalizar(request.versaoTermosUso()))
                || !versoes.politicaPrivacidade().equals(normalizar(request.versaoPoliticaPrivacidade()))) {
            throw new AceiteInvalidoException("As versões aceitas não correspondem aos documentos vigentes");
        }
        String enderecoRede = normalizar(request.enderecoRede());
        if (enderecoRede == null || enderecoRede.length() > 255) {
            throw new AceiteInvalidoException("Identificador de rede do aceite é inválido");
        }
        gateway.salvar(new AceiteTermos(UUID.randomUUID(), request.usuarioId(),
                request.aceiteEm(), versoes.termosUso(), versoes.politicaPrivacidade(),
                enderecoRede, agora));
    }

    private static String normalizar(String valor) {
        if (valor == null) return null;
        valor = valor.trim();
        return valor.isEmpty() ? null : valor;
    }
}
