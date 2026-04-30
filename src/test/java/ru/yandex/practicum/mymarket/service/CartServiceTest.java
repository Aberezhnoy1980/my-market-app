package ru.yandex.practicum.mymarket.service;

import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.ChangeAction;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void plusOnEmptyCartCreatesLineWithCountOne() {
        Item item = new Item();
        item.setTitle("T");
        item.setDescription("D");
        item.setImgPath("i.png");
        item.setPrice(100);
        when(itemRepository.findById(5L)).thenReturn(Optional.of(item));
        when(cartItemRepository.findByItemId(5L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

        cartService.changeItemCount(5L, ChangeAction.PLUS);

        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(captor.capture());
        assertThat(captor.getValue().getCount()).isEqualTo(1);
    }

    @Test
    void minusRemovesWhenCountWouldBecomeZero() {
        Item item = new Item();
        CartItem cartItem = new CartItem();
        cartItem.setItem(item);
        cartItem.setCount(1);
        when(cartItemRepository.findByItemId(1L)).thenReturn(Optional.of(cartItem));

        cartService.changeItemCount(1L, ChangeAction.MINUS);

        verify(cartItemRepository).delete(cartItem);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void deleteByIdDelegatesToRepository() {
        cartService.changeItemCount(2L, ChangeAction.DELETE);
        verify(cartItemRepository).deleteByItemId(2L);
    }

    @Test
    void getTotalSumMultipliesPriceByCount() {
        Item a = new Item();
        a.setPrice(10);
        Item b = new Item();
        b.setPrice(5);
        CartItem c1 = new CartItem();
        c1.setItem(a);
        c1.setCount(2);
        CartItem c2 = new CartItem();
        c2.setItem(b);
        c2.setCount(3);
        when(cartItemRepository.findAll()).thenReturn(List.of(c1, c2));

        assertThat(cartService.getTotalSum()).isEqualTo(10L * 2 + 5L * 3);
    }
}
