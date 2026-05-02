package ru.yandex.practicum.mymarket.dto;

import java.math.BigDecimal;

public record OrderItemView(
        long id,
        String title,
        BigDecimal price,
        int count
) {

    public BigDecimal lineTotal() {
        return price.multiply(BigDecimal.valueOf(count));
    }
}
