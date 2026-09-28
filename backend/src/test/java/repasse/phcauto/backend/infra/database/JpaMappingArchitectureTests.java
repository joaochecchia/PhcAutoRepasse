package repasse.phcauto.backend.infra.database;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

class JpaMappingArchitectureTests {
    @Test void relacionamentosSaoLazyESemCascadeDePersistencia() throws Exception {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
        var entidades = scanner.findCandidateComponents("repasse.phcauto.backend");
        var eager = new ArrayList<String>();
        var cascades = new ArrayList<String>();

        for (var bean : entidades) {
            Class<?> tipo = Class.forName(bean.getBeanClassName());
            for (Field campo : tipo.getDeclaredFields()) {
                verificar(campo, campo.getAnnotation(OneToOne.class), eager, cascades);
                verificar(campo, campo.getAnnotation(OneToMany.class), eager, cascades);
                verificar(campo, campo.getAnnotation(ManyToOne.class), eager, cascades);
                verificar(campo, campo.getAnnotation(ManyToMany.class), eager, cascades);
                var elementos = campo.getAnnotation(ElementCollection.class);
                if (elementos != null && elementos.fetch() != FetchType.LAZY) eager.add(nome(campo));
            }
        }
        assertThat(entidades).as("entidades JPA encontradas").isNotEmpty();
        assertThat(eager).as("relacionamentos EAGER").isEmpty();
        assertThat(cascades).as("cascade ALL/PERSIST").isEmpty();
    }

    private void verificar(Field campo, Annotation relacao, List<String> eager, List<String> cascades) {
        if (relacao == null) return;
        FetchType fetch;
        CascadeType[] cascade;
        if (relacao instanceof OneToOne a) { fetch=a.fetch(); cascade=a.cascade(); }
        else if (relacao instanceof OneToMany a) { fetch=a.fetch(); cascade=a.cascade(); }
        else if (relacao instanceof ManyToOne a) { fetch=a.fetch(); cascade=a.cascade(); }
        else { var a=(ManyToMany)relacao; fetch=a.fetch(); cascade=a.cascade(); }
        if (fetch != FetchType.LAZY) eager.add(nome(campo));
        if (Arrays.stream(cascade).anyMatch(c -> c == CascadeType.ALL || c == CascadeType.PERSIST))
            cascades.add(nome(campo));
    }

    private String nome(Field campo) { return campo.getDeclaringClass().getSimpleName()+"."+campo.getName(); }
}
