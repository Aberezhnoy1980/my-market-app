package ru.yandex.practicum.mymarket.cache;

import ru.yandex.practicum.mymarket.MyMarketAppApplication;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.service.ItemCatalogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

/**
 * Проверка, что cache-aside кладёт JSON в Redis (интеграция с Testcontainers Redis).
 */
@SpringBootTest(classes = MyMarketAppApplication.class)
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class ItemRedisCacheIntegrationTest {

	@Container
	static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
			.withExposedPorts(6379);

	@DynamicPropertySource
	static void registerRedis(DynamicPropertyRegistry registry) {
		registry.add("spring.data.redis.host", redis::getHost);
		registry.add("spring.data.redis.port", () -> String.valueOf(redis.getMappedPort(6379)));
	}

	@Autowired
	private ItemCatalogService itemCatalogService;

	@Autowired
	private ItemRedisCache itemRedisCache;

	@Test
	void secondReadUsesCacheLayer() {
		Item item = new Item();
		item.setId(99L);
		item.setTitle("x");
		item.setDescription("y");
		item.setImgPath("z.png");
		item.setPrice(new BigDecimal("1.00"));

		StepVerifier.create(itemRedisCache.put(item).then(itemRedisCache.get(99L)))
				.expectNextMatches(cached ->
						cached.getTitle().equals("x") && cached.getPrice().compareTo(new BigDecimal("1.00")) == 0)
				.verifyComplete();

		StepVerifier.create(itemCatalogService.getItem(99L))
				.expectNextMatches(i -> i.getId().equals(99L))
				.verifyComplete();
	}
}
