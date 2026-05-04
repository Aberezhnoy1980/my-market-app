package ru.yandex.practicum.mymarket.dto;

public record PagingView(
        int pageSize,
        int pageNumber,
        boolean hasPrevious,
        boolean hasNext
) {
}
