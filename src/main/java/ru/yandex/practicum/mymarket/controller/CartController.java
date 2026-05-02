package ru.yandex.practicum.mymarket.controller;

import ru.yandex.practicum.mymarket.model.ChangeAction;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String getCart(Model model) {
        model.addAttribute("items", cartService.getCartItems());
        model.addAttribute("total", cartService.getTotalSum());
        return "cart";
    }

    @PostMapping("/cart/items")
    public String changeCartItem(
            @RequestParam long id,
            @RequestParam ChangeAction action,
            Model model
    ) {
        cartService.changeItemCount(id, action);
        model.addAttribute("items", cartService.getCartItems());
        model.addAttribute("total", cartService.getTotalSum());
        return "cart";
    }

    @PostMapping("/buy")
    public String buy() {
        long orderId = orderService.placeOrder();
        return "redirect:/orders/" + orderId + "?newOrder=true";
    }
}
