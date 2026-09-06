package org.example.librarymanagement.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/test-500")
    public String test500() {
        throw new RuntimeException("Test error");
    }
}