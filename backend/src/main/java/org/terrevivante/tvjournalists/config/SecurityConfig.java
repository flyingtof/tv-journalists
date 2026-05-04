package org.terrevivante.tvjournalists.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/login", "/guide", "/admin/users", "/admin/themes").permitAll()
                .requestMatchers("/journalists/*").permitAll()
                .requestMatchers("/assets/**", "/favicon.ico", "/vite.svg", "/index.html").permitAll()
                .requestMatchers("/api/login", "/api/logout").permitAll()
                .requestMatchers("/api/v1/auth/me").permitAll()
                .requestMatchers("/api/v1/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/themes").hasAnyRole("ADMIN", "THEME_MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/v1/themes", "/api/v1/themes/**").hasAnyRole("ADMIN", "THEME_MANAGER")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/themes", "/api/v1/themes/**").hasAnyRole("ADMIN", "THEME_MANAGER")
                .requestMatchers("/api/v1/**").hasAnyRole("USER", "ADMIN", "THEME_MANAGER")
                .anyRequest().authenticated()
            )
            // After login, always redirect to the SPA root (never to the saved API request URL)
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/api/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/api/logout", "GET"))
                .logoutSuccessUrl("/login?logout")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .permitAll()
            )
            // This configures the API to accept JWT tokens
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
