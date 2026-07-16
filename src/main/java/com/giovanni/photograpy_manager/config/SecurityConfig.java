package com.giovanni.photograpy_manager.config;

import com.giovanni.photograpy_manager.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Resources publiques
                .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/fonts/**").permitAll()
                // Console H2 (dev uniquement)
                .requestMatchers("/h2-console/**").permitAll()
                // Galerie Magic Link (publique)
                .requestMatchers("/gallery/**").permitAll()
                // Administration : ADMIN uniquement
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Facturation : ADMIN + ASSISTANT
                .requestMatchers("/billing/**").hasAnyRole("ADMIN", "ASSISTANT")
                // Comptabilité : ADMIN uniquement
                .requestMatchers("/accounting/**").hasRole("ADMIN")
                // Tout le reste : authentifié
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .usernameParameter("email")
                .passwordParameter("password")
                .permitAll()
            )
            .rememberMe(remember -> remember
                .userDetailsService(userDetailsService)
                .tokenValiditySeconds(30 * 24 * 60 * 60) // 30 jours
                .key("photoagence-remember-me-key")
                .rememberMeParameter("remember-me")
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            // H2 console needs frames
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            )
            // Disable CSRF for H2 console in dev
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
            );

        return http.build();
    }
}
