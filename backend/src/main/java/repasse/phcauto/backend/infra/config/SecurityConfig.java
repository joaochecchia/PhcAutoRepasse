package repasse.phcauto.backend.infra.config;

import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
@org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
public class SecurityConfig {
    @Bean SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
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
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/registrar", "/api/v1/usuarios/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios").denyAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/anuncios").permitAll()
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
                .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(converter)))
                .build();
    }
}
