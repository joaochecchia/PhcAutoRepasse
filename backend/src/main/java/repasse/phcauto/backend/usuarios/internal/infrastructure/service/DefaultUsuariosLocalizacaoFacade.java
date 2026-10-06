package repasse.phcauto.backend.usuarios.internal.infrastructure.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repasse.phcauto.backend.localizacao.Coordenadas;
import repasse.phcauto.backend.localizacao.LocalizacaoFacade;
import repasse.phcauto.backend.usuarios.UsuariosLocalizacaoFacade;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.repository.write.EnderecoUsuarioWriteRepository;

@Service
class DefaultUsuariosLocalizacaoFacade implements UsuariosLocalizacaoFacade {
    private final EnderecoUsuarioWriteRepository enderecos;
    private final LocalizacaoFacade localizacao;

    DefaultUsuariosLocalizacaoFacade(
            EnderecoUsuarioWriteRepository enderecos,
            LocalizacaoFacade localizacao) {
        this.enderecos = enderecos;
        this.localizacao = localizacao;
    }

    @Override
    @Transactional(transactionManager = "writeTransactionManager", readOnly = true)
    public Coordenadas buscarEnderecoCadastrado(UUID usuarioId) {
        var endereco = enderecos.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não possui endereço cadastrado"));
        var municipio = localizacao.buscarMunicipio(endereco.getMunicipioCodigoIbge());
        return municipio.coordenadas();
    }
}
