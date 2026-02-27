package com.example.demo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security Configuration for HackNation.
 *
 * Key decisions:
 * - Stateless sessions (JWT only — no HttpSession needed).
 * - CSRF disabled (safe for stateless REST + JWT — no cookies used for auth).
 * - CORS configured here only (L2 fix: removed per-controller @CrossOrigin).
 * - Swagger UI paths whitelisted so API docs are accessible.
 * - Actuator /health whitelisted for load-balancer liveness probes.
 * - All other requests must be authenticated.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ── Auth (public) ──────────────────────────────────────────────
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

                        // ── Public read endpoints ──────────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/stats").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/hackathons").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/hackathons/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/projects/leaderboard").permitAll()

                        // ── Swagger / OpenAPI UI (dev-only; lock down in production) ──
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/api-docs",
                                "/api-docs/**",
                                "/v3/api-docs/**")
                        .permitAll()

                        // ── Health check (load balancer probe) ────────────────────────
                        .requestMatchers("/actuator/health").permitAll()

                        // ── Role-specific endpoints ────────────────────────────────────
                        .requestMatchers("/api/dashboard/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/dashboard/organizer/**").hasAnyRole("ORGANIZER", "ADMIN")
                        .requestMatchers("/api/dashboard/participant/**").hasAnyRole("PARTICIPANT", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/hackathons").hasAnyRole("ORGANIZER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/hackathons/**").hasAnyRole("ORGANIZER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/hackathons/**").hasAnyRole("ORGANIZER", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/projects").hasAnyRole("PARTICIPANT", "ADMIN")

                        .requestMatchers("/api/evaluation/**").hasAnyRole("JUDGE", "ADMIN")

                        .requestMatchers("/api/users/all").hasRole("ADMIN")
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // ── All remaining requests require authentication ───────────────
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * BCrypt with strength 12 (good balance of security & speed on modern
     * hardware).
     * Strength 10 is the default; 12 adds ~4× the work factor.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Global CORS configuration — the single source of truth for CORS.
     * FIX (L2): Removed duplicated @CrossOrigin from every controller.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000", // Create React App
                "http://localhost:5173" // Vite / React
        ));
        configuration.setAllowedMethods(
                Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(
                Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
