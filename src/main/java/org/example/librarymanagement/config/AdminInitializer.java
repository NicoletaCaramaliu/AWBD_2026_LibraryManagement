package org.example.librarymanagement.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.librarymanagement.entity.Role;
import org.example.librarymanagement.entity.User;
import org.example.librarymanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev","prodgit pul"})
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Override
    public void run(String... args) {

        if (userRepository.findByUsername("admin").isPresent()) {
            log.debug("Admin user already exists");
            return;
        }

        if (adminPassword == null || adminPassword.isBlank()) {
            log.warn(
                    "Admin user was not created because ADMIN_PASSWORD is not configured"
            );
            return;
        }

        User admin = User.builder()
                .username("admin")
                .email("admin@library.com")
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .build();

        userRepository.save(admin);

        log.info("Initial admin user created successfully");
    }
}