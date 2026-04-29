package ru.yandex.practicum.mymarket.service;

import ru.yandex.practicum.mymarket.dto.ItemView;
import ru.yandex.practicum.mymarket.dto.ItemsPageView;
import ru.yandex.practicum.mymarket.dto.PagingView;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.SortType;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ItemService {

    private static final int CARD_COLUMNS = 3;
    private static final int PLACEHOLDER_ID = -1;

    private final ItemRepository itemRepository;
    private final CartItemRepository cartItemRepository;

    public ItemService(ItemRepository itemRepository, CartItemRepository cartItemRepository) {
        this.itemRepository = itemRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public ItemsPageView getItemsPage(String search, SortType sortType, int pageNumber, int pageSize) {
        int normalizedPage = Math.max(pageNumber, 1);
        int normalizedPageSize = Math.max(pageSize, 1);
        Pageable pageable = PageRequest.of(
                normalizedPage - 1,
                normalizedPageSize,
                resolveSort(sortType)
        );

        Specification<Item> spec = buildSearchSpecification(search);
        Page<Item> page = itemRepository.findAll(spec, pageable);
        Map<Long, Integer> counts = getCartCounts();

        List<ItemView> itemViews = page.getContent().stream()
                .map(item -> toItemView(item, counts.getOrDefault(item.getId(), 0)))
                .toList();

        return new ItemsPageView(
                toRowsWithPlaceholders(itemViews),
                new PagingView(
                        normalizedPageSize,
                        normalizedPage,
                        page.hasPrevious(),
                        page.hasNext()
                )
        );
    }

    public ItemView getItemById(long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found: " + id));
        int count = cartItemRepository.findByItemId(id)
                .map(CartItem::getCount)
                .orElse(0);
        return toItemView(item, count);
    }

    private Specification<Item> buildSearchSpecification(String search) {
        String normalized = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return Specification.where(null);
        }
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), "%" + normalized + "%"),
                cb.like(cb.lower(root.get("description")), "%" + normalized + "%")
        );
    }

    private Sort resolveSort(SortType sortType) {
        if (sortType == null || sortType == SortType.NO) {
            return Sort.unsorted();
        }
        if (sortType == SortType.ALPHA) {
            return Sort.by(Sort.Order.asc("title"), Sort.Order.asc("id"));
        }
        return Sort.by(Sort.Order.asc("price"), Sort.Order.asc("id"));
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
        return new ItemView(PLACEHOLDER_ID, "", "", "", 0, 0);
    }

    private Map<Long, Integer> getCartCounts() {
        return cartItemRepository.findAll().stream()
                .collect(Collectors.toMap(ci -> ci.getItem().getId(), CartItem::getCount));
    }

    private ItemView toItemView(Item item, int count) {
        return new ItemView(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getImgPath(),
                item.getPrice(),
                count
        );
    }
}
