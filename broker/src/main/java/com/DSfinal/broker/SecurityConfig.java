package com.DSfinal.broker;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration  //tells spring that this class contains setup beans, not business logic
@EnableWebSecurity
public class SecurityConfig {

    @Bean //securityfliterchain is a chain of filters that every http request passes through before reaching the controller.
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                //any url starting with /manager/ requires the user to the logged in and have the manager role
                //every other url is open to public
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/manager/**").hasRole("MANAGER")  // locked
                        .anyRequest().permitAll()                            // everything else is public
                )
                .formLogin(form -> form
                        .loginPage("/manager/login")         // your custom login page
                        .loginProcessingUrl("/manager/login") // Spring handles the POST here
                        .defaultSuccessUrl("/manager/orders", true)
                        .permitAll() //the login page itself is accessbiel to everyone, otherwise nobody can log in lol
                )
                .logout(logout -> logout
                        .logoutUrl("/manager/logout")
                        .logoutSuccessUrl("/")
                );

        return http.build();
    }


    //inmemoryuserdetailsmanager stores users in memory(no database needed)
    // we define one user with username(manager), password(whatever), role(manager)
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails manager = User.builder()
                .username("manager")
                .password(encoder.encode("password123"))  // change this password
                //encoder.encode hashes the password using BCrypt
                .roles("MANAGER")
                .build();

        return new InMemoryUserDetailsManager(manager);
    }


    // registering BCrypt as the hasing algorithm
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}