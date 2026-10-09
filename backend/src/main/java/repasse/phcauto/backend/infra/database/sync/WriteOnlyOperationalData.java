package repasse.phcauto.backend.infra.database.sync;

import java.lang.annotation.*;

/** Dados deliberadamente não replicados para a projeção de consultas. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WriteOnlyOperationalData { }
