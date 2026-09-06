package org.example.librarymanagement.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.librarymanagement.entity.Book;
import org.example.librarymanagement.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping
    public ResponseEntity<Book> create(
            @Valid @RequestBody Book book) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bookService.create(book));
    }

    @GetMapping
    public List<Book> getAll() {
        return bookService.getAll();
    }

    @GetMapping("/{id}")
    public Book getById(@PathVariable Long id) {
        return bookService.getById(id);
    }

    @PutMapping("/{id}")
    public Book update(
            @PathVariable Long id,
            @Valid @RequestBody Book book) {

        return bookService.update(id, book);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        bookService.delete(id);

        return ResponseEntity.noContent().build();
    }
}