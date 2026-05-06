package ru.yandex.practicum.mymarket.controller;

import ru.yandex.practicum.mymarket.config.SecurityConfiguration;
import ru.yandex.practicum.mymarket.dto.CartPageData;
import ru.yandex.practicum.mymarket.model.ChangeAction;
import ru.yandex.practicum.mymarket.repository.AppUserRepository;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import reactor.core.publisher.Mono;

@WebFluxTest(CartController.class)
@Import(SecurityConfiguration.class)
class CartControllerWebFluxTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CartService cartService;

    @MockBean
    private OrderService orderService;

    @MockBean
    private AppUserRepository appUserRepository;

    @Test
    void getCartReturnsOk() {
        when(cartService.getCartPageData(anyString()))
                .thenReturn(Mono.just(new CartPageData(List.of(), BigDecimal.ZERO, "0 руб.", false, null)));

        webTestClient.mutateWith(mockUser()).get().uri("/cart/items")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void postCartItemsReturnsOk() {
        when(cartService.changeItemCount(anyString(), eq(3L), eq(ChangeAction.DELETE))).thenReturn(Mono.empty());
        when(cartService.getCartPageData(anyString()))
                .thenReturn(Mono.just(new CartPageData(List.of(), new BigDecimal("100"), "500 руб.", true, null)));

        webTestClient.mutateWith(mockUser()).mutateWith(csrf()).post().uri("/cart/items?id=3&action=DELETE")
                .exchange()
                .expectStatus().isOk();

        verify(cartService).changeItemCount(anyString(), eq(3L), eq(ChangeAction.DELETE));
    }

    @Test
    void postBuyRedirectsToNewOrder() {
        when(orderService.placeOrder(anyString())).thenReturn(Mono.just(42L));

        webTestClient.mutateWith(mockUser()).mutateWith(csrf()).post().uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/orders/42?newOrder=true");
    }
}
