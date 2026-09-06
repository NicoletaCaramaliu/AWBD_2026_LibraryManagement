package org.example.librarymanagement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.librarymanagement.entity.*;
import org.example.librarymanagement.exception.InvalidOperationException;
import org.example.librarymanagement.exception.ResourceNotFoundException;
import org.example.librarymanagement.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final PublisherRepository publisherRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final BookDetailsRepository bookDetailsRepository;

    public Book create(Book book) {

        log.debug("Creating book with title={}", book.getTitle());

        validateAndAttachRelations(book);

        Book savedBook = bookRepository.save(book);

        log.info(
                "Book created successfully. id={}, title={}",
                savedBook.getId(),
                savedBook.getTitle()
        );

        return savedBook;
    }

    @Transactional(readOnly = true)
    public Book getById(Long id) {

        log.debug("Searching for book with id={}", id);

        return bookRepository.findById(id)
                .orElseThrow(() -> {

                    log.error("Book with id {} was not found", id);

                    return new ResourceNotFoundException(
                            "Book with id " + id + " was not found"
                    );
                });
    }

    @Transactional(readOnly = true)
    public List<Book> getAll() {

        log.debug("Retrieving all books");

        return bookRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Book> getAll(Pageable pageable) {

        log.debug(
                "Retrieving books page={}, size={}, sort={}",
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort()
        );

        return bookRepository.findAll(pageable);
    }

    public Book update(Long id, Book updatedBook) {

        log.debug("Updating book with id={}", id);

        Book existing = getById(id);

        validateAndAttachRelations(updatedBook);

        existing.setTitle(updatedBook.getTitle());
        existing.setPrice(updatedBook.getPrice());
        existing.setStock(updatedBook.getStock());
        existing.setPublisher(updatedBook.getPublisher());
        existing.setAuthors(updatedBook.getAuthors());
        existing.setCategories(updatedBook.getCategories());

        if (updatedBook.getBookDetails() != null) {
            existing.setBookDetails(updatedBook.getBookDetails());
        }

        Book savedBook = bookRepository.save(existing);

        log.info(
                "Book with id {} was updated successfully",
                id
        );

        return savedBook;
    }

    public void delete(Long id) {

        log.debug("Deleting book with id={}", id);

        Book book = getById(id);

        if (!book.getLoans().isEmpty()) {

            log.error(
                    "Cannot delete book with id {} because it has loan history",
                    id
            );

            throw new InvalidOperationException(
                    "Book cannot be deleted because it has loan history"
            );
        }

        bookRepository.delete(book);

        log.info(
                "Book with id {} was deleted successfully",
                id
        );
    }

    private void validateAndAttachRelations(Book book) {

        log.debug(
                "Validating relations for book title={}",
                book.getTitle()
        );

        if (book.getBookDetails() != null &&
                book.getBookDetails().getId() != null) {

            Long bookDetailsId = book.getBookDetails().getId();

            BookDetails bookDetails =
                    bookDetailsRepository
                            .findById(bookDetailsId)
                            .orElseThrow(() -> {

                                log.error(
                                        "BookDetails with id {} was not found while validating book",
                                        bookDetailsId
                                );

                                return new ResourceNotFoundException(
                                        "BookDetails with id " +
                                                bookDetailsId +
                                                " was not found"
                                );
                            });

            book.setBookDetails(bookDetails);
        }

        if (book.getPublisher() != null &&
                book.getPublisher().getId() != null) {

            Long publisherId = book.getPublisher().getId();

            Publisher publisher =
                    publisherRepository
                            .findById(publisherId)
                            .orElseThrow(() -> {

                                log.error(
                                        "Publisher with id {} was not found while validating book",
                                        publisherId
                                );

                                return new ResourceNotFoundException(
                                        "Publisher with id " +
                                                publisherId +
                                                " was not found"
                                );
                            });

            book.setPublisher(publisher);
        }

        if (book.getAuthors() != null) {

            Set<Author> authors = new HashSet<>();

            for (Author author : book.getAuthors()) {

                if (author.getId() == null) {

                    log.error(
                            "Cannot assign author without id to book title={}",
                            book.getTitle()
                    );

                    throw new InvalidOperationException(
                            "Author id is required when assigning an author to a book"
                    );
                }

                Long authorId = author.getId();

                Author existingAuthor =
                        authorRepository.findById(authorId)
                                .orElseThrow(() -> {

                                    log.error(
                                            "Author with id {} was not found while validating book",
                                            authorId
                                    );

                                    return new ResourceNotFoundException(
                                            "Author with id " +
                                                    authorId +
                                                    " was not found"
                                    );
                                });

                authors.add(existingAuthor);
            }

            book.setAuthors(authors);
        }

        if (book.getCategories() != null) {

            Set<Category> categories = new HashSet<>();

            for (Category category : book.getCategories()) {

                if (category.getId() == null) {

                    log.error(
                            "Cannot assign category without id to book title={}",
                            book.getTitle()
                    );

                    throw new InvalidOperationException(
                            "Category id is required when assigning a category to a book"
                    );
                }

                Long categoryId = category.getId();

                Category existingCategory =
                        categoryRepository.findById(categoryId)
                                .orElseThrow(() -> {

                                    log.error(
                                            "Category with id {} was not found while validating book",
                                            categoryId
                                    );

                                    return new ResourceNotFoundException(
                                            "Category with id " +
                                                    categoryId +
                                                    " was not found"
                                    );
                                });

                categories.add(existingCategory);
            }

            book.setCategories(categories);
        }
    }
}