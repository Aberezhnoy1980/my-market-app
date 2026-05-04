package ru.yandex.practicum.mymarket.service;

import ru.yandex.practicum.mymarket.cache.ItemRedisCache;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Загрузка товаров с cache-aside в Redis.
 */
@Service
@Transactional(readOnly = true)
public class ItemCatalogService {

	private final ItemRepository itemRepository;
	private final ItemRedisCache itemRedisCache;

	public ItemCatalogService(ItemRepository itemRepository, ItemRedisCache itemRedisCache) {
		this.itemRepository = itemRepository;
		this.itemRedisCache = itemRedisCache;
	}

	public Mono<Item> getItem(long id) {
		return itemRedisCache.get(id)
				.switchIfEmpty(itemRepository.findById(id)
						.flatMap(item -> itemRedisCache.put(item).thenReturn(item)));
	}

	public Mono<List<Item>> getAllItems() {
		return itemRedisCache.getAll()
				.switchIfEmpty(itemRepository.findAll()
						.collectList()
						.flatMap(items -> itemRedisCache.putAll(items).thenReturn(items)));
	}
}
