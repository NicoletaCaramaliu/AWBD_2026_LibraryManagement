package org.example.librarymanagement.integration;

import org.example.librarymanagement.entity.*;
import org.example.librarymanagement.repository.BookRepository;
import org.example.librarymanagement.repository.LoanRepository;
import org.example.librarymanagement.repository.PublisherRepository;
import org.example.librarymanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class ReturnBookIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;

    private Book book;
    private Loan loan;

    @BeforeEach
    void setUp() {

        loanRepository.deleteAll();
        bookRepository.deleteAll();
        publisherRepository.deleteAll();
        userRepository.deleteAll();

        User user = User.builder()
                .username("maria")
                .email("maria@example.com")
                .password("password123")
                .role(Role.USER)
                .build();

        user = userRepository.save(user);

        Publisher publisher = Publisher.builder()
                .name("Vintage")
                .country("United States")
                .build();

        publisher = publisherRepository.save(publisher);

        BookDetails bookDetails = BookDetails.builder()
                .isbn("9780061120084")
                .numberOfPages(336)
                .language("English")
                .publicationYear(1960)
                .description("Classic novel")
                .build();

        book = Book.builder()
                .title("To Kill a Mockingbird")
                .price(new BigDecimal("45.00"))
                .stock(1)
                .bookDetails(bookDetails)
                .publisher(publisher)
                .build();

        book = bookRepository.save(book);

        loan = Loan.builder()
                .loanDate(LocalDate.now().minusDays(5))
                .dueDate(LocalDate.now().plusDays(9))
                .returnDate(null)
                .status(LoanStatus.ACTIVE)
                .user(user)
                .book(book)
                .build();

        loan = loanRepository.save(loan);
    }

    @Test
    void returnBook_shouldMarkLoanReturnedAndIncreaseStock() throws Exception {

        mockMvc.perform(
                        put("/api/loans/{id}/return", loan.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"))
                .andExpect(jsonPath("$.returnDate").exists());

        Loan updatedLoan = loanRepository
                .findById(loan.getId())
                .orElseThrow();

        Book updatedBook = bookRepository
                .findById(book.getId())
                .orElseThrow();

        assert updatedLoan.getStatus() == LoanStatus.RETURNED;
        assert updatedLoan.getReturnDate() != null;
        assert updatedBook.getStock() == 2;
    }
}