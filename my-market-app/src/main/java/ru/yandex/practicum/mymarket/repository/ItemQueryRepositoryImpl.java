package ru.yandex.practicum.mymarket.repository;

import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.SortType;

import java.math.BigDecimal;
import java.util.Locale;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class ItemQueryRepositoryImpl implements ItemQueryRepository {

    private final DatabaseClient databaseClient;

    public ItemQueryRepositoryImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Long> countBySearch(String search) {
        String normalized = normalize(search);
        if (normalized.isEmpty()) {
            return databaseClient.sql("SELECT COUNT(*) FROM items")
                    .map((row, meta) -> row.get(0, Long.class))
                    .one();
        }
        String pattern = "%" + normalized + "%";
        return databaseClient.sql("""
                        SELECT COUNT(*) FROM items
                        WHERE LOWER(title) LIKE LOWER(:pattern)
                           OR LOWER(description) LIKE LOWER(:pattern)
                        """)
                .bind("pattern", pattern)
                .map((row, meta) -> row.get(0, Long.class))
                .one();
    }

    @Override
    public Flux<Long> findItemIds(String search, SortType sortType, int offset, int limit) {
        String orderBy = orderByClause(sortType);
        String normalized = normalize(search);
        if (normalized.isEmpty()) {
            return databaseClient.sql("SELECT id FROM items ORDER BY " + orderBy + " LIMIT :limit OFFSET :offset")
                    .bind("limit", limit)
                    .bind("offset", offset)
                    .map((row, meta) -> row.get("id", Long.class))
                    .all();
        }
        String pattern = "%" + normalized + "%";
        return databaseClient.sql("""
                        SELECT id FROM items
                        WHERE LOWER(title) LIKE LOWER(:pattern)
                           OR LOWER(description) LIKE LOWER(:pattern)
                        ORDER BY """ + orderBy + " LIMIT :limit OFFSET :offset")
                .bind("pattern", pattern)
                .bind("limit", limit)
                .bind("offset", offset)
                .map((row, meta) -> row.get("id", Long.class))
                .all();
    }

    @Override
    public Flux<Item> findItems(String search, SortType sortType, int offset, int limit) {
        String orderBy = orderByClause(sortType);
        String normalized = normalize(search);
        if (normalized.isEmpty()) {
            return databaseClient.sql("SELECT * FROM items ORDER BY " + orderBy + " LIMIT :limit OFFSET :offset")
                    .bind("limit", limit)
                    .bind("offset", offset)
                    .map(this::mapRowToItem)
                    .all();
        }
        String pattern = "%" + normalized + "%";
        return databaseClient.sql("""
                        SELECT * FROM items
                        WHERE LOWER(title) LIKE LOWER(:pattern)
                           OR LOWER(description) LIKE LOWER(:pattern)
                        ORDER BY """ + orderBy + " LIMIT :limit OFFSET :offset")
                .bind("pattern", pattern)
                .bind("limit", limit)
                .bind("offset", offset)
                .map(this::mapRowToItem)
                .all();
    }

    private String orderByClause(SortType sortType) {
        if (sortType == null || sortType == SortType.NO) {
            return "id ASC";
        }
        if (sortType == SortType.ALPHA) {
            return "title ASC, id ASC";
        }
        return "price ASC, id ASC";
    }

    private String normalize(String search) {
        if (search == null) {
            return "";
        }
        return search.trim().toLowerCase(Locale.ROOT);
    }

    private Item mapRowToItem(io.r2dbc.spi.Row row, io.r2dbc.spi.RowMetadata meta) {
        Item item = new Item();
        item.setId(row.get("id", Long.class));
        item.setTitle(row.get("title", String.class));
        item.setDescription(row.get("description", String.class));
        item.setImgPath(row.get("img_path", String.class));
        item.setPrice(row.get("price", BigDecimal.class));
        return item;
    }
}
