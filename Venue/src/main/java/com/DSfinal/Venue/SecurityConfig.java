package com.DSfinal.Venue;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/venue").permitAll()
                        .requestMatchers("/venue/halls/**").permitAll()
                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth -> oauth
                .jwt(jwt -> jwt
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
                );

        return http.build();
    }

    @Bean
        JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

                List<String> permissions =
                        jwt.getClaimAsStringList("permissions");

                // Fallback to Auth0's `scope` claim (space-separated string)
                if (permissions == null) {
                    String scope = jwt.getClaimAsString("scope");
                    if (scope != null && !scope.isBlank()) {
                        permissions = java.util.Arrays.asList(scope.split(" "));
                    }
                }

                if (permissions == null) {
                    return List.of();
                }

                return permissions.stream()
                        .map(p -> (org.springframework.security.core.GrantedAuthority)
                                new SimpleGrantedAuthority("SCOPE_" + p))
                        .collect(java.util.stream.Collectors.toList());
        });

        return converter;
        }
}