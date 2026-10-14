package repasse.phcauto.backend.infra.security.ratelimit;

public class RateLimitExceededException extends RuntimeException {
    private final long retryAfterSeconds;
    public RateLimitExceededException(long retryAfterSeconds) {
        super("Muitas requisições. Tente novamente mais tarde");
        this.retryAfterSeconds = retryAfterSeconds;
    }
    public long retryAfterSeconds() { return retryAfterSeconds; }
}
