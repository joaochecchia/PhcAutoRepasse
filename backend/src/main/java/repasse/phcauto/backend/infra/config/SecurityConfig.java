package repasse.phcauto.backend.infra.config;

import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import repasse.phcauto.backend.usuarios.internal.infrastructure.security.AuthCookieService;

@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
@org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
public class SecurityConfig {
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

    @Bean SecurityFilterChain apiSecurity(HttpSecurity http, BearerTokenResolver bearerTokenResolver) throws Exception {
        var authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("ROLE_");
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return http.csrf(c -> c.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(c -> c.disable())
                .formLogin(c -> c.disable()).httpBasic(c -> c.disable()).logout(c -> c.disable())
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/error", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/registrar", "/api/v1/usuarios/login", "/api/v1/usuarios/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/usuarios/sessao").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios").denyAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/localizacao/municipios/coordenadas").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/compliance/documentos-vigentes").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/anuncios").permitAll()
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
                            response.getWriter().write("{\"status\":403,\"title\":\"Acesso negado\"}");
                        }))
                .oauth2ResourceServer(o -> o.bearerTokenResolver(bearerTokenResolver)
                        .jwt(j -> j.jwtAuthenticationConverter(converter)))
                .build();
    }
}
