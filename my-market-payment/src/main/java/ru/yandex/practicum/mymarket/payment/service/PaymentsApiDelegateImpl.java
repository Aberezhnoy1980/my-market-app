package ru.yandex.practicum.mymarket.payment.service;

import ru.yandex.practicum.mymarket.payment.generated.api.PaymentsApiDelegate;
import ru.yandex.practicum.mymarket.payment.generated.model.BalanceResponse;
import ru.yandex.practicum.mymarket.payment.generated.model.PaymentRequest;
import ru.yandex.practicum.mymarket.payment.generated.model.PaymentResult;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import java.math.BigDecimal;

import reactor.core.publisher.Mono;

@Service
public class PaymentsApiDelegateImpl implements PaymentsApiDelegate {

	private final AccountBalanceService accountBalanceService;

	public PaymentsApiDelegateImpl(AccountBalanceService accountBalanceService) {
		this.accountBalanceService = accountBalanceService;
	}

	@Override
	public Mono<BalanceResponse> getBalance(ServerWebExchange exchange) {
		return accountId(exchange)
				.map(accountBalanceService::currentBalancePlain)
				.map(BalanceResponse::new);
	}

	@Override
	public Mono<PaymentResult> createPayment(Mono<PaymentRequest> paymentRequest, ServerWebExchange exchange) {
		return accountId(exchange)
				.flatMap(accountId -> paymentRequest.flatMap(req -> {
					try {
						BigDecimal amount = new BigDecimal(req.getAmount());
						return accountBalanceService.debit(accountId, amount);
					} catch (NumberFormatException ex) {
						return Mono.error(ex);
					}
				}))
				.map(after -> new PaymentResult(after.toPlainString()));
	}

	private Mono<String> accountId(ServerWebExchange exchange) {
		return exchange.getPrincipal()
				.map(principal -> principal.getName())
				.defaultIfEmpty("anonymous");
	}
}
