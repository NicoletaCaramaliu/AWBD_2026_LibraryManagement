package org.example.librarymanagement.service;

import lombok.RequiredArgsConstructor;
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
public class UserService {

    private final UserRepository userRepository;

    public User create(User user) {

        userRepository.findByUsername(user.getUsername())
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Username '" +
                                    user.getUsername() +
                                    "' already exists"
                    );
                });

        userRepository.findByEmail(user.getEmail())
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Email '" +
                                    user.getEmail() +
                                    "' already exists"
                    );
                });

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User with id " + id + " was not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User update(Long id, User updatedUser) {

        User existing = getById(id);

        userRepository.findByUsername(updatedUser.getUsername())
                .filter(user -> !user.getId().equals(id))
                .ifPresent(user -> {
                    throw new DuplicateResourceException(
                            "Another user already uses this username"
                    );
                });

        userRepository.findByEmail(updatedUser.getEmail())
                .filter(user -> !user.getId().equals(id))
                .ifPresent(user -> {
                    throw new DuplicateResourceException(
                            "Another user already uses this email"
                    );
                });

        existing.setUsername(updatedUser.getUsername());
        existing.setEmail(updatedUser.getEmail());
        existing.setPassword(updatedUser.getPassword());
        existing.setRole(updatedUser.getRole());

        return userRepository.save(existing);
    }

    public void delete(Long id) {

        User user = getById(id);

        if (!user.getLoans().isEmpty()) {
            throw new InvalidOperationException(
                    "User cannot be deleted because they have loan history"
            );
        }

        userRepository.delete(user);
    }
}