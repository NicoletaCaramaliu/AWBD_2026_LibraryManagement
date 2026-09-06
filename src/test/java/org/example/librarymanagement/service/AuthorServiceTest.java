package org.example.librarymanagement.service;

import org.example.librarymanagement.entity.Author;
import org.example.librarymanagement.entity.Book;
import org.example.librarymanagement.exception.DuplicateResourceException;
import org.example.librarymanagement.exception.InvalidOperationException;
import org.example.librarymanagement.exception.ResourceNotFoundException;
import org.example.librarymanagement.repository.AuthorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private AuthorService authorService;

    private Author author;

    @BeforeEach
    void setUp() {
        author = Author.builder()
                .id(1L)
                .firstName("George")
                .lastName("Orwell")
                .books(new HashSet<>())
                .build();
    }

    @Test
    void create_shouldSaveAuthor_whenAuthorDoesNotExist() {

        when(authorRepository.findByFirstNameAndLastName(
                author.getFirstName(),
                author.getLastName()
        )).thenReturn(Optional.empty());

        when(authorRepository.save(author)).thenReturn(author);

        Author result = authorService.create(author);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("George", result.getFirstName());
        assertEquals("Orwell", result.getLastName());

        verify(authorRepository).findByFirstNameAndLastName(
                "George",
                "Orwell"
        );

        verify(authorRepository).save(author);
    }

    @Test
    void create_shouldThrowException_whenAuthorAlreadyExists() {

        when(authorRepository.findByFirstNameAndLastName(
                author.getFirstName(),
                author.getLastName()
        )).thenReturn(Optional.of(author));

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> authorService.create(author)
        );

        assertEquals(
                "Author already exists",
                exception.getMessage()
        );

        verify(authorRepository, never()).save(any(Author.class));
    }

    @Test
    void getById_shouldReturnAuthor_whenAuthorExists() {

        when(authorRepository.findById(1L))
                .thenReturn(Optional.of(author));

        Author result = authorService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("George", result.getFirstName());
        assertEquals("Orwell", result.getLastName());

        verify(authorRepository).findById(1L);
    }

    @Test
    void getById_shouldThrowException_whenAuthorDoesNotExist() {

        when(authorRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> authorService.getById(99L)
        );

        assertEquals(
                "Author with id 99 was not found",
                exception.getMessage()
        );

        verify(authorRepository).findById(99L);
    }

    @Test
    void getAll_shouldReturnAllAuthors() {

        Author secondAuthor = Author.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Austen")
                .books(new HashSet<>())
                .build();

        when(authorRepository.findAll())
                .thenReturn(List.of(author, secondAuthor));

        List<Author> result = authorService.getAll();

        assertEquals(2, result.size());
        assertEquals("George", result.get(0).getFirstName());
        assertEquals("Jane", result.get(1).getFirstName());

        verify(authorRepository).findAll();
    }

    @Test
    void update_shouldUpdateAuthor_whenDataIsValid() {

        Author updatedAuthor = Author.builder()
                .firstName("Eric")
                .lastName("Blair")
                .build();

        when(authorRepository.findById(1L))
                .thenReturn(Optional.of(author));

        when(authorRepository.findByFirstNameAndLastName(
                "Eric",
                "Blair"
        )).thenReturn(Optional.empty());

        when(authorRepository.save(author))
                .thenReturn(author);

        Author result = authorService.update(1L, updatedAuthor);

        assertEquals("Eric", result.getFirstName());
        assertEquals("Blair", result.getLastName());

        verify(authorRepository).save(author);
    }

    @Test
    void update_shouldThrowException_whenAnotherAuthorWithSameNameExists() {

        Author updatedAuthor = Author.builder()
                .firstName("Jane")
                .lastName("Austen")
                .build();

        Author duplicateAuthor = Author.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Austen")
                .build();

        when(authorRepository.findById(1L))
                .thenReturn(Optional.of(author));

        when(authorRepository.findByFirstNameAndLastName(
                "Jane",
                "Austen"
        )).thenReturn(Optional.of(duplicateAuthor));

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> authorService.update(1L, updatedAuthor)
        );

        assertEquals(
                "Another author with this name already exists",
                exception.getMessage()
        );

        verify(authorRepository, never()).save(any(Author.class));
    }

    @Test
    void delete_shouldDeleteAuthor_whenAuthorHasNoBooks() {

        when(authorRepository.findById(1L))
                .thenReturn(Optional.of(author));

        authorService.delete(1L);

        verify(authorRepository).delete(author);
    }

    @Test
    void delete_shouldThrowException_whenAuthorHasBooks() {

        Book book = new Book();
        author.getBooks().add(book);

        when(authorRepository.findById(1L))
                .thenReturn(Optional.of(author));

        InvalidOperationException exception = assertThrows(
                InvalidOperationException.class,
                () -> authorService.delete(1L)
        );

        assertEquals(
                "Author cannot be deleted because books are associated with this author",
                exception.getMessage()
        );

        verify(authorRepository, never()).delete(any(Author.class));
    }
}