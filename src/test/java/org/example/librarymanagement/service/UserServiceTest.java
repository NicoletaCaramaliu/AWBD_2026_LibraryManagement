package org.example.librarymanagement.service;

import org.example.librarymanagement.entity.Loan;
import org.example.librarymanagement.entity.Role;
import org.example.librarymanagement.entity.User;
import org.example.librarymanagement.exception.DuplicateResourceException;
import org.example.librarymanagement.exception.InvalidOperationException;
import org.example.librarymanagement.exception.ResourceNotFoundException;
import org.example.librarymanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("john")
                .email("john@example.com")
                .password("password123")
                .role(Role.USER)
                .loans(new ArrayList<>())
                .build();
    }

    @Test
    void create_shouldSaveUser_whenUsernameAndEmailAreUnique() {

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        when(userRepository.save(user))
                .thenReturn(user);

        User result = userService.create(user);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("john", result.getUsername());
        assertEquals("john@example.com", result.getEmail());
        assertEquals("encodedPassword", result.getPassword());
        assertEquals(Role.USER, result.getRole());

        verify(userRepository).findByUsername("john");
        verify(userRepository).findByEmail("john@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(user);
    }

    @Test
    void create_shouldThrowException_whenUsernameAlreadyExists() {

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> userService.create(user)
        );

        assertEquals(
                "Username 'john' already exists",
                exception.getMessage()
        );

        verify(userRepository, never())
                .findByEmail(anyString());

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void create_shouldThrowException_whenEmailAlreadyExists() {

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> userService.create(user)
        );

        assertEquals(
                "Email 'john@example.com' already exists",
                exception.getMessage()
        );

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void getById_shouldReturnUser_whenUserExists() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        User result = userService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("john", result.getUsername());
        assertEquals("john@example.com", result.getEmail());

        verify(userRepository).findById(1L);
    }

    @Test
    void getById_shouldThrowException_whenUserDoesNotExist() {

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getById(99L)
        );

        assertEquals(
                "User with id 99 was not found",
                exception.getMessage()
        );

        verify(userRepository).findById(99L);
    }

    @Test
    void getAll_shouldReturnAllUsers() {

        User secondUser = User.builder()
                .id(2L)
                .username("admin")
                .email("admin@example.com")
                .password("admin123")
                .role(Role.ADMIN)
                .loans(new ArrayList<>())
                .build();

        when(userRepository.findAll())
                .thenReturn(List.of(user, secondUser));

        List<User> result = userService.getAll();

        assertEquals(2, result.size());
        assertEquals("john", result.get(0).getUsername());
        assertEquals("admin", result.get(1).getUsername());

        verify(userRepository).findAll();
    }

    @Test
    void update_shouldUpdateUser_whenDataIsValid() {

        User updatedUser = User.builder()
                .username("john_updated")
                .email("john.updated@example.com")
                .password("newPassword")
                .role(Role.ADMIN)
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.findByUsername("john_updated"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("john.updated@example.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("newPassword"))
                .thenReturn("encodedNewPassword");

        when(userRepository.save(user))
                .thenReturn(user);

        User result = userService.update(1L, updatedUser);

        assertEquals("john_updated", result.getUsername());
        assertEquals("john.updated@example.com", result.getEmail());
        assertEquals("encodedNewPassword", result.getPassword());
        assertEquals(Role.ADMIN, result.getRole());

        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(user);
    }

    @Test
    void update_shouldThrowException_whenAnotherUserUsesSameUsername() {

        User updatedUser = User.builder()
                .username("existingUser")
                .email("john@example.com")
                .password("password")
                .role(Role.USER)
                .build();

        User anotherUser = User.builder()
                .id(2L)
                .username("existingUser")
                .email("other@example.com")
                .password("password")
                .role(Role.USER)
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.findByUsername("existingUser"))
                .thenReturn(Optional.of(anotherUser));

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> userService.update(1L, updatedUser)
        );

        assertEquals(
                "Another user already uses this username",
                exception.getMessage()
        );

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void update_shouldThrowException_whenAnotherUserUsesSameEmail() {

        User updatedUser = User.builder()
                .username("john")
                .email("existing@example.com")
                .password("password")
                .role(Role.USER)
                .build();

        User anotherUser = User.builder()
                .id(2L)
                .username("another")
                .email("existing@example.com")
                .password("password")
                .role(Role.USER)
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(userRepository.findByEmail("existing@example.com"))
                .thenReturn(Optional.of(anotherUser));

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> userService.update(1L, updatedUser)
        );

        assertEquals(
                "Another user already uses this email",
                exception.getMessage()
        );

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void updateWithoutPassword_shouldKeepExistingPassword() {

        User updatedUser = User.builder()
                .username("john_updated")
                .email("john.updated@example.com")
                .role(Role.ADMIN)
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.findByUsername("john_updated"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("john.updated@example.com"))
                .thenReturn(Optional.empty());

        when(userRepository.save(user))
                .thenReturn(user);

        User result =
                userService.updateWithoutPassword(1L, updatedUser);

        assertEquals("john_updated", result.getUsername());
        assertEquals("john.updated@example.com", result.getEmail());
        assertEquals("password123", result.getPassword());
        assertEquals(Role.ADMIN, result.getRole());

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository).save(user);
    }

    @Test
    void delete_shouldDeleteUser_whenUserHasNoLoanHistory() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        userService.delete(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void delete_shouldThrowException_whenUserHasLoanHistory() {

        user.getLoans().add(new Loan());

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        InvalidOperationException exception = assertThrows(
                InvalidOperationException.class,
                () -> userService.delete(1L)
        );

        assertEquals(
                "User cannot be deleted because they have loan history",
                exception.getMessage()
        );

        verify(userRepository, never())
                .delete(any(User.class));
    }
}
