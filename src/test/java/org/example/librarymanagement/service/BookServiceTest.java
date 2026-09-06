package org.example.librarymanagement.service;

import org.example.librarymanagement.entity.*;
import org.example.librarymanagement.exception.InvalidOperationException;
import org.example.librarymanagement.exception.ResourceNotFoundException;
import org.example.librarymanagement.repository.AuthorRepository;
import org.example.librarymanagement.repository.BookRepository;
import org.example.librarymanagement.repository.CategoryRepository;
import org.example.librarymanagement.repository.PublisherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private PublisherRepository publisherRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private BookService bookService;

    private Book book;
    private Publisher publisher;
    private Author author;
    private Category category;
    private BookDetails bookDetails;

    @BeforeEach
    void setUp() {

        publisher = Publisher.builder()
                .id(1L)
                .name("Penguin Books")
                .country("United Kingdom")
                .build();

        author = Author.builder()
                .id(1L)
                .firstName("George")
                .lastName("Orwell")
                .build();

        category = Category.builder()
                .id(1L)
                .name("Fiction")
                .description("Fiction books")
                .build();

        bookDetails = BookDetails.builder()
                .id(1L)
                .isbn("9780451524935")
                .numberOfPages(328)
                .language("English")
                .publicationYear(1949)
                .description("Dystopian novel")
                .build();

        book = Book.builder()
                .id(1L)
                .title("1984")
                .price(new BigDecimal("39.99"))
                .stock(5)
                .bookDetails(bookDetails)
                .publisher(publisher)
                .authors(new HashSet<>(Set.of(author)))
                .categories(new HashSet<>(Set.of(category)))
                .loans(new ArrayList<>())
                .build();
    }

    @Test
    void create_shouldSaveBook_whenRelationsExist() {

        when(publisherRepository.findById(1L))
                .thenReturn(Optional.of(publisher));

        when(authorRepository.findById(1L))
                .thenReturn(Optional.of(author));

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(bookRepository.save(book))
                .thenReturn(book);

        Book result = bookService.create(book);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("1984", result.getTitle());
        assertEquals(new BigDecimal("39.99"), result.getPrice());
        assertEquals(5, result.getStock());

        assertEquals(publisher, result.getPublisher());
        assertTrue(result.getAuthors().contains(author));
        assertTrue(result.getCategories().contains(category));

        verify(publisherRepository).findById(1L);
        verify(authorRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(bookRepository).save(book);
    }

    @Test
    void create_shouldThrowException_whenPublisherDoesNotExist() {

        when(publisherRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bookService.create(book)
        );

        assertEquals(
                "Publisher with id 1 was not found",
                exception.getMessage()
        );

        verify(bookRepository, never())
                .save(any(Book.class));
    }

    @Test
    void create_shouldThrowException_whenAuthorDoesNotExist() {

        when(publisherRepository.findById(1L))
                .thenReturn(Optional.of(publisher));

        when(authorRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bookService.create(book)
        );

        assertEquals(
                "Author with id 1 was not found",
                exception.getMessage()
        );

        verify(bookRepository, never())
                .save(any(Book.class));
    }

    @Test
    void create_shouldThrowException_whenAuthorHasNoId() {

        Author authorWithoutId = Author.builder()
                .firstName("George")
                .lastName("Orwell")
                .build();

        book.setAuthors(Set.of(authorWithoutId));

        when(publisherRepository.findById(1L))
                .thenReturn(Optional.of(publisher));

        InvalidOperationException exception = assertThrows(
                InvalidOperationException.class,
                () -> bookService.create(book)
        );

        assertEquals(
                "Author id is required when assigning an author to a book",
                exception.getMessage()
        );

        verify(bookRepository, never())
                .save(any(Book.class));
    }

    @Test
    void create_shouldThrowException_whenCategoryDoesNotExist() {

        when(publisherRepository.findById(1L))
                .thenReturn(Optional.of(publisher));

        when(authorRepository.findById(1L))
                .thenReturn(Optional.of(author));

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bookService.create(book)
        );

        assertEquals(
                "Category with id 1 was not found",
                exception.getMessage()
        );

        verify(bookRepository, never())
                .save(any(Book.class));
    }

    @Test
    void create_shouldThrowException_whenCategoryHasNoId() {

        Category categoryWithoutId = Category.builder()
                .name("Fiction")
                .description("Fiction books")
                .build();

        book.setCategories(Set.of(categoryWithoutId));

        when(publisherRepository.findById(1L))
                .thenReturn(Optional.of(publisher));

        when(authorRepository.findById(1L))
                .thenReturn(Optional.of(author));

        InvalidOperationException exception = assertThrows(
                InvalidOperationException.class,
                () -> bookService.create(book)
        );

        assertEquals(
                "Category id is required when assigning a category to a book",
                exception.getMessage()
        );

        verify(bookRepository, never())
                .save(any(Book.class));
    }

    @Test
    void getById_shouldReturnBook_whenBookExists() {

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        Book result = bookService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("1984", result.getTitle());

        verify(bookRepository).findById(1L);
    }

    @Test
    void getById_shouldThrowException_whenBookDoesNotExist() {

        when(bookRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bookService.getById(99L)
        );

        assertEquals(
                "Book with id 99 was not found",
                exception.getMessage()
        );

        verify(bookRepository).findById(99L);
    }

    @Test
    void getAll_shouldReturnAllBooks() {

        Book secondBook = Book.builder()
                .id(2L)
                .title("Animal Farm")
                .price(new BigDecimal("29.99"))
                .stock(3)
                .build();

        when(bookRepository.findAll())
                .thenReturn(List.of(book, secondBook));

        List<Book> result = bookService.getAll();

        assertEquals(2, result.size());
        assertEquals("1984", result.get(0).getTitle());
        assertEquals("Animal Farm", result.get(1).getTitle());

        verify(bookRepository).findAll();
    }

    @Test
    void update_shouldUpdateBook_whenDataIsValid() {

        Book updatedBook = Book.builder()
                .title("Nineteen Eighty-Four")
                .price(new BigDecimal("49.99"))
                .stock(10)
                .bookDetails(bookDetails)
                .publisher(publisher)
                .authors(new HashSet<>(Set.of(author)))
                .categories(new HashSet<>(Set.of(category)))
                .build();

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        when(publisherRepository.findById(1L))
                .thenReturn(Optional.of(publisher));

        when(authorRepository.findById(1L))
                .thenReturn(Optional.of(author));

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(bookRepository.save(book))
                .thenReturn(book);

        Book result = bookService.update(1L, updatedBook);

        assertEquals(
                "Nineteen Eighty-Four",
                result.getTitle()
        );

        assertEquals(
                new BigDecimal("49.99"),
                result.getPrice()
        );

        assertEquals(10, result.getStock());
        assertEquals(publisher, result.getPublisher());
        assertEquals(bookDetails, result.getBookDetails());

        verify(bookRepository).save(book);
    }

    @Test
    void delete_shouldDeleteBook_whenBookHasNoLoans() {

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        bookService.delete(1L);

        verify(bookRepository).delete(book);
    }

    @Test
    void delete_shouldThrowException_whenBookHasLoanHistory() {

        Loan loan = new Loan();
        book.getLoans().add(loan);

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        InvalidOperationException exception = assertThrows(
                InvalidOperationException.class,
                () -> bookService.delete(1L)
        );

        assertEquals(
                "Book cannot be deleted because it has loan history",
                exception.getMessage()
        );

        verify(bookRepository, never())
                .delete(any(Book.class));
    }
}