package ru.yandex.practicum.mymarket.form;

import ru.yandex.practicum.mymarket.model.ChangeAction;

/** POST {@code /items/{id}}: только {@code action} в теле формы (id — в path). */
public record ItemPageChangeForm(ChangeAction action) {}
