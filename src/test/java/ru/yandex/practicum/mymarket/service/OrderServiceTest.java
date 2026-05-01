package ru.yandex.practicum.mymarket.service;

import ru.yandex.practicum.mymarket.exception.EmptyCartException;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.CustomerOrder;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.CustomerOrderRepository;
import ru.yandex.practicum.mymarket.repository.OrderItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private CartService cartService;

    @Mock
    private CustomerOrderRepository customerOrderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void placeOrderThrowsWhenCartEmpty() {
        when(cartService.getCartSnapshot()).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.placeOrder())
                .isInstanceOf(EmptyCartException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void placeOrderCreatesOrderClearsCart() {
        Item item = new Item();
        item.setPrice(100);
        CartItem line = new CartItem();
        line.setItem(item);
        line.setCount(2);

        when(cartService.getCartSnapshot()).thenReturn(List.of(line));

        CustomerOrder persisted = new CustomerOrder();
        persisted.setTotalSum(200);
        ReflectionTestUtils.setField(persisted, "id", 7L);
        when(customerOrderRepository.save(any(CustomerOrder.class))).thenReturn(persisted);

        long orderId = orderService.placeOrder();

        assertThat(orderId).isEqualTo(7L);
        verify(orderItemRepository).saveAll(anyList());
        verify(cartService).clear();
    }
}
