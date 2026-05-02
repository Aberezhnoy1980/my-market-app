package ru.yandex.practicum.mymarket.controller;

import ru.yandex.practicum.mymarket.model.ChangeAction;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
        return Mono.zip(cartService.getCartItems(), cartService.getTotalSum())
                .map(tuple -> Rendering.view("cart")
                        .modelAttribute("items", tuple.getT1())
                        .modelAttribute("total", tuple.getT2())
                        .build());
    }

    @PostMapping("/cart/items")
    public Mono<Rendering> changeCartItem(
            @RequestParam long id,
            @RequestParam ChangeAction action
    ) {
        return cartService.changeItemCount(id, action)
                .then(Mono.zip(cartService.getCartItems(), cartService.getTotalSum()))
                .map(tuple -> Rendering.view("cart")
                        .modelAttribute("items", tuple.getT1())
                        .modelAttribute("total", tuple.getT2())
                        .build());
    }

    @PostMapping("/buy")
    public Mono<Rendering> buy() {
        return orderService.placeOrder()
                .map(orderId -> Rendering.redirectTo("/orders/" + orderId + "?newOrder=true").build());
    }
}
