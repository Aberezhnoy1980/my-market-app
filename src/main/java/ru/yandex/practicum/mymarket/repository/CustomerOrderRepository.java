package ru.yandex.practicum.mymarket.repository;

import ru.yandex.practicum.mymarket.model.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
}
