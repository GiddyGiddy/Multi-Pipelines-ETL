package com.ideaproject.etl.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.AbstractRequestMatcherRegistry;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static com.ideaproject.etl.controller.Routes.API_BASE_PATH;

/**
 * Configures and secure selected endpoints with Basic Auth
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    public static final String USER_ROLE = "USER";

    @Value("${dispatcher.api.username}")
    private String username;
    @Value("${dispatcher.api.password}")
    private String password;

    /**
     * Builds an In Memory User from provided configurations
     * @return the built user
     */
    @Bean
    public UserDetailsService users() {
        UserDetails userDetails = User.builder()
                .username(username)
                .password(passwordEncoder().encode(password))
                .roles(USER_ROLE)
                .build();
        return new InMemoryUserDetailsManager(userDetails);
    }

    /**
     * Bean for the Security Filter Chain, it specifies what endpoints to secure alongside other
     * configurations
     * @param http {@link HttpSecurity]
     * @return the configured security filter chain
     * @throws Exception The exception thrown
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatchers(AbstractRequestMatcherRegistry::anyRequest
                )
                .authorizeHttpRequests(authorize -> authorize
                        // Authentication for dispatcher API in general
                        .requestMatchers(API_BASE_PATH + "/**").hasRole(USER_ROLE)
                        // No authentication but https for all other endpoints
                        .anyRequest().permitAll()
                )
                // Disable csrf protection (Cross-Site Request Forgery)
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    /**
     * Bean to encode the password
     * @return BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

