package ru.yandex.practicum.mymarket.dto;

public record ItemView(
        long id,
        String title,
        String description,
        String imgPath,
        long price,
        int count
) {
}
