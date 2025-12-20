package com.ondra.contenidos.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuración central de Spring Security para el microservicio de contenidos.
 *
 * <p>Define reglas de autorización y la cadena de filtros de seguridad.
 * Distingue entre endpoints públicos de consulta y protegidos de escritura.</p>
 *
 * <p>Orden de filtros aplicados:</p>
 * <ol>
 *   <li>ServiceTokenFilter - Autenticación entre microservicios</li>
 *   <li>JwtAuthenticationFilter - Autenticación de usuarios</li>
 *   <li>UsernamePasswordAuthenticationFilter - Filtro estándar de Spring</li>
 * </ol>
 */
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ServiceTokenFilter serviceTokenFilter;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * Configura CORS para permitir peticiones desde el frontend.
     *
     * @return fuente de configuración CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200", frontendUrl));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        log.info("🌐 CORS configurado para: http://localhost:4200, {}", frontendUrl);

        return source;
    }

    /**
     * Configura la cadena de filtros de seguridad y las reglas de autorización.
     *
     * <p>Reglas de acceso por recurso:</p>
     * <ul>
     *   <li>Géneros: GET públicos</li>
     *   <li>Multimedia: Todos protegidos</li>
     *   <li>Canciones y Álbumes: GET públicos, resto protegidos</li>
     *   <li>Favoritos, Compras, Carrito: Todos protegidos</li>
     *   <li>Comentarios: GET públicos excepto /mis-comentarios</li>
     *   <li>Valoraciones: GET públicos, resto protegidos</li>
     * </ul>
     *
     * @param http configurador de seguridad HTTP
     * @return cadena de filtros configurada
     * @throws Exception si ocurre error en la configuración
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/generos/**").permitAll()
                        .requestMatchers("/api/multimedia/**").authenticated()

                        .requestMatchers(HttpMethod.POST, "/api/canciones/*/reproducir").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/canciones/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/canciones/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/canciones/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/canciones/**").authenticated()

                        .requestMatchers(HttpMethod.GET, "/api/albumes/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/albumes/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/albumes/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/albumes/**").authenticated()

                        .requestMatchers("/api/cobros/**").authenticated()

                        .requestMatchers("/api/favoritos/**").authenticated()
                        .requestMatchers("/api/compras/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/comentarios/mis-comentarios").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/comentarios/**").permitAll()
                        .requestMatchers("/api/comentarios/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/valoraciones/**").permitAll()
                        .requestMatchers("/api/valoraciones/**").authenticated()
                        .requestMatchers("/api/carrito/**").authenticated()
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )

                .addFilterBefore(serviceTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("🔒 SecurityFilterChain configurado correctamente");
        return http.build();
    }
}