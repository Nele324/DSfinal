package com.DSfinal.broker;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import java.util.Set;
import java.util.HashSet;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import java.util.Map;
import java.util.HashMap;
import jakarta.servlet.http.HttpServletRequest;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity

public class SecurityConfig {


   @Bean
   
        SecurityFilterChain filterChain(
                HttpSecurity http,
                OAuth2AuthorizationRequestResolver authorizationRequestResolver) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/js/**", "/css/**", "/images/**", "/error")
                .permitAll()

                // Manager pages
                .requestMatchers("/manager/**")
                .hasRole("MANAGER")

                // Order creation page
                .requestMatchers("/broker/review-order")
                .authenticated()

                // Everything else
                .anyRequest()
                .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(endpoint ->
                        endpoint.authorizationRequestResolver(
                        authorizationRequestResolver
                        )
                )
                .userInfoEndpoint(userInfo ->
                        userInfo.oidcUserService(oidcUserService())
                )
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/").permitAll()
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
        }

        @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {

        OidcUserService delegate = new OidcUserService();

        return userRequest -> {

            OidcUser user = delegate.loadUser(userRequest);

            Set<GrantedAuthority> authorities =
                    new HashSet<>(user.getAuthorities());

            String email = user.getEmail();

            if ("manager@gmail.com".equals(email)) {
                authorities.add(
                        new SimpleGrantedAuthority("ROLE_MANAGER"));
            }

            return new DefaultOidcUser(
                    authorities,
                    user.getIdToken(),
                    user.getUserInfo());
        };
    }

    @Bean
        public OAuth2AuthorizationRequestResolver customAuthorizationRequestResolver(
                ClientRegistrationRepository repo) {

        DefaultOAuth2AuthorizationRequestResolver resolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        repo,
                        "/oauth2/authorization");

        return new OAuth2AuthorizationRequestResolver() {
                @Override
                public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                        OAuth2AuthorizationRequest authRequest = resolver.resolve(request);
                        return customize(authRequest);
                }

                @Override
                public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
                        OAuth2AuthorizationRequest authRequest = resolver.resolve(request, clientRegistrationId);
                        return customize(authRequest);
                }

                private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest authRequest) {
                        if (authRequest == null) {
                                return null;
                        }

                        Map<String, Object> params =
                                new HashMap<>(authRequest.getAdditionalParameters());

                        params.put("prompt", "login");

                        return OAuth2AuthorizationRequest
                                .from(authRequest)
                                .additionalParameters(params)
                                .build();
                }
        };
        }
}