package ru.yandex.practicum.mymarket.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "items.cache")
public class ItemCacheProperties {

	/**
	 * TTL записей о товаре в Redis.
	 */
	private Duration ttl = Duration.ofMinutes(3);

	public Duration getTtl() {
		return ttl;
	}

	public void setTtl(Duration ttl) {
		this.ttl = ttl;
	}
}
