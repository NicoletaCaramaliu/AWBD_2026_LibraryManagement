package org.example.librarymanagement.service;

import org.example.librarymanagement.entity.BookDetails;
import org.example.librarymanagement.exception.DuplicateResourceException;
import org.example.librarymanagement.exception.InvalidOperationException;
import org.example.librarymanagement.exception.ResourceNotFoundException;
import org.example.librarymanagement.repository.BookDetailsRepository;
import org.example.librarymanagement.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookDetailsServiceTest {

    @Mock
    private BookDetailsRepository bookDetailsRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookDetailsService bookDetailsService;

    private BookDetails bookDetails;

    @BeforeEach
    void setUp() {
        bookDetails = BookDetails.builder()
                .id(1L)
                .isbn("9780451524935")
                .numberOfPages(328)
                .language("English")
                .publicationYear(1949)
                .description("Dystopian novel")
                .build();
    }

    @Test
    void create_shouldSaveBookDetails_whenIsbnDoesNotExist() {

        when(bookDetailsRepository.findByIsbn("9780451524935"))
                .thenReturn(Optional.empty());

        when(bookDetailsRepository.save(bookDetails))
                .thenReturn(bookDetails);

        BookDetails result = bookDetailsService.create(bookDetails);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("9780451524935", result.getIsbn());
        assertEquals(328, result.getNumberOfPages());
        assertEquals("English", result.getLanguage());
        assertEquals(1949, result.getPublicationYear());
        assertEquals("Dystopian novel", result.getDescription());

        verify(bookDetailsRepository)
                .findByIsbn("9780451524935");

        verify(bookDetailsRepository)
                .save(bookDetails);
    }

    @Test
    void create_shouldThrowException_whenIsbnAlreadyExists() {

        when(bookDetailsRepository.findByIsbn("9780451524935"))
                .thenReturn(Optional.of(bookDetails));

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> bookDetailsService.create(bookDetails)
        );

        assertEquals(
                "Book details with ISBN '9780451524935' already exist",
                exception.getMessage()
        );

        verify(bookDetailsRepository, never())
                .save(any(BookDetails.class));
    }

    @Test
    void getById_shouldReturnBookDetails_whenBookDetailsExist() {

        when(bookDetailsRepository.findById(1L))
                .thenReturn(Optional.of(bookDetails));

        BookDetails result = bookDetailsService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("9780451524935", result.getIsbn());

        verify(bookDetailsRepository).findById(1L);
    }

    @Test
    void getById_shouldThrowException_whenBookDetailsDoNotExist() {

        when(bookDetailsRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bookDetailsService.getById(99L)
        );

        assertEquals(
                "BookDetails with id 99 was not found",
                exception.getMessage()
        );

        verify(bookDetailsRepository).findById(99L);
    }

    @Test
    void getAll_shouldReturnAllBookDetails() {

        BookDetails secondBookDetails = BookDetails.builder()
                .id(2L)
                .isbn("9780141439518")
                .numberOfPages(432)
                .language("English")
                .publicationYear(1813)
                .description("Classic novel")
                .build();

        when(bookDetailsRepository.findAll())
                .thenReturn(List.of(bookDetails, secondBookDetails));

        List<BookDetails> result = bookDetailsService.getAll();

        assertEquals(2, result.size());
        assertEquals("9780451524935", result.get(0).getIsbn());
        assertEquals("9780141439518", result.get(1).getIsbn());

        verify(bookDetailsRepository).findAll();
    }

    @Test
    void update_shouldUpdateBookDetails_whenDataIsValid() {

        BookDetails updatedDetails = BookDetails.builder()
                .isbn("9780451524936")
                .numberOfPages(350)
                .language("Romanian")
                .publicationYear(1950)
                .description("Updated description")
                .build();

        when(bookDetailsRepository.findById(1L))
                .thenReturn(Optional.of(bookDetails));

        when(bookDetailsRepository.findByIsbn("9780451524936"))
                .thenReturn(Optional.empty());

        when(bookDetailsRepository.save(bookDetails))
                .thenReturn(bookDetails);

        BookDetails result = bookDetailsService.update(
                1L,
                updatedDetails
        );

        assertEquals("9780451524936", result.getIsbn());
        assertEquals(350, result.getNumberOfPages());
        assertEquals("Romanian", result.getLanguage());
        assertEquals(1950, result.getPublicationYear());
        assertEquals("Updated description", result.getDescription());

        verify(bookDetailsRepository).save(bookDetails);
    }

    @Test
    void update_shouldThrowException_whenAnotherBookUsesSameIsbn() {

        BookDetails updatedDetails = BookDetails.builder()
                .isbn("9780141439518")
                .numberOfPages(400)
                .language("English")
                .publicationYear(2000)
                .description("Updated")
                .build();

        BookDetails duplicateDetails = BookDetails.builder()
                .id(2L)
                .isbn("9780141439518")
                .build();

        when(bookDetailsRepository.findById(1L))
                .thenReturn(Optional.of(bookDetails));

        when(bookDetailsRepository.findByIsbn("9780141439518"))
                .thenReturn(Optional.of(duplicateDetails));

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> bookDetailsService.update(
                        1L,
                        updatedDetails
                )
        );

        assertEquals(
                "Another book already uses this ISBN",
                exception.getMessage()
        );

        verify(bookDetailsRepository, never())
                .save(any(BookDetails.class));
    }

    @Test
    void delete_shouldDeleteBookDetails_whenNotAssociatedWithBook() {

        when(bookDetailsRepository.findById(1L))
                .thenReturn(Optional.of(bookDetails));

        when(bookRepository.existsByBookDetails_Id(1L))
                .thenReturn(false);

        bookDetailsService.delete(1L);

        verify(bookDetailsRepository).delete(bookDetails);
    }

    @Test
    void delete_shouldThrowException_whenAssociatedWithBook() {

        when(bookDetailsRepository.findById(1L))
                .thenReturn(Optional.of(bookDetails));

        when(bookRepository.existsByBookDetails_Id(1L))
                .thenReturn(true);

        InvalidOperationException exception = assertThrows(
                InvalidOperationException.class,
                () -> bookDetailsService.delete(1L)
        );

        assertEquals(
                "Book details cannot be deleted while associated with a book",
                exception.getMessage()
        );

        verify(bookDetailsRepository, never())
                .delete(any(BookDetails.class));
    }
}