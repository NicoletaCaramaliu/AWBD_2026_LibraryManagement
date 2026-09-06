package org.example.librarymanagement.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.librarymanagement.entity.Author;
import org.example.librarymanagement.entity.Book;
import org.example.librarymanagement.entity.BookDetails;
import org.example.librarymanagement.entity.Category;
import org.example.librarymanagement.service.AuthorService;
import org.example.librarymanagement.service.BookDetailsService;
import org.example.librarymanagement.service.BookService;
import org.example.librarymanagement.service.CategoryService;
import org.example.librarymanagement.service.PublisherService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    public String listBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            Model model) {

        if (!sortBy.equals("title") && !sortBy.equals("price")) {
            sortBy = "title";
        }

        Sort.Direction sortDirection =
                direction.equalsIgnoreCase("desc")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        Page<Book> bookPage = bookService.getAll(pageable);

        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("bookPage", bookPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);

        return "books/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {

        Book book = new Book();
        book.setBookDetails(new BookDetails());

        model.addAttribute("book", book);

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

        setAuthors(book, authorIds);
        setCategories(book, categoryIds);

        if (bindingResult.hasErrors()) {
            addFormData(model);
            return "books/form";
        }

        BookDetails savedDetails =
                bookDetailsService.create(
                        book.getBookDetails()
                );

        book.setBookDetails(savedDetails);

        bookService.create(book);

        return "redirect:/books";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(
            @PathVariable Long id,
            Model model) {

        Book book = bookService.getById(id);

        model.addAttribute("book", book);

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

        Book existingBook = bookService.getById(id);

        setAuthors(book, authorIds);
        setCategories(book, categoryIds);

        if (bindingResult.hasErrors()) {
            book.setId(id);
            addFormData(model);
            return "books/form";
        }

        BookDetails updatedDetails = book.getBookDetails();

        BookDetails savedDetails =
                bookDetailsService.update(
                        existingBook.getBookDetails().getId(),
                        updatedDetails
                );

        book.setBookDetails(savedDetails);

        bookService.update(id, book);

        return "redirect:/books";
    }

    @PostMapping("/{id}/delete")
    public String deleteBook(@PathVariable Long id) {

        bookService.delete(id);

        return "redirect:/books";
    }

    private void setAuthors(
            Book book,
            Set<Long> authorIds) {

        Set<Author> authors = new HashSet<>();

        if (authorIds != null) {
            for (Long authorId : authorIds) {

                Author author = new Author();
                author.setId(authorId);

                authors.add(author);
            }
        }

        book.setAuthors(authors);
    }

    private void setCategories(
            Book book,
            Set<Long> categoryIds) {

        Set<Category> categories = new HashSet<>();

        if (categoryIds != null) {
            for (Long categoryId : categoryIds) {

                Category category = new Category();
                category.setId(categoryId);

                categories.add(category);
            }
        }

        book.setCategories(categories);
    }

    private void addFormData(Model model) {

        model.addAttribute(
                "publishers",
                publisherService.getAll()
        );

        model.addAttribute(
                "authors",
                authorService.getAll()
        );

        model.addAttribute(
                "categories",
                categoryService.getAll()
        );
    }
}