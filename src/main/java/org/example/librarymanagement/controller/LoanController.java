package org.example.librarymanagement.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.librarymanagement.entity.Loan;
import org.example.librarymanagement.service.LoanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    public ResponseEntity<Loan> create(
            @RequestParam Long userId,
            @RequestParam Long bookId) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(loanService.create(userId, bookId));
    }

    @GetMapping
    public List<Loan> getAll() {
        return loanService.getAll();
    }

    @GetMapping("/{id}")
    public Loan getById(@PathVariable Long id) {
        return loanService.getById(id);
    }

    @PutMapping("/{id}")
    public Loan update(
            @PathVariable Long id,
            @Valid @RequestBody Loan loan) {

        return loanService.update(id, loan);
    }

    @PutMapping("/{id}/return")
    public Loan returnBook(@PathVariable Long id) {
        return loanService.returnBook(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        loanService.delete(id);

        return ResponseEntity.noContent().build();
    }
}