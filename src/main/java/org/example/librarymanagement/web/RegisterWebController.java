package org.example.librarymanagement.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.librarymanagement.entity.Role;
import org.example.librarymanagement.entity.User;
import org.example.librarymanagement.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class RegisterWebController {

    private final UserService userService;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {

        User user = new User();
        user.setRole(Role.USER);

        model.addAttribute("user", user);

        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("user") User user,
            BindingResult bindingResult,
            Model model) {

        user.setRole(Role.USER);

        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            userService.create(user);
        } catch (RuntimeException exception) {

            model.addAttribute(
                    "registerError",
                    exception.getMessage()
            );

            return "register";
        }

        return "redirect:/login?registered";
    }
}