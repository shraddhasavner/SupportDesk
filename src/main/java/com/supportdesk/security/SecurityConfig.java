package com.supportdesk.security;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * SecurityConfig: Central Spring Security configuration.
 * 
 * [English]:
 * - Stateless session policy (no HTTP sessions created; relies on JWT).
 * - Disables CSRF (REST APIs are stateless).
 * - Configures fine-grained role-based URL authorization rules:
 *   * POST /api/auth/login: Public (permitAll)
 *   * Employee management: ADMIN only
 *   * Dashboard: ADMIN and TECHNICIAN only
 *   * Ticket technician assignment: ADMIN only
 *   * Ticket status updates & resolution: ADMIN and TECHNICIAN
 *   * All other endpoints: Requires authenticated user
 * - Wires custom 401 Unauthorized and 403 Forbidden handlers.
 * - Adds JwtAuthenticationFilter before UsernamePasswordAuthenticationFilter.
 * - Configures environment-aware CORS with strict trusted origin enforcement in production.
 * 
 * [Hinglish]:
 * - Spring Security ka main configuration class.
 * - Kaunsa endpoint kaunsa role access kar sakta hai yeh yahan decide hota hai.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final List<String> allowedOrigins;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          CustomAuthenticationEntryPoint authenticationEntryPoint,
                          CustomAccessDeniedHandler accessDeniedHandler,
                          @Value("${app.cors.allowed-origins:}") List<String> allowedOrigins) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Enable CORS with dedicated CorsConfigurationSource
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Disable CSRF because REST APIs are stateless and authenticated with JWT
                .csrf(csrf -> csrf.disable())
                // Stateless session management
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Custom 401 and 403 error handlers
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                // URL authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Allow CORS preflight requests without requiring credentials
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Static web resources (HTML, CSS, JS, favicon)
                        .requestMatchers("/", "/index.html", "/login.html", "/css/**", "/js/**", "/favicon.ico").permitAll()

                        // Public auth endpoint
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

                        // Employee management: only ADMIN can create, delete, or list all employees
                        .requestMatchers(HttpMethod.POST, "/api/employees").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/employees/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/employees").hasRole("ADMIN")

                        // Dashboard & reports: only ADMIN and TECHNICIAN
                        .requestMatchers("/api/dashboard/**").hasAnyRole("ADMIN", "TECHNICIAN")

                        // Technician assignment: only ADMIN
                        .requestMatchers(HttpMethod.PATCH, "/api/tickets/*/assign/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/tickets/*/assign").hasRole("ADMIN")

                        // Overdue tickets & lifecycle status: ADMIN and TECHNICIAN
                        .requestMatchers(HttpMethod.GET, "/api/tickets/overdue").hasAnyRole("ADMIN", "TECHNICIAN")
                        .requestMatchers(HttpMethod.PATCH, "/api/tickets/*/status").hasAnyRole("ADMIN", "TECHNICIAN")
                        .requestMatchers(HttpMethod.PATCH, "/api/tickets/*/resolve").hasAnyRole("ADMIN", "TECHNICIAN")
                        .requestMatchers(HttpMethod.PATCH, "/api/tickets/*/resolution-notes").hasAnyRole("ADMIN", "TECHNICIAN")

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                // Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Builds the CorsConfigurationSource bean.
     * Enforces explicitly configured trusted origins. Wildcard origins with credentials are strictly prohibited.
     * If no origins are configured, all cross-origin requests are blocked.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        List<String> validOrigins = allowedOrigins != null
                ? allowedOrigins.stream()
                        .map(String::trim)
                        .filter(origin -> !origin.isEmpty() && !origin.equals("*"))
                        .toList()
                : List.of();

        if (!validOrigins.isEmpty()) {
            configuration.setAllowedOrigins(validOrigins);
            configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
            configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With", "Origin"));
            configuration.setExposedHeaders(List.of("Authorization"));
            configuration.setAllowCredentials(true);
            configuration.setMaxAge(3600L);
        } else {
            // When no origins are configured, strictly disallow cross-origin requests
            configuration.setAllowedOrigins(List.of());
            configuration.setAllowedMethods(List.of());
            configuration.setAllowedHeaders(List.of());
            configuration.setAllowCredentials(false);
        }

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
