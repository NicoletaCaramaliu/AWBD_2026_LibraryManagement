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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class BorrowBookIntegrationTest {

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

    private User user;
    private Book book;

    @BeforeEach
    void setUp() {

        loanRepository.deleteAll();
        bookRepository.deleteAll();
        publisherRepository.deleteAll();
        userRepository.deleteAll();

        user = User.builder()
                .username("john")
                .email("john@example.com")
                .password("password123")
                .role(Role.USER)
                .build();

        user = userRepository.save(user);

        Publisher publisher = Publisher.builder()
                .name("Penguin Books")
                .country("United Kingdom")
                .build();

        publisher = publisherRepository.save(publisher);

        BookDetails bookDetails = BookDetails.builder()
                .isbn("9780451524935")
                .numberOfPages(328)
                .language("English")
                .publicationYear(1949)
                .description("Dystopian novel")
                .build();

        book = Book.builder()
                .title("1984")
                .price(new BigDecimal("39.99"))
                .stock(3)
                .bookDetails(bookDetails)
                .publisher(publisher)
                .build();

        book = bookRepository.save(book);
    }

    @Test
    void borrowBook_shouldCreateActiveLoanAndDecreaseStock() throws Exception {

        mockMvc.perform(
                        post("/api/loans")
                                .param("userId", user.getId().toString())
                                .param("bookId", book.getId().toString())
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.user.id").value(user.getId()))
                .andExpect(jsonPath("$.book.id").value(book.getId()))
                .andExpect(jsonPath("$.loanDate").exists())
                .andExpect(jsonPath("$.dueDate").exists())
                .andExpect(jsonPath("$.returnDate").doesNotExist());

        Book updatedBook = bookRepository
                .findById(book.getId())
                .orElseThrow();

        assert updatedBook.getStock() == 2;
        assert loanRepository.count() == 1;
    }
}