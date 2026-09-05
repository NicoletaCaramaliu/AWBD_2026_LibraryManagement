package org.example.librarymanagement.repository;

import org.example.librarymanagement.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorRepository extends JpaRepository<Author, Long> {

    Optional<Author> findByFirstNameAndLastName(
            String firstName,
            String lastName
    );
}