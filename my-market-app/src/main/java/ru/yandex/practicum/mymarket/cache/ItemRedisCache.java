package ru.yandex.practicum.mymarket.cache;

import ru.yandex.practicum.mymarket.model.Item;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class ItemRedisCache {

	private static final String PREFIX = "mymarket:item:";

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
				.get(key(id))
				.flatMap(json -> Mono.fromCallable(() -> objectMapper.readValue(json, ItemSnapshot.class).toItem()))
				.onErrorResume(e -> Mono.empty());
	}

	public Mono<Void> put(Item item) {
		if (item.getId() == null) {
			return Mono.empty();
		}
		try {
			String json = objectMapper.writeValueAsString(ItemSnapshot.from(item));
			return redis.opsForValue()
					.set(key(item.getId()), json, properties.getTtl())
					.then();
		} catch (JsonProcessingException e) {
			return Mono.error(e);
		}
	}

	private String key(long id) {
		return PREFIX + id;
	}
}
