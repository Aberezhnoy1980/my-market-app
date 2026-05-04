package ru.yandex.practicum.mymarket.form;

import ru.yandex.practicum.mymarket.model.ChangeAction;

/** POST {@code /cart/items}: id и action из тела формы. */
public record CartItemChangeForm(long id, ChangeAction action) {}
