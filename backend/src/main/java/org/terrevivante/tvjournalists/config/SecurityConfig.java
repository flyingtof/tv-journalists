package org.terrevivante.tvjournalists.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String THEME_MANAGER_ROLE = "THEME_MANAGER";
    private static final String JOURNALIST_MANAGER_ROLE = "JOURNALIST_MANAGER";
    private static final String API_THEMES = "/api/v1/themes";
    private static final String API_JOURNALISTS = "/api/v1/journalists";

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/login", "/guide", "/admin/users", "/admin/themes").permitAll()
                .requestMatchers("/journalists/*").permitAll()
                .requestMatchers("/assets/**", "/favicon.ico", "/vite.svg", "/index.html").permitAll()
                .requestMatchers("/api/login", "/api/logout").permitAll()
                .requestMatchers("/api/v1/auth/me").permitAll()
                .requestMatchers("/api/v1/users/**").hasRole(ADMIN_ROLE)
                .requestMatchers(HttpMethod.POST, API_THEMES).hasAnyRole(ADMIN_ROLE, THEME_MANAGER_ROLE)
                .requestMatchers(HttpMethod.PUT, API_THEMES, API_THEMES+"/**").hasAnyRole(ADMIN_ROLE, THEME_MANAGER_ROLE)
                .requestMatchers(HttpMethod.DELETE, API_THEMES, API_THEMES+"/**").hasAnyRole(ADMIN_ROLE, THEME_MANAGER_ROLE)
                .requestMatchers(HttpMethod.POST, API_JOURNALISTS).hasAnyRole(ADMIN_ROLE, JOURNALIST_MANAGER_ROLE)
                .requestMatchers(HttpMethod.PUT, API_JOURNALISTS, API_JOURNALISTS + "/**").hasAnyRole(ADMIN_ROLE, JOURNALIST_MANAGER_ROLE)
                .requestMatchers(HttpMethod.DELETE, API_JOURNALISTS, API_JOURNALISTS + "/**").hasAnyRole(ADMIN_ROLE, JOURNALIST_MANAGER_ROLE)
                .requestMatchers("/api/v1/**").hasAnyRole("USER", ADMIN_ROLE, THEME_MANAGER_ROLE, JOURNALIST_MANAGER_ROLE)
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
                .logoutRequestMatcher(PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET,"/api/logout"))
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
