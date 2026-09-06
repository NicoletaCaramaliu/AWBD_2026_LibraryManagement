package org.example.librarymanagement.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.librarymanagement.entity.Role;
import org.example.librarymanagement.entity.User;
import org.example.librarymanagement.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserWebController {

    private final UserService userService;

    @GetMapping
    public String listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "username") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            Model model) {

        if (!sortBy.equals("id") &&
                !sortBy.equals("username") &&
                !sortBy.equals("email")) {

            sortBy = "username";
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

        Page<User> userPage =
                userService.getAll(pageable);

        model.addAttribute(
                "users",
                userPage.getContent()
        );

        model.addAttribute(
                "userPage",
                userPage
        );

        model.addAttribute(
                "currentPage",
                page
        );

        model.addAttribute(
                "pageSize",
                size
        );

        model.addAttribute(
                "sortBy",
                sortBy
        );

        model.addAttribute(
                "direction",
                direction
        );

        return "users/list";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
                "user",
                userService.getById(id)
        );

        model.addAttribute(
                "roles",
                Role.values()
        );

        return "users/form";
    }

    @PostMapping("/{id}")
    public String updateUser(
            @PathVariable Long id,
            @ModelAttribute("user") User user) {

        userService.updateWithoutPassword(id, user);

        return "redirect:/users";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(
            @PathVariable Long id) {

        userService.delete(id);

        return "redirect:/users";
    }
}