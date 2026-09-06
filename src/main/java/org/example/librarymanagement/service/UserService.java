package org.example.librarymanagement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.librarymanagement.entity.User;
import org.example.librarymanagement.exception.DuplicateResourceException;
import org.example.librarymanagement.exception.InvalidOperationException;
import org.example.librarymanagement.exception.ResourceNotFoundException;
import org.example.librarymanagement.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public User create(User user) {

        log.debug(
                "Creating user with username={}",
                user.getUsername()
        );

        userRepository.findByUsername(user.getUsername())
                .ifPresent(existing -> {

                    log.error(
                            "Cannot create user. Username '{}' already exists",
                            user.getUsername()
                    );

                    throw new DuplicateResourceException(
                            "Username '" +
                                    user.getUsername() +
                                    "' already exists"
                    );
                });

        userRepository.findByEmail(user.getEmail())
                .ifPresent(existing -> {

                    log.error(
                            "Cannot create user. Email '{}' already exists",
                            user.getEmail()
                    );

                    throw new DuplicateResourceException(
                            "Email '" +
                                    user.getEmail() +
                                    "' already exists"
                    );
                });

        User savedUser = userRepository.save(user);

        log.info(
                "User created successfully. id={}, username={}",
                savedUser.getId(),
                savedUser.getUsername()
        );

        return savedUser;
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {

        log.debug("Searching for user with id={}", id);

        return userRepository.findById(id)
                .orElseThrow(() -> {

                    log.error(
                            "User with id {} was not found",
                            id
                    );

                    return new ResourceNotFoundException(
                            "User with id " + id + " was not found"
                    );
                });
    }

    @Transactional(readOnly = true)
    public List<User> getAll() {

        log.debug("Retrieving all users");

        return userRepository.findAll();
    }

    public User update(Long id, User updatedUser) {

        log.debug("Updating user with id={}", id);

        User existing = getById(id);

        userRepository.findByUsername(updatedUser.getUsername())
                .filter(user -> !user.getId().equals(id))
                .ifPresent(user -> {

                    log.error(
                            "Cannot update user with id {}. Username '{}' is already used",
                            id,
                            updatedUser.getUsername()
                    );

                    throw new DuplicateResourceException(
                            "Another user already uses this username"
                    );
                });

        userRepository.findByEmail(updatedUser.getEmail())
                .filter(user -> !user.getId().equals(id))
                .ifPresent(user -> {

                    log.error(
                            "Cannot update user with id {}. Email '{}' is already used",
                            id,
                            updatedUser.getEmail()
                    );

                    throw new DuplicateResourceException(
                            "Another user already uses this email"
                    );
                });

        existing.setUsername(updatedUser.getUsername());
        existing.setEmail(updatedUser.getEmail());
        existing.setPassword(updatedUser.getPassword());
        existing.setRole(updatedUser.getRole());

        User savedUser = userRepository.save(existing);

        log.info(
                "User with id {} was updated successfully",
                id
        );

        return savedUser;
    }

    public void delete(Long id) {

        log.debug("Deleting user with id={}", id);

        User user = getById(id);

        if (!user.getLoans().isEmpty()) {

            log.error(
                    "Cannot delete user with id {} because they have loan history",
                    id
            );

            throw new InvalidOperationException(
                    "User cannot be deleted because they have loan history"
            );
        }

        userRepository.delete(user);

        log.info(
                "User with id {} was deleted successfully",
                id
        );
    }
}