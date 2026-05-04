package ru.yandex.practicum.mymarket.form;

import ru.yandex.practicum.mymarket.model.ChangeAction;
import ru.yandex.practicum.mymarket.model.SortType;

/**
 * POST {@code /items} из витрины: поля формы и query при бинде через {@link org.springframework.web.bind.annotation.ModelAttribute}.
 */
public record ItemsPageChangeForm(
        long id,
        String search,
        SortType sort,
        Integer pageNumber,
        Integer pageSize,
        ChangeAction action
) {}
