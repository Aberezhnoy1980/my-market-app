package ru.yandex.practicum.mymarket.service;

import ru.yandex.practicum.mymarket.dto.ItemView;
import ru.yandex.practicum.mymarket.mapper.ItemViewMapper;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.ChangeAction;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.exception.ItemNotFoundException;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;
    private final ItemViewMapper itemViewMapper;

    public CartService(
            CartItemRepository cartItemRepository,
            ItemRepository itemRepository,
            ItemViewMapper itemViewMapper
    ) {
        this.cartItemRepository = cartItemRepository;
        this.itemRepository = itemRepository;
        this.itemViewMapper = itemViewMapper;
    }

    public Map<Long, Integer> getCountByItemId() {
        return cartItemRepository.findAll().stream()
                .collect(Collectors.toMap(ci -> ci.getItem().getId(), CartItem::getCount));
    }

    public List<ItemView> getCartItems() {
        List<CartItem> cartItems = cartItemRepository.findAll();
        return cartItems.stream()
                .map(ci -> itemViewMapper.toView(ci.getItem(), ci.getCount()))
                .toList();
    }

    public BigDecimal getTotalSum() {
        return cartItemRepository.findAll().stream()
                .map(ci -> ci.getItem().getPrice().multiply(BigDecimal.valueOf(ci.getCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public void changeItemCount(long itemId, ChangeAction action) {
        if (action == ChangeAction.DELETE) {
            cartItemRepository.deleteByItemId(itemId);
            return;
        }

        CartItem cartItem = cartItemRepository.findByItemId(itemId)
                .orElseGet(() -> createNewCartItem(itemId));

        if (action == ChangeAction.PLUS) {
            cartItem.setCount(cartItem.getCount() + 1);
            cartItemRepository.save(cartItem);
            return;
        }

        if (action == ChangeAction.MINUS) {
            int nextCount = cartItem.getCount() - 1;
            if (nextCount <= 0) {
                cartItemRepository.delete(cartItem);
            } else {
                cartItem.setCount(nextCount);
                cartItemRepository.save(cartItem);
            }
        }
    }

    @Transactional
    public void clear() {
        cartItemRepository.deleteAll();
    }

    public List<CartItem> getCartSnapshot() {
        return cartItemRepository.findAll();
    }

    private CartItem createNewCartItem(long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));
        CartItem cartItem = new CartItem();
        cartItem.setItem(item);
        cartItem.setCount(0);
        return cartItem;
    }
}
