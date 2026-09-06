package org.example.librarymanagement.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.librarymanagement.entity.BookDetails;
import org.example.librarymanagement.service.BookDetailsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/book-details")
@RequiredArgsConstructor
public class BookDetailsController {

    private final BookDetailsService bookDetailsService;

    @PostMapping
    public ResponseEntity<BookDetails> create(
            @Valid @RequestBody BookDetails details) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bookDetailsService.create(details));
    }

    @GetMapping
    public List<BookDetails> getAll() {
        return bookDetailsService.getAll();
    }

    @GetMapping("/{id}")
    public BookDetails getById(@PathVariable Long id) {
        return bookDetailsService.getById(id);
    }

    @PutMapping("/{id}")
    public BookDetails update(
            @PathVariable Long id,
            @Valid @RequestBody BookDetails details) {

        return bookDetailsService.update(id, details);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        bookDetailsService.delete(id);

        return ResponseEntity.noContent().build();
    }
}