package ru.yandex.practicum.mymarket.payment.service;

import ru.yandex.practicum.mymarket.payment.exception.InsufficientFundsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicReference;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class AccountBalanceService {

	private static final int SCALE = 2;

	private final AtomicReference<BigDecimal> balance;

	public AccountBalanceService(@Value("${payment.initial-balance}") String initialBalanceRaw) {
		BigDecimal initial = new BigDecimal(initialBalanceRaw).setScale(SCALE, RoundingMode.HALF_UP);
		this.balance = new AtomicReference<>(initial);
	}

	public String currentBalancePlain() {
		return balance.get().toPlainString();
	}

	/**
	 * Списывает сумму; при успехе возвращает баланс после списания.
	 */
	public Mono<BigDecimal> debit(BigDecimal amount) {
		if (amount == null || amount.signum() <= 0) {
			return Mono.error(new IllegalArgumentException("Amount must be positive"));
		}
		BigDecimal normalized = amount.setScale(SCALE, RoundingMode.HALF_UP);
		return Mono.fromCallable(() -> debitSync(normalized))
				.subscribeOn(Schedulers.boundedElastic());
	}

	private BigDecimal debitSync(BigDecimal normalized) {
		for (;;) {
			BigDecimal current = balance.get();
			BigDecimal next = current.subtract(normalized);
			if (next.signum() < 0) {
				throw new InsufficientFundsException();
			}
			if (balance.compareAndSet(current, next)) {
				return next;
			}
		}
	}
}
