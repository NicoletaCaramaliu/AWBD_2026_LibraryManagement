package org.example.librarymanagement.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.librarymanagement.entity.BookDetails;
import org.example.librarymanagement.service.BookDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/book-details")
@RequiredArgsConstructor
public class BookDetailsWebController {

    private final BookDetailsService bookDetailsService;

    @GetMapping
    public String listBookDetails(Model model) {
        model.addAttribute("bookDetailsList", bookDetailsService.getAll());
        return "book-details/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("bookDetails", new BookDetails());
        return "book-details/form";
    }

    @PostMapping
    public String createBookDetails(
            @Valid @ModelAttribute("bookDetails") BookDetails bookDetails,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "book-details/form";
        }

        bookDetailsService.create(bookDetails);
        return "redirect:/book-details";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("bookDetails", bookDetailsService.getById(id));
        return "book-details/form";
    }

    @PostMapping("/{id}")
    public String updateBookDetails(
            @PathVariable Long id,
            @Valid @ModelAttribute("bookDetails") BookDetails bookDetails,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "book-details/form";
        }

        bookDetailsService.update(id, bookDetails);
        return "redirect:/book-details";
    }

    @PostMapping("/{id}/delete")
    public String deleteBookDetails(@PathVariable Long id) {
        bookDetailsService.delete(id);
        return "redirect:/book-details";
    }
}