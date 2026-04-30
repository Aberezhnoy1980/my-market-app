package ru.yandex.practicum.mymarket.controller;

import ru.yandex.practicum.mymarket.model.ChangeAction;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(CartController.class)
class CartControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @MockBean
    private OrderService orderService;

    @Test
    void getCartReturnsCartView() throws Exception {
        when(cartService.getCartItems()).thenReturn(List.of());
        when(cartService.getTotalSum()).thenReturn(0L);

        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));
    }

    @Test
    void postCartItemsReturnsCartView() throws Exception {
        when(cartService.getCartItems()).thenReturn(List.of());
        when(cartService.getTotalSum()).thenReturn(100L);

        mockMvc.perform(post("/cart/items")
                        .param("id", "3")
                        .param("action", "DELETE"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));

        verify(cartService).changeItemCount(3L, ChangeAction.DELETE);
    }

    @Test
    void postBuyRedirectsToNewOrder() throws Exception {
        when(orderService.placeOrder()).thenReturn(42L);

        mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/42?newOrder=true"));
    }
}
