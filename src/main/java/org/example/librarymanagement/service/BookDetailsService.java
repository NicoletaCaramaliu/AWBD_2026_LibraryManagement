package org.example.librarymanagement.service;

import lombok.RequiredArgsConstructor;
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
public class BookDetailsService {

    private final BookDetailsRepository bookDetailsRepository;
    private final BookRepository bookRepository;

    public BookDetails create(BookDetails bookDetails) {

        bookDetailsRepository.findByIsbn(bookDetails.getIsbn())
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Book details with ISBN '" +
                                    bookDetails.getIsbn() +
                                    "' already exist"
                    );
                });

        return bookDetailsRepository.save(bookDetails);
    }

    @Transactional(readOnly = true)
    public BookDetails getById(Long id) {
        return bookDetailsRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "BookDetails with id " + id + " was not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<BookDetails> getAll() {
        return bookDetailsRepository.findAll();
    }

    public BookDetails update(
            Long id,
            BookDetails updatedDetails) {

        BookDetails existing = getById(id);

        bookDetailsRepository.findByIsbn(updatedDetails.getIsbn())
                .filter(details -> !details.getId().equals(id))
                .ifPresent(details -> {
                    throw new DuplicateResourceException(
                            "Another book already uses this ISBN"
                    );
                });

        existing.setIsbn(updatedDetails.getIsbn());
        existing.setNumberOfPages(updatedDetails.getNumberOfPages());
        existing.setLanguage(updatedDetails.getLanguage());
        existing.setPublicationYear(updatedDetails.getPublicationYear());
        existing.setDescription(updatedDetails.getDescription());

        return bookDetailsRepository.save(existing);
    }

    public void delete(Long id) {

        BookDetails details = getById(id);

        if (bookRepository.existsByBookDetails_Id(id)) {
            throw new InvalidOperationException(
                    "Book details cannot be deleted while associated with a book"
            );
        }

        bookDetailsRepository.delete(details);
    }
}