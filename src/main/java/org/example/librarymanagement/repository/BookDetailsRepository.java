package org.example.librarymanagement.repository;

import org.example.librarymanagement.entity.BookDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookDetailsRepository
        extends JpaRepository<BookDetails, Long> {

    Optional<BookDetails> findByIsbn(String isbn);
}