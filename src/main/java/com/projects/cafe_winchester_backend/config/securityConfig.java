package com.projects.cafe_winchester_backend.config;

import com.projects.cafe_winchester_backend.filter.JwtRequestFilter;
import com.projects.cafe_winchester_backend.security.JwtAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class securityConfig {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth ->
                auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/products").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/products/*").permitAll()

                        .requestMatchers("/api/products/update/*").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST,"/api/shop/orders/neworder").hasAnyRole("USER", "ADMIN")

                        .requestMatchers(HttpMethod.GET,"/api/shop/user/favourites").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST,"/api/shop/user/favourites/*").hasAnyRole("USER", "ADMIN")

                        .requestMatchers("/api/manage/data/**").hasRole("ADMIN")
        )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint)) // Use fallback custom entry point for auth errors if any exists after processing through all filters
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // Use stateless session policy for JWT

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class); // Add JWT filter before the standard auth filter

        http.csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Use BCrypt for password hashing // By default uses 10 rounds, can change by passing the number as parameter to the BCryptPasswordEncoder(12)
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager(); // Create AuthenticationManager instance
    }

}
