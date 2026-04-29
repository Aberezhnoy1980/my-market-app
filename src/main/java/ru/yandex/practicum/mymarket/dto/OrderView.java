package ru.yandex.practicum.mymarket.dto;

import java.util.List;

public record OrderView(
        long id,
        List<OrderItemView> items,
        long totalSum
) {
}
