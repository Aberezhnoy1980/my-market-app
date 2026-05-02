package ru.yandex.practicum.mymarket.controller;

import ru.yandex.practicum.mymarket.dto.OrderItemView;
import ru.yandex.practicum.mymarket.dto.OrderView;
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

import static org.mockito.Mockito.when;
import reactor.core.publisher.Mono;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class OrderControllerWebMvcTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderService orderService;

    @Test
    void getOrdersReturnsOk() {
        when(orderService.getOrders()).thenReturn(Mono.just(List.of()));

        webTestClient.get().uri("/orders")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getOrderReturnsOk() {
        OrderView order = new OrderView(
                1L,
                List.of(new OrderItemView(10L, "Item", new BigDecimal("100"), 2)),
                new BigDecimal("200")
        );
        when(orderService.getOrderById(1L)).thenReturn(Mono.just(order));

        webTestClient.get().uri("/orders/1?newOrder=true")
                .exchange()
                .expectStatus().isOk();
    }
}
