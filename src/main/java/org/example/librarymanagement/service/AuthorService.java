package org.example.librarymanagement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.librarymanagement.entity.Author;
import org.example.librarymanagement.exception.DuplicateResourceException;
import org.example.librarymanagement.exception.InvalidOperationException;
import org.example.librarymanagement.exception.ResourceNotFoundException;
import org.example.librarymanagement.repository.AuthorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthorService {

    private final AuthorRepository authorRepository;

    public Author create(Author author) {

        log.debug(
                "Creating author: {} {}",
                author.getFirstName(),
                author.getLastName()
        );

        authorRepository
                .findByFirstNameAndLastName(
                        author.getFirstName(),
                        author.getLastName()
                )
                .ifPresent(existing -> {
                    log.error(
                            "Cannot create author. Author {} {} already exists",
                            author.getFirstName(),
                            author.getLastName()
                    );

                    throw new DuplicateResourceException(
                            "Author already exists"
                    );
                });

        Author savedAuthor = authorRepository.save(author);

        log.info(
                "Author created successfully. id={}",
                savedAuthor.getId()
        );

        return savedAuthor;
    }

    @Transactional(readOnly = true)
    public Author getById(Long id) {

        log.debug("Searching for author with id={}", id);

        return authorRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Author with id {} was not found", id);

                    return new ResourceNotFoundException(
                            "Author with id " + id + " was not found"
                    );
                });
    }

    @Transactional(readOnly = true)
    public List<Author> getAll() {

        log.debug("Retrieving all authors");

        return authorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Author> getAll(Pageable pageable) {

        log.debug(
                "Retrieving authors page={}, size={}, sort={}",
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort()
        );

        return authorRepository.findAll(pageable);
    }

    public Author update(Long id, Author updatedAuthor) {

        log.debug("Updating author with id={}", id);

        Author existingAuthor = getById(id);

        authorRepository
                .findByFirstNameAndLastName(
                        updatedAuthor.getFirstName(),
                        updatedAuthor.getLastName()
                )
                .filter(author -> !author.getId().equals(id))
                .ifPresent(author -> {
                    log.error(
                            "Cannot update author with id {}. Another author with the same name already exists",
                            id
                    );

                    throw new DuplicateResourceException(
                            "Another author with this name already exists"
                    );
                });

        existingAuthor.setFirstName(updatedAuthor.getFirstName());
        existingAuthor.setLastName(updatedAuthor.getLastName());

        Author savedAuthor = authorRepository.save(existingAuthor);

        log.info(
                "Author with id {} was updated successfully",
                id
        );

        return savedAuthor;
    }

    public void delete(Long id) {

        log.debug("Deleting author with id={}", id);

        Author author = getById(id);

        if (!author.getBooks().isEmpty()) {
            log.error(
                    "Cannot delete author with id {} because books are associated with this author",
                    id
            );

            throw new InvalidOperationException(
                    "Author cannot be deleted because books are associated with this author"
            );
        }

        authorRepository.delete(author);

        log.info(
                "Author with id {} was deleted successfully",
                id
        );
    }
}