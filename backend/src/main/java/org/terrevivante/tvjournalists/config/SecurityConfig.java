package org.terrevivante.tvjournalists.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
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
                .requestMatchers("/api/login", "/api/logout", "/api/v1/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            // After login, always redirect to the SPA root (never to the saved API request URL)
            .formLogin(form -> form
                .loginPage("http://localhost:5173/login") // Tells Spring to redirect here when authentication is needed
                .loginProcessingUrl("/api/login")
                .defaultSuccessUrl("http://localhost:5173/", true)
                .failureUrl("http://localhost:5173/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/api/logout", "GET"))
                .logoutSuccessUrl("http://localhost:5173/login?logout")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .permitAll()
            )
            // This configures the API to accept JWT tokens
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails userDetails = User.builder()
                .username("user")
                .password(passwordEncoder().encode("password"))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(userDetails);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
