package ru.yandex.practicum.mymarket.controller;

import ru.yandex.practicum.mymarket.dto.OrderItemView;
import ru.yandex.practicum.mymarket.dto.OrderView;
import ru.yandex.practicum.mymarket.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(OrderController.class)
class OrderControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    void getOrdersReturnsOrdersView() throws Exception {
        when(orderService.getOrders()).thenReturn(List.of());

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"));
    }

    @Test
    void getOrderReturnsOrderView() throws Exception {
        OrderView order = new OrderView(
                1L,
                List.of(new OrderItemView(10L, "Item", 100, 2)),
                200L
        );
        when(orderService.getOrderById(1L)).thenReturn(order);

        mockMvc.perform(get("/orders/1").param("newOrder", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"));
    }
}
