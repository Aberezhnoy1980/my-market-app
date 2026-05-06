package ru.yandex.practicum.mymarket.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderView(
        long id,
        List<OrderItemView> items,
        BigDecimal totalSum
) {
}
