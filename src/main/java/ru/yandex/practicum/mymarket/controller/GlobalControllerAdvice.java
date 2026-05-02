package ru.yandex.practicum.mymarket.controller;

import ru.yandex.practicum.mymarket.exception.EmptyCartException;
import ru.yandex.practicum.mymarket.exception.NotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebInputException;

import reactor.core.publisher.Mono;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler({
            NotFoundException.class,
            EmptyCartException.class,
            WebExchangeBindException.class,
            ServerWebInputException.class,
            IllegalArgumentException.class
    })
    public Mono<Rendering> handleBusinessErrors(Throwable ex) {
        String message = ex.getMessage();
        if (ex instanceof WebExchangeBindException bindEx && bindEx.getMessage() != null) {
            message = bindEx.getMessage();
        }
        return Mono.just(Rendering.view("error")
                .modelAttribute("errorMessage", message != null ? message : "Request error")
                .build());
    }
}
