package ru.yandex.practicum.mymarket.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

/**
 * Полный контекст приложения против одного PostgreSQL: JDBC (Liquibase) и R2DBC указывают на один инстанс.
 * Свойства берутся из контейнера через {@link DynamicPropertySource} (выше приоритета, чем {@code application-test} с H2).
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class ItemRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        postgres.start();
        // Один источник правды: R2DBC URL выводим из JDBC URL контейнера, чтобы host/port/database
        // совпадали с тем, куда Liquibase катает миграции (иначе 42P01 на CI).
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.r2dbc.url", () -> toR2dbcUrl(postgres.getJdbcUrl()));
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
        registry.add("spring.r2dbc.pool.enabled", () -> false);
    }

    /** jdbc:postgresql://... → r2dbc:postgresql://... (query string отбрасываем — не все параметры JDBC нужны R2DBC). */
    private static String toR2dbcUrl(String jdbcUrl) {
        if (!jdbcUrl.startsWith("jdbc:postgresql://")) {
            throw new IllegalStateException("Expected PostgreSQL JDBC URL, got: " + jdbcUrl);
        }
        String tail = jdbcUrl.substring("jdbc:".length());
        String pathAndHost = tail.split("\\?", 2)[0];
        return "r2dbc:" + pathAndHost;
    }

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void liquibaseSeedCreatesThreeItems() {
        StepVerifier.create(itemRepository.findAll().count())
                .expectNext(3L)
                .verifyComplete();
    }
}
