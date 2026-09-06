package org.example.librarymanagement.service;

import lombok.RequiredArgsConstructor;
import org.example.librarymanagement.entity.Author;
import org.example.librarymanagement.exception.DuplicateResourceException;
import org.example.librarymanagement.exception.InvalidOperationException;
import org.example.librarymanagement.exception.ResourceNotFoundException;
import org.example.librarymanagement.repository.AuthorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthorService {

    private final AuthorRepository authorRepository;

    public Author create(Author author) {

        authorRepository
                .findByFirstNameAndLastName(
                        author.getFirstName(),
                        author.getLastName()
                )
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Author already exists"
                    );
                });

        return authorRepository.save(author);
    }

    @Transactional(readOnly = true)
    public Author getById(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Author with id " + id + " was not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<Author> getAll() {
        return authorRepository.findAll();
    }

    public Author update(Long id, Author updatedAuthor) {

        Author existingAuthor = getById(id);

        authorRepository
                .findByFirstNameAndLastName(
                        updatedAuthor.getFirstName(),
                        updatedAuthor.getLastName()
                )
                .filter(author -> !author.getId().equals(id))
                .ifPresent(author -> {
                    throw new DuplicateResourceException(
                            "Another author with this name already exists"
                    );
                });

        existingAuthor.setFirstName(updatedAuthor.getFirstName());
        existingAuthor.setLastName(updatedAuthor.getLastName());

        return authorRepository.save(existingAuthor);
    }

    public void delete(Long id) {

        Author author = getById(id);

        if (!author.getBooks().isEmpty()) {
            throw new InvalidOperationException(
                    "Author cannot be deleted because books are associated with this author"
            );
        }

        authorRepository.delete(author);
    }
}