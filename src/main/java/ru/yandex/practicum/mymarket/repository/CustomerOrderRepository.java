package ru.yandex.practicum.mymarket.repository;

import ru.yandex.practicum.mymarket.model.CustomerOrder;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CustomerOrderRepository extends ReactiveCrudRepository<CustomerOrder, Long> {
}
