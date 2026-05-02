package ru.yandex.practicum.mymarket.dto;

import java.util.List;

public record ItemsPageView(
        List<List<ItemView>> items,
        PagingView paging
) {
}
