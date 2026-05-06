package ru.yandex.practicum.mymarket.repository;

import ru.yandex.practicum.mymarket.model.CustomerOrder;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerOrderRepository extends ReactiveCrudRepository<CustomerOrder, Long> {

    Flux<CustomerOrder> findAllByUserIdOrderByIdDesc(Long userId);

    Mono<CustomerOrder> findByIdAndUserId(Long id, Long userId);
}
