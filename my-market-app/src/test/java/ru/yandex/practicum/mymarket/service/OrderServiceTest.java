package ru.yandex.practicum.mymarket.service;

import ru.yandex.practicum.mymarket.exception.EmptyCartException;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.CustomerOrder;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.CustomerOrderRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
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

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void placeOrderThrowsWhenCartEmpty() {
        when(cartService.getCartSnapshot()).thenReturn(Mono.just(List.of()));

        StepVerifier.create(orderService.placeOrder())
                .expectError(EmptyCartException.class)
                .verify();
    }

    @Test
    void placeOrderCreatesOrderClearsCart() {
        Item item = new Item();
        item.setId(3L);
        item.setPrice(new BigDecimal("100"));
        CartItem line = new CartItem();
        line.setItemId(3L);
        line.setCount(2);

        when(cartService.getCartSnapshot()).thenReturn(Mono.just(List.of(line)));
        when(itemRepository.findById(3L)).thenReturn(Mono.just(item));

        CustomerOrder persisted = new CustomerOrder();
        persisted.setTotalSum(new BigDecimal("200"));
        persisted.setId(7L);
        when(customerOrderRepository.save(any(CustomerOrder.class))).thenReturn(Mono.just(persisted));
        when(orderItemRepository.saveAll(anyList())).thenReturn(Flux.empty());
        when(cartService.clear()).thenReturn(Mono.empty());
        when(paymentService.chargeOrderAmount(any(BigDecimal.class))).thenReturn(Mono.empty());

        StepVerifier.create(orderService.placeOrder())
                .expectNext(7L)
                .verifyComplete();

        verify(paymentService).chargeOrderAmount(argThat(a -> a.compareTo(new BigDecimal("200")) == 0));
        verify(orderItemRepository).saveAll(anyList());
        verify(cartService).clear();
    }
}
