package ru.yandex.practicum.mymarket.repository;

import ru.yandex.practicum.mymarket.model.CartItem;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Mono;

public interface CartItemRepository extends ReactiveCrudRepository<CartItem, Long> {

    Mono<CartItem> findByItemId(Long itemId);

    @Modifying
    @Query("DELETE FROM cart_items WHERE item_id = :itemId")
    Mono<Long> deleteByItemId(Long itemId);
}
