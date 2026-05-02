package ru.yandex.practicum.mymarket.controller;

import ru.yandex.practicum.mymarket.exception.EmptyCartException;
import ru.yandex.practicum.mymarket.exception.NotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler({
            NotFoundException.class,
            EmptyCartException.class,
            MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class
    })
    public String handleBusinessErrors(Exception ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }
}
