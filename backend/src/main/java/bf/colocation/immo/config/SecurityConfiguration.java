package bf.colocation.immo.config;

import static org.springframework.security.config.Customizer.withDefaults;

import bf.colocation.immo.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import tech.jhipster.config.JHipsterProperties;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    private final JHipsterProperties jHipsterProperties;

    public SecurityConfiguration(JHipsterProperties jHipsterProperties) {
        this.jHipsterProperties = jHipsterProperties;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http.cors(withDefaults())
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz ->
                // prettier-ignore
                authz
                    .requestMatchers(HttpMethod.POST, "/api/authenticate").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()
                    // Visiteur : consultation et recherche des annonces sans authentification (EF-10.5)
                    .requestMatchers(HttpMethod.GET, "/api/annonces/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/immobiliers/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/detail-colocations/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/localites/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/quartiers/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/type-immobiliers/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/equipements/**").permitAll()
                    // La réputation d'un démarcheur est publique : elle éclaire le choix du locataire.
                    .requestMatchers(HttpMethod.GET, "/api/demarcheurs/*/reputation").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/images/**").permitAll()
                    // Les photos d'annonces s'affichent dans le catalogue public.
                    .requestMatchers(HttpMethod.GET, "/api/files/images/**").permitAll()
                    // Illustrations de repli par type de bien, servies en statique :
                    // elles s'affichent dans le catalogue public, y compris hors connexion.
                    .requestMatchers(HttpMethod.GET, "/illustrations/**").permitAll()
                    // Fonds photographique du projet, affiché dans le catalogue public.
                    .requestMatchers(HttpMethod.GET, "/photos/**").permitAll()
                    // Les pièces justificatives (CNIB…) sont des données personnelles :
                    // authentification obligatoire, puis contrôle de propriété dans FileResource.
                    .requestMatchers("/api/files/**").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/annonces/*/vue").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/authenticate").permitAll()
                    .requestMatchers("/api/register").permitAll()
                    .requestMatchers("/api/activate").permitAll()
                    .requestMatchers("/api/account/reset-password/init").permitAll()
                    .requestMatchers("/api/account/reset-password/finish").permitAll()
                    .requestMatchers("/api/admin/**").hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers("/api/**").authenticated()
                    .requestMatchers("/v3/api-docs/**").permitAll()
                    .requestMatchers("/management/health").permitAll()
                    .requestMatchers("/management/health/**").permitAll()
                    .requestMatchers("/management/info").permitAll()
                    .requestMatchers("/management/prometheus").permitAll()
                    .requestMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN)
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions ->
                exceptions
                    .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                    .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));
        return http.build();
    }
}
