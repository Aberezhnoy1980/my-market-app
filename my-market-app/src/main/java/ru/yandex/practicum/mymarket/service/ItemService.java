package ru.yandex.practicum.mymarket.service;

import ru.yandex.practicum.mymarket.dto.ItemView;
import ru.yandex.practicum.mymarket.dto.ItemsPageView;
import ru.yandex.practicum.mymarket.dto.PagingView;
import ru.yandex.practicum.mymarket.exception.ItemNotFoundException;
import ru.yandex.practicum.mymarket.mapper.ItemViewMapper;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.SortType;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ItemService {

    private static final int CARD_COLUMNS = 3;
    private static final int PLACEHOLDER_ID = -1;

    private final CartItemRepository cartItemRepository;
    private final ItemViewMapper itemViewMapper;
    private final ItemCatalogService itemCatalogService;

    public ItemService(
            CartItemRepository cartItemRepository,
            ItemViewMapper itemViewMapper,
            ItemCatalogService itemCatalogService
    ) {
        this.cartItemRepository = cartItemRepository;
        this.itemViewMapper = itemViewMapper;
        this.itemCatalogService = itemCatalogService;
    }

    public Mono<ItemsPageView> getItemsPage(String search, SortType sortType, int pageNumber, int pageSize) {
        int normalizedPage = Math.max(pageNumber, 1);
        int normalizedPageSize = Math.max(pageSize, 1);
        int offset = (normalizedPage - 1) * normalizedPageSize;
        String normalizedSearch = normalizeSearch(search);

        Mono<Map<Long, Integer>> countsMono = cartItemRepository.findAll()
                .collectList()
                .map(list -> list.stream().collect(Collectors.toMap(CartItem::getItemId, CartItem::getCount, (a, b) -> b)));

        Mono<List<Item>> pageItemsMono = itemCatalogService.getAllItems()
                .map(items -> applySearchSortAndPage(items, normalizedSearch, sortType, offset, normalizedPageSize));

        Mono<Long> totalMono = itemCatalogService.getAllItems()
                .map(items -> applySearch(items, normalizedSearch).size())
                .map(Integer::longValue);

        return Mono.zip(countsMono.defaultIfEmpty(Map.of()), totalMono, pageItemsMono)
                .map(tuple -> {
                    Map<Long, Integer> counts = tuple.getT1();
                    long total = tuple.getT2();
                    List<Item> items = tuple.getT3();

                    List<ItemView> itemViews = items.stream()
                            .map(item -> itemViewMapper.toView(item, counts.getOrDefault(item.getId(), 0)))
                            .toList();

                    boolean hasPrevious = normalizedPage > 1;
                    boolean hasNext = (long) offset + items.size() < total;

                    return new ItemsPageView(
                            toRowsWithPlaceholders(itemViews),
                            new PagingView(normalizedPageSize, normalizedPage, hasPrevious, hasNext)
                    );
                });
    }

    public Mono<ItemView> getItemById(long id) {
        Mono<Item> itemMono = itemCatalogService.getItem(id)
                .switchIfEmpty(Mono.error(new ItemNotFoundException(id)));
        Mono<Integer> countMono = cartItemRepository.findByItemId(id)
                .map(CartItem::getCount)
                .defaultIfEmpty(0);
        return Mono.zip(itemMono, countMono)
                .map(t -> itemViewMapper.toView(t.getT1(), t.getT2()));
    }

    private List<Item> applySearchSortAndPage(List<Item> items, String search, SortType sortType, int offset, int limit) {
        List<Item> filtered = applySearch(items, search);
        filtered.sort(comparatorFor(sortType));
        if (offset >= filtered.size()) {
            return List.of();
        }
        int toIndex = Math.min(offset + limit, filtered.size());
        return filtered.subList(offset, toIndex);
    }

    private List<Item> applySearch(List<Item> items, String search) {
        if (search.isEmpty()) {
            return new ArrayList<>(items);
        }
        return items.stream()
                .filter(item -> {
                    String title = safeLower(item.getTitle());
                    String description = safeLower(item.getDescription());
                    return title.contains(search) || description.contains(search);
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private Comparator<Item> comparatorFor(SortType sortType) {
        if (sortType == SortType.ALPHA) {
            return Comparator.comparing((Item i) -> safeLower(i.getTitle()))
                    .thenComparing(Item::getId);
        }
        if (sortType == SortType.PRICE) {
            return Comparator.comparing(Item::getPrice)
                    .thenComparing(Item::getId);
        }
        return Comparator.comparing(Item::getId);
    }

    private String normalizeSearch(String search) {
        if (search == null) {
            return "";
        }
        return search.trim().toLowerCase(Locale.ROOT);
    }

    private String safeLower(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT);
    }

    private List<List<ItemView>> toRowsWithPlaceholders(List<ItemView> items) {
        List<List<ItemView>> rows = new ArrayList<>();
        for (int i = 0; i < items.size(); i += CARD_COLUMNS) {
            List<ItemView> row = new ArrayList<>(items.subList(i, Math.min(i + CARD_COLUMNS, items.size())));
            while (row.size() < CARD_COLUMNS) {
                row.add(placeholderItem());
            }
            rows.add(row);
        }
        return rows;
    }

    private ItemView placeholderItem() {
        return new ItemView(PLACEHOLDER_ID, "", "", "", BigDecimal.ZERO, 0);
    }
}
