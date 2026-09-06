package org.example.librarymanagement.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.librarymanagement.entity.Author;
import org.example.librarymanagement.entity.Book;
import org.example.librarymanagement.entity.Category;
import org.example.librarymanagement.service.AuthorService;
import org.example.librarymanagement.service.BookDetailsService;
import org.example.librarymanagement.service.BookService;
import org.example.librarymanagement.service.CategoryService;
import org.example.librarymanagement.service.PublisherService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookWebController {

    private final BookService bookService;
    private final PublisherService publisherService;
    private final AuthorService authorService;
    private final CategoryService categoryService;
    private final BookDetailsService bookDetailsService;

    @GetMapping
    public String listBooks(Model model) {
        model.addAttribute("books", bookService.getAll());
        return "books/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("book", new Book());
        addFormData(model);
        return "books/form";
    }

    @PostMapping
    public String createBook(
            @Valid @ModelAttribute("book") Book book,
            BindingResult bindingResult,
            @RequestParam(required = false) Set<Long> authorIds,
            @RequestParam(required = false) Set<Long> categoryIds,
            Model model) {

        if (authorIds != null) {
            Set<Author> authors = new HashSet<>();
            for (Long id : authorIds) {
                Author author = new Author();
                author.setId(id);
                authors.add(author);
            }
            book.setAuthors(authors);
        }

        if (categoryIds != null) {
            Set<Category> categories = new HashSet<>();
            for (Long id : categoryIds) {
                Category category = new Category();
                category.setId(id);
                categories.add(category);
            }
            book.setCategories(categories);
        }

        if (bindingResult.hasErrors()) {
            addFormData(model);
            return "books/form";
        }

        bookService.create(book);
        return "redirect:/books";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("book", bookService.getById(id));
        addFormData(model);
        return "books/form";
    }

    @PostMapping("/{id}")
    public String updateBook(
            @PathVariable Long id,
            @Valid @ModelAttribute("book") Book book,
            BindingResult bindingResult,
            @RequestParam(required = false) Set<Long> authorIds,
            @RequestParam(required = false) Set<Long> categoryIds,
            Model model) {

        if (authorIds != null) {
            Set<Author> authors = new HashSet<>();
            for (Long authorId : authorIds) {
                Author author = new Author();
                author.setId(authorId);
                authors.add(author);
            }
            book.setAuthors(authors);
        }

        if (categoryIds != null) {
            Set<Category> categories = new HashSet<>();
            for (Long categoryId : categoryIds) {
                Category category = new Category();
                category.setId(categoryId);
                categories.add(category);
            }
            book.setCategories(categories);
        }

        if (bindingResult.hasErrors()) {
            addFormData(model);
            return "books/form";
        }

        bookService.update(id, book);
        return "redirect:/books";
    }

    @PostMapping("/{id}/delete")
    public String deleteBook(@PathVariable Long id) {
        bookService.delete(id);
        return "redirect:/books";
    }

    private void addFormData(Model model) {
        model.addAttribute("publishers", publisherService.getAll());
        model.addAttribute("authors", authorService.getAll());
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("bookDetailsList", bookDetailsService.getAll());
    }
}