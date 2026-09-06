package org.example.librarymanagement.service;

import lombok.RequiredArgsConstructor;
import org.example.librarymanagement.entity.*;
import org.example.librarymanagement.exception.InvalidOperationException;
import org.example.librarymanagement.exception.ResourceNotFoundException;
import org.example.librarymanagement.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class BookService {

    private final BookRepository bookRepository;
    private final PublisherRepository publisherRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final BookDetailsRepository bookDetailsRepository;

    public Book create(Book book) {

        validateAndAttachRelations(book);

        return bookRepository.save(book);
    }

    @Transactional(readOnly = true)
    public Book getById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book with id " + id + " was not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<Book> getAll() {
        return bookRepository.findAll();
    }

    public Book update(Long id, Book updatedBook) {

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

        return bookRepository.save(existing);
    }

    public void delete(Long id) {

        Book book = getById(id);

        if (!book.getLoans().isEmpty()) {
            throw new InvalidOperationException(
                    "Book cannot be deleted because it has loan history"
            );
        }

        bookRepository.delete(book);
    }

    private void validateAndAttachRelations(Book book) {

        if (book.getBookDetails() != null &&
                book.getBookDetails().getId() != null) {

            Long bookDetailsId = book.getBookDetails().getId();

            BookDetails bookDetails =
                    bookDetailsRepository
                            .findById(bookDetailsId)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "BookDetails with id " +
                                                    bookDetailsId +
                                                    " was not found"
                                    )
                            );

            book.setBookDetails(bookDetails);
        }

        if (book.getPublisher() != null &&
                book.getPublisher().getId() != null) {

            Publisher publisher =
                    publisherRepository
                            .findById(book.getPublisher().getId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Publisher with id " +
                                                    book.getPublisher().getId() +
                                                    " was not found"
                                    )
                            );

            book.setPublisher(publisher);
        }

        if (book.getAuthors() != null) {

            Set<Author> authors = new HashSet<>();

            for (Author author : book.getAuthors()) {

                if (author.getId() == null) {
                    throw new InvalidOperationException(
                            "Author id is required when assigning an author to a book"
                    );
                }

                Author existingAuthor =
                        authorRepository.findById(author.getId())
                                .orElseThrow(() ->
                                        new ResourceNotFoundException(
                                                "Author with id " +
                                                        author.getId() +
                                                        " was not found"
                                        )
                                );

                authors.add(existingAuthor);
            }

            book.setAuthors(authors);
        }

        if (book.getCategories() != null) {

            Set<Category> categories = new HashSet<>();

            for (Category category : book.getCategories()) {

                if (category.getId() == null) {
                    throw new InvalidOperationException(
                            "Category id is required when assigning a category to a book"
                    );
                }

                Category existingCategory =
                        categoryRepository.findById(category.getId())
                                .orElseThrow(() ->
                                        new ResourceNotFoundException(
                                                "Category with id " +
                                                        category.getId() +
                                                        " was not found"
                                        )
                                );

                categories.add(existingCategory);
            }

            book.setCategories(categories);
        }
    }
}