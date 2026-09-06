package org.example.librarymanagement.service;

import org.example.librarymanagement.entity.Book;
import org.example.librarymanagement.entity.Loan;
import org.example.librarymanagement.entity.LoanStatus;
import org.example.librarymanagement.entity.User;
import org.example.librarymanagement.exception.InvalidOperationException;
import org.example.librarymanagement.exception.ResourceNotFoundException;
import org.example.librarymanagement.repository.BookRepository;
import org.example.librarymanagement.repository.LoanRepository;
import org.example.librarymanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LoanService loanService;

    private User user;
    private Book book;
    private Loan loan;

    @BeforeEach
    void setUp() {

        user = User.builder()
                .id(1L)
                .username("john")
                .email("john@example.com")
                .password("password")
                .build();

        book = Book.builder()
                .id(1L)
                .title("1984")
                .stock(3)
                .build();

        loan = Loan.builder()
                .id(1L)
                .loanDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .returnDate(null)
                .status(LoanStatus.ACTIVE)
                .user(user)
                .book(book)
                .build();
    }

    @Test
    void create_shouldCreateLoan_whenBookIsAvailable() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        when(loanRepository.existsByUser_IdAndStatus(
                1L,
                LoanStatus.ACTIVE
        )).thenReturn(false);

        when(loanRepository.save(any(Loan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Loan result = loanService.create(1L, 1L);

        assertNotNull(result);
        assertEquals(LoanStatus.ACTIVE, result.getStatus());
        assertEquals(user, result.getUser());
        assertEquals(book, result.getBook());

        assertEquals(
                LocalDate.now(),
                result.getLoanDate()
        );

        assertEquals(
                LocalDate.now().plusDays(14),
                result.getDueDate()
        );

        assertNull(result.getReturnDate());

        assertEquals(2, book.getStock());

        verify(userRepository).findById(1L);
        verify(bookRepository).findById(1L);
        verify(bookRepository).save(book);
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void create_shouldThrowException_whenUserDoesNotExist() {

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> loanService.create(99L, 1L)
        );

        assertEquals(
                "User with id 99 was not found",
                exception.getMessage()
        );

        verify(bookRepository, never())
                .findById(anyLong());

        verify(loanRepository, never())
                .save(any(Loan.class));
    }

    @Test
    void create_shouldThrowException_whenBookDoesNotExist() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(bookRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> loanService.create(1L, 99L)
        );

        assertEquals(
                "Book with id 99 was not found",
                exception.getMessage()
        );

        verify(loanRepository, never())
                .save(any(Loan.class));
    }

    @Test
    void create_shouldThrowException_whenBookIsOutOfStock() {

        book.setStock(0);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        InvalidOperationException exception = assertThrows(
                InvalidOperationException.class,
                () -> loanService.create(1L, 1L)
        );

        assertEquals(
                "Book is currently unavailable",
                exception.getMessage()
        );

        assertEquals(0, book.getStock());

        verify(bookRepository, never())
                .save(any(Book.class));

        verify(loanRepository, never())
                .save(any(Loan.class));
    }

    @Test
    void getById_shouldReturnLoan_whenLoanExists() {

        when(loanRepository.findById(1L))
                .thenReturn(Optional.of(loan));

        Loan result = loanService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(LoanStatus.ACTIVE, result.getStatus());

        verify(loanRepository).findById(1L);
    }

    @Test
    void getById_shouldThrowException_whenLoanDoesNotExist() {

        when(loanRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> loanService.getById(99L)
        );

        assertEquals(
                "Loan with id 99 was not found",
                exception.getMessage()
        );

        verify(loanRepository).findById(99L);
    }

    @Test
    void getAll_shouldReturnAllLoans() {

        Loan secondLoan = Loan.builder()
                .id(2L)
                .loanDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .status(LoanStatus.ACTIVE)
                .user(user)
                .book(book)
                .build();

        when(loanRepository.findAll())
                .thenReturn(List.of(loan, secondLoan));

        List<Loan> result = loanService.getAll();

        assertEquals(2, result.size());

        verify(loanRepository).findAll();
    }

    @Test
    void update_shouldUpdateDueDate_whenLoanIsActiveAndDateIsValid() {

        LocalDate newDueDate = LocalDate.now().plusDays(20);

        Loan updatedLoan = Loan.builder()
                .dueDate(newDueDate)
                .build();

        when(loanRepository.findById(1L))
                .thenReturn(Optional.of(loan));

        when(loanRepository.save(loan))
                .thenReturn(loan);

        Loan result = loanService.update(
                1L,
                updatedLoan
        );

        assertEquals(
                newDueDate,
                result.getDueDate()
        );

        verify(loanRepository).save(loan);
    }

    @Test
    void update_shouldThrowException_whenLoanIsReturned() {

        loan.setStatus(LoanStatus.RETURNED);

        Loan updatedLoan = Loan.builder()
                .dueDate(LocalDate.now().plusDays(20))
                .build();

        when(loanRepository.findById(1L))
                .thenReturn(Optional.of(loan));

        InvalidOperationException exception = assertThrows(
                InvalidOperationException.class,
                () -> loanService.update(
                        1L,
                        updatedLoan
                )
        );

        assertEquals(
                "A returned loan cannot be modified",
                exception.getMessage()
        );

        verify(loanRepository, never())
                .save(any(Loan.class));
    }

    @Test
    void update_shouldThrowException_whenDueDateIsBeforeLoanDate() {

        Loan updatedLoan = Loan.builder()
                .dueDate(
                        loan.getLoanDate().minusDays(1)
                )
                .build();

        when(loanRepository.findById(1L))
                .thenReturn(Optional.of(loan));

        InvalidOperationException exception = assertThrows(
                InvalidOperationException.class,
                () -> loanService.update(
                        1L,
                        updatedLoan
                )
        );

        assertEquals(
                "Due date cannot be before loan date",
                exception.getMessage()
        );

        verify(loanRepository, never())
                .save(any(Loan.class));
    }

    @Test
    void returnBook_shouldReturnLoanAndIncreaseStock() {

        when(loanRepository.findById(1L))
                .thenReturn(Optional.of(loan));

        when(bookRepository.save(book))
                .thenReturn(book);

        when(loanRepository.save(loan))
                .thenReturn(loan);

        int initialStock = book.getStock();

        Loan result = loanService.returnBook(1L);

        assertEquals(
                LoanStatus.RETURNED,
                result.getStatus()
        );

        assertNotNull(result.getReturnDate());

        assertEquals(
                LocalDate.now(),
                result.getReturnDate()
        );

        assertEquals(
                initialStock + 1,
                book.getStock()
        );

        verify(bookRepository).save(book);
        verify(loanRepository).save(loan);
    }

    @Test
    void returnBook_shouldThrowException_whenLoanAlreadyReturned() {

        loan.setStatus(LoanStatus.RETURNED);

        when(loanRepository.findById(1L))
                .thenReturn(Optional.of(loan));

        InvalidOperationException exception = assertThrows(
                InvalidOperationException.class,
                () -> loanService.returnBook(1L)
        );

        assertEquals(
                "This book has already been returned",
                exception.getMessage()
        );

        verify(bookRepository, never())
                .save(any(Book.class));

        verify(loanRepository, never())
                .save(any(Loan.class));
    }

    @Test
    void delete_shouldDeleteLoan_whenLoanIsReturned() {

        loan.setStatus(LoanStatus.RETURNED);

        when(loanRepository.findById(1L))
                .thenReturn(Optional.of(loan));

        loanService.delete(1L);

        verify(loanRepository).delete(loan);
    }

    @Test
    void delete_shouldThrowException_whenLoanIsActive() {

        when(loanRepository.findById(1L))
                .thenReturn(Optional.of(loan));

        InvalidOperationException exception = assertThrows(
                InvalidOperationException.class,
                () -> loanService.delete(1L)
        );

        assertEquals(
                "An active loan cannot be deleted. Return the book first.",
                exception.getMessage()
        );

        verify(loanRepository, never())
                .delete(any(Loan.class));
    }
}