package org.example.librarymanagement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public Loan create(Long userId, Long bookId) {

        log.debug("Creating loan for userId={} and bookId={}", userId, bookId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Cannot create loan. User with id {} was not found", userId);

                    return new ResourceNotFoundException(
                            "User with id " + userId + " was not found"
                    );
                });

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> {
                    log.error("Cannot create loan. Book with id {} was not found", bookId);

                    return new ResourceNotFoundException(
                            "Book with id " + bookId + " was not found"
                    );
                });

        if (book.getStock() <= 0) {
            log.error("Cannot create loan for bookId={}. Book is out of stock", bookId);

            throw new InvalidOperationException(
                    "Book is currently unavailable"
            );
        }

        if (loanRepository.existsByUser_IdAndStatus(
                userId,
                LoanStatus.ACTIVE)) {

            log.debug("User with id {} already has an active loan", userId);

            // Deocamdată nu folosim regula ca restricție.
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

        Loan savedLoan = loanRepository.save(loan);

        log.info(
                "Loan created successfully. loanId={}, userId={}, bookId={}",
                savedLoan.getId(),
                userId,
                bookId
        );

        return savedLoan;
    }

    @Transactional(readOnly = true)
    public Loan getById(Long id) {

        log.debug("Searching for loan with id={}", id);

        return loanRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Loan with id {} was not found", id);

                    return new ResourceNotFoundException(
                            "Loan with id " + id + " was not found"
                    );
                });
    }

    @Transactional(readOnly = true)
    public List<Loan> getAll() {

        log.debug("Retrieving all loans");

        return loanRepository.findAll();
    }

    public Loan update(Long id, Loan updatedLoan) {

        log.debug("Updating loan with id={}", id);

        Loan existing = getById(id);

        if (existing.getStatus() == LoanStatus.RETURNED) {
            log.error("Cannot update loan with id {} because it is already returned", id);

            throw new InvalidOperationException(
                    "A returned loan cannot be modified"
            );
        }

        if (updatedLoan.getDueDate() != null) {

            if (updatedLoan.getDueDate()
                    .isBefore(existing.getLoanDate())) {

                log.error(
                        "Cannot update loan with id {}. Due date {} is before loan date {}",
                        id,
                        updatedLoan.getDueDate(),
                        existing.getLoanDate()
                );

                throw new InvalidOperationException(
                        "Due date cannot be before loan date"
                );
            }

            existing.setDueDate(updatedLoan.getDueDate());
        }

        Loan savedLoan = loanRepository.save(existing);

        log.info("Loan with id {} was updated successfully", id);

        return savedLoan;
    }

    public Loan returnBook(Long id) {

        log.debug("Returning book for loanId={}", id);

        Loan loan = getById(id);

        if (loan.getStatus() == LoanStatus.RETURNED) {
            log.error("Cannot return loan with id {} because it is already returned", id);

            throw new InvalidOperationException(
                    "This book has already been returned"
            );
        }

        loan.setReturnDate(LocalDate.now());
        loan.setStatus(LoanStatus.RETURNED);

        Book book = loan.getBook();

        book.setStock(book.getStock() + 1);

        bookRepository.save(book);

        Loan savedLoan = loanRepository.save(loan);

        log.info(
                "Book returned successfully. loanId={}, bookId={}",
                id,
                book.getId()
        );

        return savedLoan;
    }

    public void delete(Long id) {

        log.debug("Deleting loan with id={}", id);

        Loan loan = getById(id);

        if (loan.getStatus() == LoanStatus.ACTIVE) {
            log.error("Cannot delete active loan with id={}", id);

            throw new InvalidOperationException(
                    "An active loan cannot be deleted. Return the book first."
            );
        }

        loanRepository.delete(loan);

        log.info("Loan with id {} was deleted successfully", id);
    }
}