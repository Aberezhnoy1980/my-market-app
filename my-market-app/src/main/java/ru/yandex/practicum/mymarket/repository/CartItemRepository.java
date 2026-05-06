package ru.yandex.practicum.mymarket.repository;

import ru.yandex.practicum.mymarket.model.CartItem;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CartItemRepository extends ReactiveCrudRepository<CartItem, Long> {

    Flux<CartItem> findAllByUserId(Long userId);

    Mono<CartItem> findByUserIdAndItemId(Long userId, Long itemId);

    @Modifying
    @Query("DELETE FROM cart_items WHERE user_id = :userId AND item_id = :itemId")
    Mono<Long> deleteByUserIdAndItemId(Long userId, Long itemId);

    @Modifying
    @Query("DELETE FROM cart_items WHERE user_id = :userId")
    Mono<Long> deleteAllByUserId(Long userId);
}
