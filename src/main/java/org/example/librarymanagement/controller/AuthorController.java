package org.example.librarymanagement.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.librarymanagement.entity.Author;
import org.example.librarymanagement.service.AuthorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    @PostMapping
    public ResponseEntity<Author> create(
            @Valid @RequestBody Author author) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authorService.create(author));
    }

    @GetMapping
    public List<Author> getAll() {
        return authorService.getAll();
    }

    @GetMapping("/{id}")
    public Author getById(@PathVariable Long id) {
        return authorService.getById(id);
    }

    @PutMapping("/{id}")
    public Author update(
            @PathVariable Long id,
            @Valid @RequestBody Author author) {

        return authorService.update(id, author);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        authorService.delete(id);

        return ResponseEntity.noContent().build();
    }
}