package ru.yandex.practicum.mymarket.cache;

import ru.yandex.practicum.mymarket.model.Item;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Component
public class ItemRedisCache {

	private static final String ITEM_PREFIX = "mymarket:item:";
	private static final String ITEMS_ALL_KEY = "mymarket:items:all";

	private final ReactiveStringRedisTemplate redis;
	private final ObjectMapper objectMapper;
	private final ItemCacheProperties properties;

	public ItemRedisCache(
			ReactiveStringRedisTemplate redis,
			ObjectMapper objectMapper,
			ItemCacheProperties properties
	) {
		this.redis = redis;
		this.objectMapper = objectMapper;
		this.properties = properties;
	}

	public Mono<Item> get(long id) {
		return redis.opsForValue()
				.get(itemKey(id))
				.flatMap(json -> Mono.fromCallable(() -> objectMapper.readValue(json, ItemSnapshot.class).toItem()))
				.onErrorResume(e -> Mono.empty());
	}

	public Mono<Void> put(Item item) {
		if (item.getId() == null) {
			return Mono.empty();
		}
		return Mono.fromCallable(() -> objectMapper.writeValueAsString(ItemSnapshot.from(item)))
				.subscribeOn(Schedulers.boundedElastic())
				.flatMap(json -> redis.opsForValue()
						.set(itemKey(item.getId()), json, properties.getTtl()))
				.then();
	}

	public Mono<List<Item>> getAll() {
		return redis.opsForValue()
				.get(ITEMS_ALL_KEY)
				.flatMap(json -> Mono.fromCallable(() -> objectMapper.readValue(
						json,
						new TypeReference<List<ItemSnapshot>>() {
						}
				)))
				.map(list -> list.stream().map(ItemSnapshot::toItem).toList())
				.onErrorResume(e -> Mono.empty());
	}

	public Mono<Void> putAll(List<Item> items) {
		return Mono.fromCallable(() -> objectMapper.writeValueAsString(items.stream().map(ItemSnapshot::from).toList()))
				.subscribeOn(Schedulers.boundedElastic())
				.flatMap(json -> redis.opsForValue()
						.set(ITEMS_ALL_KEY, json, properties.getTtl()))
				.then();
	}

	private String itemKey(long id) {
		return ITEM_PREFIX + id;
	}
}
