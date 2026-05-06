package ru.yandex.practicum.mymarket.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Строки корзины, сумма и состояние оплаты (баланс из сервиса платежей).
 *
 * @param balanceText   отображаемый баланс или {@code null}, если сервис недоступен
 * @param paymentHint   сообщение при недоступной оплате или недостатке средств; для пустой корзины — {@code null}
 */
public record CartPageData(
		List<ItemView> items,
		BigDecimal total,
		String balanceText,
		boolean checkoutEnabled,
		String paymentHint
) {}
