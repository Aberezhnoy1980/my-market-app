package ru.yandex.practicum.mymarket.service;

import ru.yandex.practicum.mymarket.dto.CheckoutUiState;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.ChangeAction;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.mapper.ItemViewMapper;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ItemCatalogService itemCatalogService;

    @Spy
    private ItemViewMapper itemViewMapper = new ItemViewMapper();

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private CartService cartService;

    @Test
    void plusOnEmptyCartCreatesLineWithCountOne() {
        Item item = new Item();
        item.setTitle("T");
        item.setDescription("D");
        item.setImgPath("i.png");
        item.setPrice(new BigDecimal("100"));
        item.setId(5L);
        when(itemCatalogService.getItem(5L)).thenReturn(Mono.just(item));
        when(cartItemRepository.findByItemId(5L)).thenReturn(Mono.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(cartService.changeItemCount(5L, ChangeAction.PLUS))
                .verifyComplete();

        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(captor.capture());
        assertThat(captor.getValue().getCount()).isEqualTo(1);
        assertThat(captor.getValue().getItemId()).isEqualTo(5L);
    }

    @Test
    void minusRemovesWhenCountWouldBecomeZero() {
        CartItem cartItem = new CartItem();
        cartItem.setItemId(1L);
        cartItem.setCount(1);
        when(cartItemRepository.findByItemId(1L)).thenReturn(Mono.just(cartItem));
        when(cartItemRepository.delete(cartItem)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.changeItemCount(1L, ChangeAction.MINUS))
                .verifyComplete();

        verify(cartItemRepository).delete(cartItem);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void deleteByIdDelegatesToRepository() {
        when(cartItemRepository.deleteByItemId(2L)).thenReturn(Mono.just(1L));

        StepVerifier.create(cartService.changeItemCount(2L, ChangeAction.DELETE))
                .verifyComplete();

        verify(cartItemRepository).deleteByItemId(2L);
    }

    @Test
    void getCartPageDataBuildsLineTotalsAndSum() {
        Item a = new Item();
        a.setPrice(new BigDecimal("10"));
        a.setId(1L);
        Item b = new Item();
        b.setPrice(new BigDecimal("5"));
        b.setId(2L);
        CartItem c1 = new CartItem();
        c1.setItemId(1L);
        c1.setCount(2);
        CartItem c2 = new CartItem();
        c2.setItemId(2L);
        c2.setCount(3);
        when(cartItemRepository.findAll()).thenReturn(Flux.just(c1, c2));
        when(itemCatalogService.getItem(1L)).thenReturn(Mono.just(a));
        when(itemCatalogService.getItem(2L)).thenReturn(Mono.just(b));
        when(paymentService.describeCheckout(any(BigDecimal.class), anyBoolean()))
                .thenReturn(Mono.just(new CheckoutUiState("999 руб.", true, null)));

        StepVerifier.create(cartService.getCartPageData())
                .expectNextMatches(data -> data.total().compareTo(new BigDecimal("35")) == 0
                        && data.items().size() == 2
                        && data.checkoutEnabled())
                .verifyComplete();
    }
}
