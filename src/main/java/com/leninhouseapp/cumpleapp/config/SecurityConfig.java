package com.leninhouseapp.cumpleapp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * IMPORTANTE: este archivo es una REFERENCIA. Reemplaza los valores
 * (rutas, roles) según tu SecurityConfig real - no la tengo en el contexto.
 * Lo clave para arreglar el logout está en el bloque .logout(...).
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/tv").hasRole("TELEVISION")
                        .requestMatchers("/player/**", "/api/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/home", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        // Permite logout por GET (así funciona tu <a href="/logout"> o location.href)
                        .logoutRequestMatcher(
                                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/logout", "GET")
                        )
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                // Si usas WebSockets con SockJS, puede ser necesario relajar CSRF para /ws/**
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/ws/**")
                );

        return http.build();
    }
}