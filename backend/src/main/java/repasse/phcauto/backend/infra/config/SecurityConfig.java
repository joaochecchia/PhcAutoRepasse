package repasse.phcauto.backend.infra.config;

import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import repasse.phcauto.backend.usuarios.internal.infrastructure.security.AuthCookieService;
import repasse.phcauto.backend.infra.security.ratelimit.RateLimitFilter;
import repasse.phcauto.backend.infra.security.csrf.CsrfCookieFilter;

@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
@org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
public class SecurityConfig {
    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:http://localhost:5173}") String origins) {
        List<String> allowedOrigins = Arrays.stream(origins.split(","))
                .map(String::trim).filter(origin -> !origin.isEmpty()).toList();
        if (allowedOrigins.isEmpty() || allowedOrigins.stream().anyMatch(origin -> origin.contains("*"))) {
            throw new IllegalArgumentException("CORS exige origens explícitas do frontend, sem wildcard");
        }
        var configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-XSRF-TOKEN"));
        configuration.setAllowCredentials(true);
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean BearerTokenResolver bearerTokenResolver() {
        var headerResolver = new org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver();
        return request -> {
            if (request.getCookies() != null) {
                for (var cookie : request.getCookies()) {
                    if (AuthCookieService.COOKIE_NAME.equals(cookie.getName()) && !cookie.getValue().isBlank()) {
                        return cookie.getValue();
                    }
                }
            }
            return headerResolver.resolve(request);
        };
    }

    @Bean SecurityFilterChain apiSecurity(HttpSecurity http, BearerTokenResolver bearerTokenResolver,
            RateLimitFilter rateLimitFilter,
            @Value("${AUTH_COOKIE_SECURE:false}") boolean secureCookies,
            @Value("${app.security.production:false}") boolean production) throws Exception {
        if (production && !secureCookies) {
            throw new IllegalStateException("Produção exige AUTH_COOKIE_SECURE=true");
        }
        var authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("ROLE_");
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        var csrfRepository = org.springframework.security.web.csrf.CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfRepository.setCookieCustomizer(cookie -> cookie.path("/").sameSite("Lax").secure(secureCookies));
        var csrfHandler = new org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler();
        var csrfParaNavegador = (org.springframework.security.web.util.matcher.RequestMatcher) request -> {
            String authorization = request.getHeader(org.springframework.http.HttpHeaders.AUTHORIZATION);
            boolean bearer = authorization != null && authorization.regionMatches(true, 0, "Bearer ", 0, 7);
            return !bearer && org.springframework.security.web.csrf.CsrfFilter.DEFAULT_CSRF_MATCHER.matches(request);
        };
        return http.cors(org.springframework.security.config.Customizer.withDefaults())
                .csrf(c -> c.csrfTokenRepository(csrfRepository)
                        .csrfTokenRequestHandler(csrfHandler)
                        .requireCsrfProtectionMatcher(csrfParaNavegador))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(c -> c.disable())
                .formLogin(c -> c.disable()).httpBasic(c -> c.disable()).logout(c -> c.disable())
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/error", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info", "/actuator/prometheus").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/registrar", "/api/v1/usuarios/login", "/api/v1/usuarios/refresh", "/api/v1/usuarios/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/usuarios/sessao").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios").denyAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/localizacao/municipios/coordenadas").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/compliance/documentos-vigentes").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/lojistas", "/api/v1/perfis/*", "/api/v1/perfis/*/foto").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/anuncios").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/anuncios/pagina-inicial").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/anuncios/opcoes-cadastro").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/anuncios/*/fotos",
                                "/api/v1/anuncios/*/fotos/*/arquivo").permitAll()
                        .requestMatchers("/api/v1/assinaturas/planos", "/api/v1/assinaturas/planos/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/usuarios/**", "/api/v1/anuncios", "/api/v1/anuncios/**").authenticated()
                        .anyRequest().denyAll())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((request, response, error) -> {
                            response.setStatus(401);
                            response.setHeader("WWW-Authenticate", "Bearer");
                            response.setCharacterEncoding("UTF-8");
                            response.setContentType("application/problem+json");
                            response.getWriter().write("{\"status\":401,\"title\":\"Não autenticado\",\"detail\":\"Token Bearer ausente ou inválido\"}");
                        })
                        .accessDeniedHandler((request, response, error) -> {
                            response.setStatus(403);
                            response.setCharacterEncoding("UTF-8");
                            response.setContentType("application/problem+json");
                            boolean csrf = error instanceof org.springframework.security.web.csrf.MissingCsrfTokenException
                                    || error instanceof org.springframework.security.web.csrf.InvalidCsrfTokenException;
                            response.getWriter().write(csrf
                                    ? "{\"status\":403,\"title\":\"Token CSRF inválido\",\"detail\":\"Atualize a página e tente novamente\"}"
                                    : "{\"status\":403,\"title\":\"Acesso negado\"}");
                        }))
                .oauth2ResourceServer(o -> o.bearerTokenResolver(bearerTokenResolver)
                        .jwt(j -> j.jwtAuthenticationConverter(converter)))
                .addFilterAfter(rateLimitFilter,
                        org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter.class)
                .addFilterAfter(new CsrfCookieFilter(), org.springframework.security.web.csrf.CsrfFilter.class)
                .build();
    }

    @Bean
    org.springframework.boot.web.servlet.FilterRegistrationBean<RateLimitFilter> disableRateLimitServletRegistration(
            RateLimitFilter filter) {
        var registration = new org.springframework.boot.web.servlet.FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
