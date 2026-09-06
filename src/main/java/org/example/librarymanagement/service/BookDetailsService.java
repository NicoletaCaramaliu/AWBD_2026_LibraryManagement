package org.example.librarymanagement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.librarymanagement.entity.BookDetails;
import org.example.librarymanagement.exception.DuplicateResourceException;
import org.example.librarymanagement.exception.InvalidOperationException;
import org.example.librarymanagement.exception.ResourceNotFoundException;
import org.example.librarymanagement.repository.BookDetailsRepository;
import org.example.librarymanagement.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BookDetailsService {

    private final BookDetailsRepository bookDetailsRepository;
    private final BookRepository bookRepository;

    public BookDetails create(BookDetails bookDetails) {

        log.debug(
                "Creating book details with ISBN={}",
                bookDetails.getIsbn()
        );

        bookDetailsRepository.findByIsbn(bookDetails.getIsbn())
                .ifPresent(existing -> {

                    log.error(
                            "Cannot create book details. ISBN '{}' already exists",
                            bookDetails.getIsbn()
                    );

                    throw new DuplicateResourceException(
                            "Book details with ISBN '" +
                                    bookDetails.getIsbn() +
                                    "' already exist"
                    );
                });

        BookDetails savedDetails =
                bookDetailsRepository.save(bookDetails);

        log.info(
                "Book details created successfully. id={}",
                savedDetails.getId()
        );

        return savedDetails;
    }

    @Transactional(readOnly = true)
    public BookDetails getById(Long id) {

        log.debug("Searching for book details with id={}", id);

        return bookDetailsRepository.findById(id)
                .orElseThrow(() -> {

                    log.error(
                            "BookDetails with id {} was not found",
                            id
                    );

                    return new ResourceNotFoundException(
                            "BookDetails with id " + id + " was not found"
                    );
                });
    }

    @Transactional(readOnly = true)
    public List<BookDetails> getAll() {

        log.debug("Retrieving all book details");

        return bookDetailsRepository.findAll();
    }

    public BookDetails update(
            Long id,
            BookDetails updatedDetails) {

        log.debug(
                "Updating book details with id={}",
                id
        );

        BookDetails existing = getById(id);

        bookDetailsRepository.findByIsbn(updatedDetails.getIsbn())
                .filter(details -> !details.getId().equals(id))
                .ifPresent(details -> {

                    log.error(
                            "Cannot update book details with id {}. ISBN '{}' is already used",
                            id,
                            updatedDetails.getIsbn()
                    );

                    throw new DuplicateResourceException(
                            "Another book already uses this ISBN"
                    );
                });

        existing.setIsbn(updatedDetails.getIsbn());
        existing.setNumberOfPages(updatedDetails.getNumberOfPages());
        existing.setLanguage(updatedDetails.getLanguage());
        existing.setPublicationYear(updatedDetails.getPublicationYear());
        existing.setDescription(updatedDetails.getDescription());

        BookDetails savedDetails =
                bookDetailsRepository.save(existing);

        log.info(
                "Book details with id {} were updated successfully",
                id
        );

        return savedDetails;
    }

    public void delete(Long id) {

        log.debug(
                "Deleting book details with id={}",
                id
        );

        BookDetails details = getById(id);

        if (bookRepository.existsByBookDetails_Id(id)) {

            log.error(
                    "Cannot delete book details with id {} because they are associated with a book",
                    id
            );

            throw new InvalidOperationException(
                    "Book details cannot be deleted while associated with a book"
            );
        }

        bookDetailsRepository.delete(details);

        log.info(
                "Book details with id {} were deleted successfully",
                id
        );
    }
}