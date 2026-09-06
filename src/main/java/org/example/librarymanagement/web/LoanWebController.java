package org.example.librarymanagement.web;

import lombok.RequiredArgsConstructor;
import org.example.librarymanagement.service.BookService;
import org.example.librarymanagement.service.LoanService;
import org.example.librarymanagement.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/loans")
@RequiredArgsConstructor
public class LoanWebController {

    private final LoanService loanService;
    private final UserService userService;
    private final BookService bookService;

    @GetMapping
    public String listLoans(Model model) {
        model.addAttribute("loans", loanService.getAll());
        return "loans/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("users", userService.getAll());
        model.addAttribute("books", bookService.getAll());
        return "loans/form";
    }

    @PostMapping
    public String createLoan(
            @RequestParam Long userId,
            @RequestParam Long bookId) {

        loanService.create(userId, bookId);
        return "redirect:/loans";
    }

    @PostMapping("/{id}/return")
    public String returnBook(@PathVariable Long id) {
        loanService.returnBook(id);
        return "redirect:/loans";
    }

    @PostMapping("/{id}/delete")
    public String deleteLoan(@PathVariable Long id) {
        loanService.delete(id);
        return "redirect:/loans";
    }
}