package org.example.librarymanagement.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.librarymanagement.entity.Author;
import org.example.librarymanagement.service.AuthorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/authors")
@RequiredArgsConstructor
public class AuthorWebController {

    private final AuthorService authorService;

    @GetMapping
    public String listAuthors(Model model) {
        model.addAttribute("authors", authorService.getAll());
        return "authors/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("author", new Author());
        return "authors/form";
    }

    @PostMapping
    public String createAuthor(
            @Valid @ModelAttribute("author") Author author,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "authors/form";
        }

        authorService.create(author);
        return "redirect:/authors";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("author", authorService.getById(id));
        return "authors/form";
    }

    @PostMapping("/{id}")
    public String updateAuthor(
            @PathVariable Long id,
            @Valid @ModelAttribute("author") Author author,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "authors/form";
        }

        authorService.update(id, author);
        return "redirect:/authors";
    }

    @PostMapping("/{id}/delete")
    public String deleteAuthor(@PathVariable Long id) {
        authorService.delete(id);
        return "redirect:/authors";
    }
}