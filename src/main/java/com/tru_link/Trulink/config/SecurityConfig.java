package com.tru_link.Trulink.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)throws Exception{
        http.
                csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth ->auth
                        .requestMatchers("/health", "/info", "/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/{shortKey:[A-Za-z0-9]{6}}")
                        .permitAll()
                        .requestMatchers(HttpMethod.HEAD, "/{shortKey:[A-Za-z0-9]{6}}")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/create")
                        .permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->{} ));
        return http.build();
    }
}
