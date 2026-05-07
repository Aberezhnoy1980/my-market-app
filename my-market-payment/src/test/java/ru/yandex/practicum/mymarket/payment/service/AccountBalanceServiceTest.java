package ru.yandex.practicum.mymarket.payment.service;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.payment.exception.InsufficientFundsException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountBalanceServiceTest {
	private static final String ACCOUNT = "user-1";

	@Test
	void initialBalanceIsReadable() {
		AccountBalanceService svc = new AccountBalanceService("1234.50");
		assertEquals("1234.50", svc.currentBalancePlain(ACCOUNT));
	}

	@Test
	void debitReducesBalance() {
		AccountBalanceService svc = new AccountBalanceService("1000.00");
		StepVerifier.create(svc.debit(ACCOUNT, new BigDecimal("200.50")))
				.expectNext(new BigDecimal("799.50"))
				.verifyComplete();
		assertEquals("799.50", svc.currentBalancePlain(ACCOUNT));
	}

	@Test
	void debitInsufficientFails() {
		AccountBalanceService svc = new AccountBalanceService("100.00");
		StepVerifier.create(svc.debit(ACCOUNT, new BigDecimal("200.00")))
				.expectError(InsufficientFundsException.class)
				.verify();
	}

	@Test
	void debitRejectsNonPositiveAmount() {
		AccountBalanceService svc = new AccountBalanceService("1000.00");
		StepVerifier.create(svc.debit(ACCOUNT, BigDecimal.ZERO))
				.expectError(IllegalArgumentException.class)
				.verify();
	}
}
