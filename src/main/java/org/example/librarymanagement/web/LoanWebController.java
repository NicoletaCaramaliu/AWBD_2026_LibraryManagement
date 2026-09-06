package org.example.librarymanagement.web;

import lombok.RequiredArgsConstructor;
import org.example.librarymanagement.entity.Loan;
import org.example.librarymanagement.entity.User;
import org.example.librarymanagement.exception.InvalidOperationException;
import org.example.librarymanagement.service.BookService;
import org.example.librarymanagement.service.LoanService;
import org.example.librarymanagement.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/loans")
@RequiredArgsConstructor
public class LoanWebController {

    private final LoanService loanService;
    private final UserService userService;
    private final BookService bookService;

    @GetMapping
    public String listLoans(
            Model model,
            Authentication authentication) {

        User currentUser =
                userService.getByUsername(authentication.getName());

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));

        List<Loan> loans;

        if (isAdmin) {
            loans = loanService.getAll();
        } else {
            loans = loanService.getAllByUserId(
                    currentUser.getId()
            );
        }

        model.addAttribute("loans", loans);

        return "loans/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {

        model.addAttribute(
                "books",
                bookService.getAll()
        );

        return "loans/form";
    }

    @PostMapping
    public String createLoan(
            @RequestParam Long bookId,
            Authentication authentication) {

        User currentUser =
                userService.getByUsername(
                        authentication.getName()
                );

        loanService.create(
                currentUser.getId(),
                bookId
        );

        return "redirect:/loans";
    }

    @PostMapping("/{id}/return")
    public String returnBook(
            @PathVariable Long id,
            Authentication authentication) {

        Loan loan = loanService.getById(id);

        User currentUser =
                userService.getByUsername(
                        authentication.getName()
                );

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin &&
                !loan.getUser().getId().equals(currentUser.getId())) {

            throw new InvalidOperationException(
                    "Nu poți returna împrumutul altui utilizator"
            );
        }

        loanService.returnBook(id);

        return "redirect:/loans";
    }

    @PostMapping("/{id}/delete")
    public String deleteLoan(@PathVariable Long id) {

        loanService.delete(id);

        return "redirect:/loans";
    }
}