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
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth

                        // resurse publice
                        .requestMatchers(
                                "/css/**",
                                "/error"
                        ).permitAll()

                        // pagina home - USER sau ADMIN
                        .requestMatchers("/")
                        .hasAnyRole("USER", "ADMIN")

                        // doar ADMIN gestioneaza utilizatorii
                        .requestMatchers("/users/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/users/**")
                        .hasRole("ADMIN")

                        // endpoint pentru testarea erorii 500
                        .requestMatchers("/test-500")
                        .hasRole("ADMIN")

                        // CATALOG - WEB

                        // USER si ADMIN pot vedea listele
                        .requestMatchers(
                                HttpMethod.GET,
                                "/books",
                                "/authors",
                                "/categories",
                                "/publishers",
                                "/book-details"
                        )
                        .hasAnyRole("USER", "ADMIN")

                        // ADMIN poate intra pe formularele de creare/editare
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

                        // orice modificare WEB pentru catalog = ADMIN
                        .requestMatchers(
                                HttpMethod.POST,
                                "/books/**",
                                "/authors/**",
                                "/categories/**",
                                "/publishers/**",
                                "/book-details/**"
                        )
                        .hasRole("ADMIN")


                        // LOANS - WEB

                        // stergerea unui imprumut doar ADMIN
                        .requestMatchers(
                                "/loans/*/delete"
                        )
                        .hasRole("ADMIN")

                        // USER si ADMIN pot vedea/crea/returna imprumuturi
                        .requestMatchers("/loans/**")
                        .hasAnyRole("USER", "ADMIN")


                        // REST API - READ

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/books/**",
                                "/api/authors/**",
                                "/api/categories/**",
                                "/api/publishers/**",
                                "/api/book-details/**"
                        )
                        .hasAnyRole("USER", "ADMIN")


                        // REST API - WRITE


                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/books/**",
                                "/api/authors/**",
                                "/api/categories/**",
                                "/api/publishers/**",
                                "/api/book-details/**"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/books/**",
                                "/api/authors/**",
                                "/api/categories/**",
                                "/api/publishers/**",
                                "/api/book-details/**"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/books/**",
                                "/api/authors/**",
                                "/api/categories/**",
                                "/api/publishers/**",
                                "/api/book-details/**"
                        )
                        .hasRole("ADMIN")

                        // =========================
                        // REST LOANS
                        // =========================

                        // stergere imprumut doar ADMIN
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/loans/**"
                        )
                        .hasRole("ADMIN")

                        // restul operatiilor pe loans
                        .requestMatchers("/api/loans/**")
                        .hasAnyRole("USER", "ADMIN")

                        // orice alt endpoint necesita autentificare
                        .anyRequest()
                        .authenticated()
                )

                .formLogin(form -> form
                        .permitAll()
                )

                .logout(logout -> logout
                        .permitAll()
                )

                .rememberMe(remember -> remember
                        .key("library-management-key")
                        .tokenValiditySeconds(7 * 24 * 60 * 60)
                );

        return http.build();
    }
}