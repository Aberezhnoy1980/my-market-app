package ru.yandex.practicum.mymarket.dto;

import java.math.BigDecimal;
import java.util.List;

/** Строки корзины и сумма за один проход по данным корзины. */
public record CartPageData(List<ItemView> items, BigDecimal total) {}
