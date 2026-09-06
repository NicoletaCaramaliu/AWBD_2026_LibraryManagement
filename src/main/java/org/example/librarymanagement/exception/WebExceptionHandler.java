package org.example.librarymanagement.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(basePackages = "org.example.librarymanagement.web")
public class WebExceptionHandler {

    @ExceptionHandler(DuplicateResourceException.class)
    public String handleDuplicate(
            DuplicateResourceException ex,
            Model model) {

        model.addAttribute("errorTitle", "Date duplicate");
        model.addAttribute("errorMessage", ex.getMessage());

        return "error/business-error";
    }

    @ExceptionHandler(InvalidOperationException.class)
    public String handleInvalidOperation(
            InvalidOperationException ex,
            Model model) {

        model.addAttribute("errorTitle", "Operație imposibilă");
        model.addAttribute("errorMessage", ex.getMessage());

        return "error/business-error";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound() {
        return "error/404";
    }
}