package ru.yandex.practicum.mymarket.repository;

import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.SortType;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ItemQueryRepository {

    Mono<Long> countBySearch(String search);

    Flux<Item> findItems(String search, SortType sortType, int offset, int limit);
}
