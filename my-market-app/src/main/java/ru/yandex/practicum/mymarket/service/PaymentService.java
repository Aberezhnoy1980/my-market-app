package ru.yandex.practicum.mymarket.service;

import ru.yandex.practicum.mymarket.dto.CheckoutUiState;
import ru.yandex.practicum.mymarket.exception.InsufficientPaymentBalanceException;
import ru.yandex.practicum.mymarket.exception.PaymentServiceUnavailableException;
import ru.yandex.practicum.mymarket.payment.client.PaymentsApi;
import ru.yandex.practicum.mymarket.payment.client.model.PaymentRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.math.RoundingMode;

import reactor.core.publisher.Mono;

@Service
public class PaymentService {

	private static final int MONEY_SCALE = 2;

	private final PaymentsApi paymentsApi;

	public PaymentService(PaymentsApi paymentsApi) {
		this.paymentsApi = paymentsApi;
	}

	/**
	 * Данные для корзины: баланс, доступность кнопки заказа, причина блокировки.
	 */
	public Mono<CheckoutUiState> describeCheckout(BigDecimal cartTotal, boolean hasItems) {
		return paymentsApi.getBalance()
				.map(response -> new BigDecimal(response.getBalance()).setScale(MONEY_SCALE, RoundingMode.HALF_UP))
				.map(balance -> toCheckoutUi(balance, cartTotal, hasItems))
				.onErrorResume(e -> Mono.just(unavailableState()));
	}

	private CheckoutUiState toCheckoutUi(BigDecimal balance, BigDecimal cartTotal, boolean hasItems) {
		String balanceText = balance.toPlainString() + " руб.";
		if (!hasItems || cartTotal.signum() <= 0) {
			return new CheckoutUiState(balanceText, false, null);
		}
		BigDecimal total = cartTotal.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
		if (balance.compareTo(total) < 0) {
			return new CheckoutUiState(
					balanceText,
					false,
					"Недостаточно средств на счёте в сервисе платежей."
			);
		}
		return new CheckoutUiState(balanceText, true, null);
	}

	private CheckoutUiState unavailableState() {
		return new CheckoutUiState(
				null,
				false,
				"Сервис платежей недоступен. Оформление заказа временно невозможно."
		);
	}

	/**
	 * Списание суммы заказа в сервисе платежей перед сохранением заказа в БД.
	 */
	public Mono<Void> chargeOrderAmount(BigDecimal amount) {
		PaymentRequest request = new PaymentRequest();
		request.setAmount(amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP).toPlainString());
		return paymentsApi.createPayment(request)
				.then()
				.onErrorMap(this::mapChargeError);
	}

	private Throwable mapChargeError(Throwable throwable) {
		if (throwable instanceof WebClientResponseException ex) {
			if (ex.getStatusCode().value() == 402) {
				return new InsufficientPaymentBalanceException();
			}
			return new PaymentServiceUnavailableException("Ошибка сервиса платежей при оплате.", ex);
		}
		if (throwable instanceof WebClientRequestException ex) {
			return new PaymentServiceUnavailableException("Не удалось связаться с сервисом платежей.", ex);
		}
		return new PaymentServiceUnavailableException("Не удалось выполнить оплату.", throwable);
	}
}
