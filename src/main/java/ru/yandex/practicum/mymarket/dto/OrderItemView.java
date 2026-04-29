package ru.yandex.practicum.mymarket.dto;

public record OrderItemView(
        long id,
        String title,
        long price,
        int count
) {
}
