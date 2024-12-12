package com.team9.cinema.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // auth rules
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/home", "/register", "/movies", "/movies/search",
                                "/verify-email", "/verify-token", "/login", "/search",
                                "/resetpassword", "/verify-resetpassword",
                                "/verified-resetpassword", "/css/**", "/js/**",
                                "/movies/filter") // public access
                        .permitAll()
                        .requestMatchers("/delete-payment/**", "/add-payment/**", "/payment-methods/**", "/booking/**", "/history/**").authenticated() // authenticated access
                        .requestMatchers("/admin/**", "/admin/users/delete/**","/movies/add", "/movies/edit/**", "/movies/delete/**").hasRole("ADMIN")  // admin access
                        .anyRequest().authenticated()  // all other routes
                )
                // login
                .formLogin((form) -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/home", true)
                        .failureHandler(customAuthenticationFailureHandler())
                        .permitAll()
                )
                // logout
                .logout((logout) -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/home")
                        .permitAll()
                )
                // access denied
                .exceptionHandling((exceptions) -> exceptions
                        .accessDeniedPage("/403")
                )
                // session
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                // CSRF
                .csrf((csrf) -> csrf
                        .ignoringRequestMatchers("/delete-payment/**", "/add-payment/**", "/checkout", "/admin/users/delete/**", "/booking/**")
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        authProvider.setPostAuthenticationChecks(user -> {
            if (user.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_SUSPENDED"))) {
                throw new BadCredentialsException("Your account is suspended. Please contact teamninecinema@gmail.com for further details.");
            }
        });
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public AuthenticationFailureHandler customAuthenticationFailureHandler() {
        return (request, response, exception) -> {
            if (exception instanceof BadCredentialsException && exception.getMessage().contains("suspended")) {
                response.sendRedirect("/login?suspended=true"); // redirect to login with suspended error
            } else {
                response.sendRedirect("/login?error=true"); // redirect to login with normal error
            }
        };
    }
}
