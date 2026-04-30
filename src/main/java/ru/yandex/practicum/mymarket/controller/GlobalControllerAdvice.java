package ru.yandex.practicum.mymarket.controller;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler({
            EntityNotFoundException.class,
            IllegalStateException.class,
            MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class
    })
    public String handleBusinessErrors(Exception ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }
}
