package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CadastroPorPapelValidator.class)
public @interface CadastroPorPapelValido {
    String message() default "Campos obrigatórios incompatíveis com o papel informado";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
