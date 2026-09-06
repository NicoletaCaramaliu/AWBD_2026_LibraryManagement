package org.example.librarymanagement.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.librarymanagement.entity.Publisher;
import org.example.librarymanagement.service.PublisherService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/publishers")
@RequiredArgsConstructor
public class PublisherWebController {

    private final PublisherService publisherService;

    @GetMapping
    public String listPublishers(Model model) {
        model.addAttribute("publishers", publisherService.getAll());
        return "publishers/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("publisher", new Publisher());
        return "publishers/form";
    }

    @PostMapping
    public String createPublisher(
            @Valid @ModelAttribute("publisher") Publisher publisher,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "publishers/form";
        }

        publisherService.create(publisher);
        return "redirect:/publishers";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("publisher", publisherService.getById(id));
        return "publishers/form";
    }

    @PostMapping("/{id}")
    public String updatePublisher(
            @PathVariable Long id,
            @Valid @ModelAttribute("publisher") Publisher publisher,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "publishers/form";
        }

        publisherService.update(id, publisher);
        return "redirect:/publishers";
    }

    @PostMapping("/{id}/delete")
    public String deletePublisher(@PathVariable Long id) {
        publisherService.delete(id);
        return "redirect:/publishers";
    }
}