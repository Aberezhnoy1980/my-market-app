package ru.yandex.practicum.mymarket.service;

import ru.yandex.practicum.mymarket.dto.ItemView;
import ru.yandex.practicum.mymarket.exception.ItemNotFoundException;
import ru.yandex.practicum.mymarket.mapper.ItemViewMapper;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.ChangeAction;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import reactor.core.publisher.Mono;

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

    public Mono<List<ItemView>> getCartItems() {
        return cartItemRepository.findAll()
                .concatMap(ci -> itemRepository.findById(ci.getItemId())
                        .map(item -> itemViewMapper.toView(item, ci.getCount())))
                .collectList();
    }

    public Mono<BigDecimal> getTotalSum() {
        return cartItemRepository.findAll()
                .concatMap(ci -> itemRepository.findById(ci.getItemId())
                        .map(item -> item.getPrice().multiply(BigDecimal.valueOf(ci.getCount()))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public Mono<Void> changeItemCount(long itemId, ChangeAction action) {
        if (action == ChangeAction.DELETE) {
            return cartItemRepository.deleteByItemId(itemId).then();
        }
        if (action == ChangeAction.PLUS) {
            return plus(itemId);
        }
        if (action == ChangeAction.MINUS) {
            return minus(itemId);
        }
        return Mono.empty();
    }

    private Mono<Void> plus(long itemId) {
        return cartItemRepository.findByItemId(itemId)
                .flatMap(ci -> {
                    ci.setCount(ci.getCount() + 1);
                    return cartItemRepository.save(ci);
                })
                .switchIfEmpty(createLineWithCountOne(itemId))
                .then();
    }

    private Mono<CartItem> createLineWithCountOne(long itemId) {
        return itemRepository.findById(itemId)
                .switchIfEmpty(Mono.error(new ItemNotFoundException(itemId)))
                .flatMap(item -> {
                    CartItem cartItem = new CartItem();
                    cartItem.setItemId(itemId);
                    cartItem.setCount(1);
                    return cartItemRepository.save(cartItem);
                });
    }

    private Mono<Void> minus(long itemId) {
        return cartItemRepository.findByItemId(itemId)
                .flatMap(ci -> {
                    int nextCount = ci.getCount() - 1;
                    if (nextCount <= 0) {
                        return cartItemRepository.delete(ci);
                    }
                    ci.setCount(nextCount);
                    return cartItemRepository.save(ci);
                })
                .then();
    }

    @Transactional
    public Mono<Void> clear() {
        return cartItemRepository.deleteAll();
    }

    public Mono<List<CartItem>> getCartSnapshot() {
        return cartItemRepository.findAll().collectList();
    }
}
