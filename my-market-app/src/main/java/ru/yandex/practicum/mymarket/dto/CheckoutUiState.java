package ru.yandex.practicum.mymarket.dto;

/**
 * Состояние оплаты для страницы корзины (баланс, можно ли нажать «Купить», текст подсказки).
 */
public record CheckoutUiState(String balanceText, boolean checkoutEnabled, String paymentHint) {}
