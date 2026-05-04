package ru.yandex.practicum.mymarket.repository;

import ru.yandex.practicum.mymarket.model.OrderItem;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;

public interface OrderItemRepository extends ReactiveCrudRepository<OrderItem, Long> {

    Flux<OrderItem> findByOrderId(Long orderId);
}
