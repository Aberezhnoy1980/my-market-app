package ru.yandex.practicum.mymarket.dto;

import java.math.BigDecimal;

public record ItemView(
        long id,
        String title,
        String description,
        String imgPath,
        BigDecimal price,
        int count
) {
}
