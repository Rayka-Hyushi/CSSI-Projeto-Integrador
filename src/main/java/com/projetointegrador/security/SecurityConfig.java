package com.projetointegrador.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain customSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/cadastro", "/css/**", "/js/**", "/h2-console/**", "/uploads/**")
                        .permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/cliente/**").hasRole("CLIENTE")
                        .requestMatchers("/prestador/**").hasRole("PRESTADOR")
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)) // Necessário para o H2 renderizar no navegador;
                .formLogin(form -> form
                        .loginPage("/")
                        .loginProcessingUrl("/login")
                        .successHandler(customSuccessHandler())
                        .failureHandler(customFailureHandler())
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return (request, response, authentication) -> {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
            boolean isCliente = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_CLIENTE"));
            boolean isPrestador = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_PRESTADOR"));

            if (isAdmin) {
                response.sendRedirect("/admin/solicitacoes");
            } else if (isCliente) {
                response.sendRedirect("/cliente/inicio");
            } else if (isPrestador) {
                response.sendRedirect("/prestador/inicio");
            } else {
                response.sendRedirect("/");
            }
        };
    }

    @Bean
    public AuthenticationFailureHandler customFailureHandler() {
        return (request, response, exception) -> {
            if (exception instanceof DisabledException) {
                response.sendRedirect("/?erro=nao-aprovado");
            } else {
                response.sendRedirect("/?erro=login");
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
