package org.example.librarymanagement.service;

import lombok.RequiredArgsConstructor;
import org.example.librarymanagement.entity.Book;
import org.example.librarymanagement.entity.Loan;
import org.example.librarymanagement.entity.LoanStatus;
import org.example.librarymanagement.entity.User;
import org.example.librarymanagement.exception.InvalidOperationException;
import org.example.librarymanagement.exception.ResourceNotFoundException;
import org.example.librarymanagement.repository.BookRepository;
import org.example.librarymanagement.repository.LoanRepository;
import org.example.librarymanagement.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public Loan create(Long userId, Long bookId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User with id " + userId + " was not found"
                        )
                );

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book with id " + bookId + " was not found"
                        )
                );

        if (book.getStock() <= 0) {
            throw new InvalidOperationException(
                    "Book is currently unavailable"
            );
        }

        if (loanRepository.existsByUser_IdAndStatus(
                userId,
                LoanStatus.ACTIVE)) {

            // Poți scoate regula dacă profesorul nu vrea această limitare.
            // Deocamdată NU o folosim ca regulă restrictivă.
        }

        book.setStock(book.getStock() - 1);

        Loan loan = Loan.builder()
                .loanDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .returnDate(null)
                .status(LoanStatus.ACTIVE)
                .user(user)
                .book(book)
                .build();

        bookRepository.save(book);

        return loanRepository.save(loan);
    }

    @Transactional(readOnly = true)
    public Loan getById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan with id " + id + " was not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<Loan> getAll() {
        return loanRepository.findAll();
    }

    public Loan update(Long id, Loan updatedLoan) {

        Loan existing = getById(id);

        if (existing.getStatus() == LoanStatus.RETURNED) {
            throw new InvalidOperationException(
                    "A returned loan cannot be modified"
            );
        }

        if (updatedLoan.getDueDate() != null) {

            if (updatedLoan.getDueDate()
                    .isBefore(existing.getLoanDate())) {

                throw new InvalidOperationException(
                        "Due date cannot be before loan date"
                );
            }

            existing.setDueDate(updatedLoan.getDueDate());
        }

        return loanRepository.save(existing);
    }

    public Loan returnBook(Long id) {

        Loan loan = getById(id);

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new InvalidOperationException(
                    "This book has already been returned"
            );
        }

        loan.setReturnDate(LocalDate.now());
        loan.setStatus(LoanStatus.RETURNED);

        Book book = loan.getBook();

        book.setStock(book.getStock() + 1);

        bookRepository.save(book);

        return loanRepository.save(loan);
    }

    public void delete(Long id) {

        Loan loan = getById(id);

        if (loan.getStatus() == LoanStatus.ACTIVE) {
            throw new InvalidOperationException(
                    "An active loan cannot be deleted. Return the book first."
            );
        }

        loanRepository.delete(loan);
    }
}