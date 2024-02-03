package com.evalvis.post;

import com.evalvis.post.logging.HttpLoggingFilter;
import com.evalvis.security.JwtFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private HttpLoggingFilter loggingFilter;
    @Autowired
    private JwtFilter jwtFilter;
    @Value("${post.frontend-url}")
    private String frontendUrl;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .requiresChannel(channel -> channel.anyRequest().requiresSecure())
                .csrf(csrf -> csrf.disable()) // Implemented manually by using double submit token.
                .exceptionHandling(
                        exception -> exception.authenticationEntryPoint(
                                (request, response, authException) -> {
                                    logger.error("Unauthorized error: {}", authException.getMessage());
                                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized");
                                }
                        )
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        requests -> requests
                                .requestMatchers(HttpMethod.OPTIONS)
                                .permitAll()
                                .requestMatchers("/posts/create")
                                .authenticated()
                                .requestMatchers("/posts", "/posts/{id}")
                                .permitAll()
                                .anyRequest()
                                .authenticated()
                );
        http.addFilterBefore(loggingFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Collections.singletonList(frontendUrl));
        config.setAllowedMethods(List.of("GET", "POST", "OPTIONS", "PUT"));
        config.setAllowedHeaders(List.of("Origin", "Content-Type", "Accept", "Authorization", "X-CSRF-TOKEN"));
        config.setExposedHeaders(Collections.singletonList("IS-OWNER"));
        config.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
