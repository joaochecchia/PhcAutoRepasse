package repasse.phcauto.backend.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.*;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;

@Configuration(proxyBeanMethods = false)
public class SocialOAuthConfiguration {
    @Bean
    @ConditionalOnProperty(name = "app.security.oauth2.enabled", havingValue = "true")
    ClientRegistrationRepository socialClientRegistrationRepository(
            @Value("${app.security.oauth2.google.client-id:}") String googleId,
            @Value("${app.security.oauth2.google.client-secret:}") String googleSecret) {
        if (googleId.isBlank() || googleSecret.isBlank()) {
            throw new IllegalStateException("OAuth Google habilitado exige GOOGLE_CLIENT_ID e GOOGLE_CLIENT_SECRET");
        }
        return new InMemoryClientRegistrationRepository(
                CommonOAuth2Provider.GOOGLE.getBuilder("google").clientId(googleId).clientSecret(googleSecret)
                        .scope("openid", "profile", "email").build());
    }
}
