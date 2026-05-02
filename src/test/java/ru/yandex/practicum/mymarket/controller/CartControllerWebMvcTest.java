package ru.yandex.practicum.mymarket.controller;

import ru.yandex.practicum.mymarket.model.ChangeAction;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import reactor.core.publisher.Mono;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class CartControllerWebMvcTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CartService cartService;

    @MockBean
    private OrderService orderService;

    @Test
    void getCartReturnsOk() {
        when(cartService.getCartItems()).thenReturn(Mono.just(List.of()));
        when(cartService.getTotalSum()).thenReturn(Mono.just(BigDecimal.ZERO));

        webTestClient.get().uri("/cart/items")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void postCartItemsReturnsOk() {
        when(cartService.changeItemCount(3L, ChangeAction.DELETE)).thenReturn(Mono.empty());
        when(cartService.getCartItems()).thenReturn(Mono.just(List.of()));
        when(cartService.getTotalSum()).thenReturn(Mono.just(new BigDecimal("100")));

        webTestClient.post().uri("/cart/items?id=3&action=DELETE")
                .exchange()
                .expectStatus().isOk();

        verify(cartService).changeItemCount(3L, ChangeAction.DELETE);
    }

    @Test
    void postBuyRedirectsToNewOrder() {
        when(orderService.placeOrder()).thenReturn(Mono.just(42L));

        webTestClient.post().uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/orders/42?newOrder=true");
    }
}
