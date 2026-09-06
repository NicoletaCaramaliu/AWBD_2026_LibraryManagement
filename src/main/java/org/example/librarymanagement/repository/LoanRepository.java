package org.example.librarymanagement.repository;

import org.example.librarymanagement.entity.Loan;
import org.example.librarymanagement.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    boolean existsByUser_IdAndStatus(
            Long userId,
            LoanStatus status
    );

    boolean existsByBook_IdAndStatus(
            Long bookId,
            LoanStatus status
    );
}