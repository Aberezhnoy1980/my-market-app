package ru.yandex.practicum.mymarket.repository;

import ru.yandex.practicum.mymarket.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
