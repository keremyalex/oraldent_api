package com.example.odontologia_api.config;

import com.example.odontologia_api.security.JwtAuthenticationFilter;
import com.example.odontologia_api.security.RestAuthenticationEntryPoint;
import com.example.odontologia_api.security.UsuarioDetailsService;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UsuarioDetailsService usuarioDetailsService;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            UsuarioDetailsService usuarioDetailsService,
            RestAuthenticationEntryPoint restAuthenticationEntryPoint
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.usuarioDetailsService = usuarioDetailsService;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(restAuthenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/error"
                        ).permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/register/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/servicios/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/citas").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/citas/disponibilidad").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/citas/*/cancelar").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/citas/*/reprogramar").permitAll()
                        .requestMatchers("/api/doctor/**", "/api/horarios/**").hasAnyRole("ADMIN", "DOCTOR", "RECEPCION")
                        .requestMatchers(HttpMethod.POST, "/api/servicios/**").hasAnyRole("ADMIN", "DOCTOR", "RECEPCION")
                        .requestMatchers(HttpMethod.PUT, "/api/servicios/**").hasAnyRole("ADMIN", "DOCTOR", "RECEPCION")
                        .requestMatchers(HttpMethod.DELETE, "/api/servicios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/pacientes/**").hasAnyRole("ADMIN", "DOCTOR", "RECEPCION")
                        .requestMatchers(HttpMethod.POST, "/api/pacientes/**").hasAnyRole("ADMIN", "DOCTOR", "RECEPCION")
                        .requestMatchers(HttpMethod.PUT, "/api/pacientes/**").hasAnyRole("ADMIN", "DOCTOR", "RECEPCION")
                        .requestMatchers(HttpMethod.DELETE, "/api/pacientes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/citas/**").hasAnyRole("ADMIN", "DOCTOR", "RECEPCION")
                        .requestMatchers("/api/odontogramas/**", "/api/pacientes/*/odontograma", "/api/pacientes/*/odontogramas").hasAnyRole("ADMIN", "DOCTOR", "RECEPCION")
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "http://127.0.0.1:4200",
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "https://oraldent.keremy.com"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
