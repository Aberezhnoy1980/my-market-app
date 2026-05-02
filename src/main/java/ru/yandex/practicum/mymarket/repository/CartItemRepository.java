package ru.yandex.practicum.mymarket.repository;

import ru.yandex.practicum.mymarket.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByItemId(Long itemId);

    void deleteByItemId(Long itemId);
}
