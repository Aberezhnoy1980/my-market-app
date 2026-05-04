package ru.yandex.practicum.mymarket.controller;

import ru.yandex.practicum.mymarket.form.CartItemChangeForm;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;

import reactor.core.publisher.Mono;

@Controller
@RequestMapping
public class CartController {

    private final CartService cartService;
    private final OrderService orderService;

    public CartController(CartService cartService, OrderService orderService) {
        this.cartService = cartService;
        this.orderService = orderService;
    }

    @GetMapping("/cart/items")
    public Mono<Rendering> getCart() {
        return cartService.getCartPageData()
                .map(data -> Rendering.view("cart")
                        .modelAttribute("items", data.items())
                        .modelAttribute("total", data.total())
                        .build());
    }

    @PostMapping("/cart/items")
    public Mono<Rendering> changeCartItem(@ModelAttribute CartItemChangeForm form) {
        return cartService.changeItemCount(form.id(), form.action())
                .then(cartService.getCartPageData())
                .map(data -> Rendering.view("cart")
                        .modelAttribute("items", data.items())
                        .modelAttribute("total", data.total())
                        .build());
    }

    @PostMapping("/buy")
    public Mono<Rendering> buy() {
        return orderService.placeOrder()
                .map(orderId -> Rendering.redirectTo("/orders/" + orderId + "?newOrder=true").build());
    }
}
