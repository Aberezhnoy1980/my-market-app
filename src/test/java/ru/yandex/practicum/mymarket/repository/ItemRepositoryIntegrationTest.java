package ru.yandex.practicum.mymarket.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

/**
 * Полный контекст приложения против одного PostgreSQL: JDBC (Liquibase) и R2DBC на одной БД.
 * Свойства из контейнера — через {@link DynamicPropertySource}. Явно продублированы URL для Hikari и
 * Liquibase, чтобы не остаться на дефолтном localhost из {@code application.properties} на CI.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "spring.application.name=item-repository-integration-it")
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class ItemRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        postgres.start();
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        // Hikari часто читает jdbc-url напрямую — без этого Liquibase мог уехать на другой инстанс.
        registry.add("spring.datasource.hikari.jdbc-url", postgres::getJdbcUrl);
        registry.add("spring.datasource.hikari.username", postgres::getUsername);
        registry.add("spring.datasource.hikari.password", postgres::getPassword);
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.liquibase.url", postgres::getJdbcUrl);
        registry.add("spring.liquibase.user", postgres::getUsername);
        registry.add("spring.liquibase.password", postgres::getPassword);
        registry.add("spring.r2dbc.url", () -> toR2dbcUrl(postgres.getJdbcUrl()));
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
        registry.add("spring.r2dbc.pool.enabled", () -> false);
    }

    /** jdbc:postgresql://... → r2dbc:postgresql://... */
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
