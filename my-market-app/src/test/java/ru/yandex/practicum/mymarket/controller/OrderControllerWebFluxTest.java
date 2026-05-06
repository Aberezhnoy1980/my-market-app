package ru.yandex.practicum.mymarket.controller;

import ru.yandex.practicum.mymarket.config.SecurityConfiguration;
import ru.yandex.practicum.mymarket.dto.OrderItemView;
import ru.yandex.practicum.mymarket.dto.OrderView;
import ru.yandex.practicum.mymarket.repository.AppUserRepository;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import reactor.core.publisher.Mono;

@WebFluxTest(OrderController.class)
@Import(SecurityConfiguration.class)
class OrderControllerWebFluxTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderService orderService;

    @MockBean
    private AppUserRepository appUserRepository;

    @Test
    void getOrdersReturnsOk() {
        when(orderService.getOrders(anyString())).thenReturn(Mono.just(List.of()));

        webTestClient.mutateWith(mockUser()).get().uri("/orders")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getOrdersRedirectsToLoginForAnonymous() {
        webTestClient.get().uri("/orders")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*/login");
    }

    @Test
    void getOrderReturnsOk() {
        OrderView order = new OrderView(
                1L,
                List.of(new OrderItemView(10L, "Item", new BigDecimal("100"), 2)),
                new BigDecimal("200")
        );
        when(orderService.getOrderById(anyString(), eq(1L))).thenReturn(Mono.just(order));

        webTestClient.mutateWith(mockUser()).get().uri("/orders/1?newOrder=true")
                .exchange()
                .expectStatus().isOk();
    }
}
