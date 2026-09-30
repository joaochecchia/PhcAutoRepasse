package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import repasse.phcauto.backend.domain.model.identidade.PapelUsuario;

import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.request.CriarUsuarioRequest;
public class CadastroPorPapelValidator implements ConstraintValidator<CadastroPorPapelValido, CriarUsuarioRequest> {
    @Override public boolean isValid(CriarUsuarioRequest r, ConstraintValidatorContext context) {
        if (r == null) return true;
        var papel = r.papel() == null ? PapelUsuario.CLIENTE : r.papel();
        if (papel == PapelUsuario.DONO)
            return r.tipoPessoa() == null && vazio(r.telefone()) && r.cpf() == null
                    && r.dataNascimento() == null && r.cnpj() == null && r.razaoSocial() == null
                    && r.endereco() == null && r.aceitouTermos() == null && r.aceiteTermosEm() == null
                    && vazio(r.versaoTermosUso()) && vazio(r.versaoPoliticaPrivacidade());
        if (papel != PapelUsuario.CLIENTE && papel != PapelUsuario.ADMIN) return false;
        boolean comum = r.tipoPessoa() != null && !vazio(r.telefone())
                && Boolean.TRUE.equals(r.aceitouTermos()) && r.aceiteTermosEm() != null
                && !vazio(r.versaoTermosUso()) && !vazio(r.versaoPoliticaPrivacidade());
        return comum && (papel != PapelUsuario.CLIENTE || r.endereco() != null);
    }
    private static boolean vazio(String valor) { return valor == null || valor.isBlank(); }
}
