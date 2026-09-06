package org.example.librarymanagement.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.librarymanagement.entity.Publisher;
import org.example.librarymanagement.service.PublisherService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/publishers")
@RequiredArgsConstructor
public class PublisherController {

    private final PublisherService publisherService;

    @PostMapping
    public ResponseEntity<Publisher> create(
            @Valid @RequestBody Publisher publisher) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(publisherService.create(publisher));
    }

    @GetMapping
    public List<Publisher> getAll() {
        return publisherService.getAll();
    }

    @GetMapping("/{id}")
    public Publisher getById(@PathVariable Long id) {
        return publisherService.getById(id);
    }

    @PutMapping("/{id}")
    public Publisher update(
            @PathVariable Long id,
            @Valid @RequestBody Publisher publisher) {

        return publisherService.update(id, publisher);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        publisherService.delete(id);

        return ResponseEntity.noContent().build();
    }
}