package repasse.phcauto.backend.infra.observability;

import io.sentry.Sentry;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SentryLifecycle implements InitializingBean, DisposableBean {
    private final String dsn;
    private final String environment;
    private final String release;

    public SentryLifecycle(@Value("${app.observability.sentry.dsn:}") String dsn,
            @Value("${app.observability.sentry.environment:development}") String environment,
            @Value("${app.observability.sentry.release:}") String release) {
        this.dsn = dsn;
        this.environment = environment;
        this.release = release;
    }

    @Override
    public void afterPropertiesSet() {
        if (dsn == null || dsn.isBlank()) return;
        Sentry.init(options -> {
            options.setDsn(dsn);
            options.setEnvironment(environment);
            if (release != null && !release.isBlank()) options.setRelease(release);
            options.setSendDefaultPii(false);
        });
    }

    @Override
    public void destroy() {
        if (Sentry.isEnabled()) Sentry.close();
    }
}
