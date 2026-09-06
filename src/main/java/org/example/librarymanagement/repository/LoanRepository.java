package org.example.librarymanagement.repository;

import org.example.librarymanagement.entity.Loan;
import org.example.librarymanagement.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    boolean existsByUser_IdAndStatus(
            Long userId,
            LoanStatus status
    );

    boolean existsByBook_IdAndStatus(
            Long bookId,
            LoanStatus status
    );

    List<Loan> findByUser_Id(Long userId);
}