package org.example.librarymanagement.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final DataSource dataSource;

    @Bean
    public UserDetailsService userDetailsService() {

        JdbcUserDetailsManager userDetailsManager =
                new JdbcUserDetailsManager(dataSource);

        userDetailsManager.setUsersByUsernameQuery(
                """
                SELECT username, password, true
                FROM users
                WHERE username = ?
                """
        );

        userDetailsManager.setAuthoritiesByUsernameQuery(
                """
                SELECT username, CONCAT('ROLE_', role)
                FROM users
                WHERE username = ?
                """
        );

        return userDetailsManager;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .authorizeHttpRequests(auth -> auth

                        // Public
                        .requestMatchers(
                                "/login",
                                "/register",
                                "/css/**",
                                "/error"
                        ).permitAll()

                        // Home
                        .requestMatchers("/")
                        .hasAnyRole("USER", "ADMIN")

                        // Users - doar ADMIN
                        .requestMatchers("/users/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/users/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/test-500")
                        .hasRole("ADMIN")

                        // Catalog - vizualizare
                        .requestMatchers(
                                HttpMethod.GET,
                                "/books",
                                "/authors",
                                "/categories",
                                "/publishers",
                                "/book-details"
                        )
                        .hasAnyRole("USER", "ADMIN")

                        // Catalog - pagini de adaugare/editare
                        .requestMatchers(
                                "/books/new",
                                "/books/*/edit",
                                "/authors/new",
                                "/authors/*/edit",
                                "/categories/new",
                                "/categories/*/edit",
                                "/publishers/new",
                                "/publishers/*/edit",
                                "/book-details/new",
                                "/book-details/*/edit"
                        )
                        .hasRole("ADMIN")

                        // Catalog - modificari
                        .requestMatchers(
                                HttpMethod.POST,
                                "/books/**",
                                "/authors/**",
                                "/categories/**",
                                "/publishers/**",
                                "/book-details/**"
                        )
                        .hasRole("ADMIN")

                        // Loans Web
                        .requestMatchers("/loans/*/delete")
                        .hasRole("ADMIN")

                        .requestMatchers("/loans/**")
                        .hasAnyRole("USER", "ADMIN")

                        // REST catalog GET
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/books/**",
                                "/api/authors/**",
                                "/api/categories/**",
                                "/api/publishers/**",
                                "/api/book-details/**"
                        )
                        .hasAnyRole("USER", "ADMIN")

                        // REST catalog POST
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/books/**",
                                "/api/authors/**",
                                "/api/categories/**",
                                "/api/publishers/**",
                                "/api/book-details/**"
                        )
                        .hasRole("ADMIN")

                        // REST catalog PUT
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/books/**",
                                "/api/authors/**",
                                "/api/categories/**",
                                "/api/publishers/**",
                                "/api/book-details/**"
                        )
                        .hasRole("ADMIN")

                        // REST catalog DELETE
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/books/**",
                                "/api/authors/**",
                                "/api/categories/**",
                                "/api/publishers/**",
                                "/api/book-details/**"
                        )
                        .hasRole("ADMIN")

                        // REST loans delete - doar ADMIN
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/loans/**"
                        )
                        .hasRole("ADMIN")

                        // REST loans
                        .requestMatchers("/api/loans/**")
                        .hasAnyRole("USER", "ADMIN")

                        .anyRequest()
                        .authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )

                .rememberMe(remember -> remember
                        .key("library-management-key")
                        .tokenValiditySeconds(7 * 24 * 60 * 60)
                );

        return http.build();
    }
}