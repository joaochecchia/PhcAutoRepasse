package repasse.phcauto.backend.anuncios.internal.core;

public interface CriarAnuncioUseCase {
    AnuncioCriadoResultado execute(CriarAnuncioCommand command);
}
