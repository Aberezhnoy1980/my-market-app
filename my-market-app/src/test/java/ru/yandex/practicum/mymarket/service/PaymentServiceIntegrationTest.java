package ru.yandex.practicum.mymarket.service;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.config.PaymentClientConfiguration;
import ru.yandex.practicum.mymarket.exception.InsufficientPaymentBalanceException;
import ru.yandex.practicum.mymarket.exception.PaymentServiceUnavailableException;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Интеграция с HTTP API платежей через сгенерированный WebClient-клиент (MockWebServer).
 */
@SpringBootTest(classes = {PaymentClientConfiguration.class, PaymentService.class})
class PaymentServiceIntegrationTest {

	private static final MockWebServer PAYMENT_SERVER = new MockWebServer();

	static {
		try {
			PAYMENT_SERVER.start();
		} catch (IOException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	@DynamicPropertySource
	static void registerPaymentBaseUrl(DynamicPropertyRegistry registry) {
		registry.add("payment.service.base-url", () -> "http://127.0.0.1:" + PAYMENT_SERVER.getPort());
	}

	@AfterAll
	static void shutdownMockServer() throws IOException {
		PAYMENT_SERVER.shutdown();
	}

	@Autowired
	private PaymentService paymentService;

	@Test
	void describeCheckoutWhenBalanceSufficient() {
		PAYMENT_SERVER.enqueue(new MockResponse()
				.setBody("{\"balance\":\"5000.00\"}")
				.addHeader("Content-Type", "application/json"));

		StepVerifier.create(paymentService.describeCheckout(new BigDecimal("100.00"), true))
				.expectNextMatches(ui -> ui.checkoutEnabled()
						&& ui.balanceText().startsWith("5000")
						&& ui.paymentHint() == null)
				.verifyComplete();
	}

	@Test
	void describeCheckoutWhenBalanceInsufficient() {
		PAYMENT_SERVER.enqueue(new MockResponse()
				.setBody("{\"balance\":\"10.00\"}")
				.addHeader("Content-Type", "application/json"));

		StepVerifier.create(paymentService.describeCheckout(new BigDecimal("100.00"), true))
				.expectNextMatches(ui -> !ui.checkoutEnabled()
						&& ui.paymentHint() != null)
				.verifyComplete();
	}

	@Test
	void describeCheckoutWhenPaymentReturnsErrorReturnsUnavailable() {
		PAYMENT_SERVER.enqueue(new MockResponse().setResponseCode(503));

		StepVerifier.create(paymentService.describeCheckout(BigDecimal.ONE, true))
				.expectNextMatches(ui -> !ui.checkoutEnabled()
						&& ui.balanceText() == null
						&& ui.paymentHint() != null)
				.verifyComplete();
	}

	@Test
	void chargeOrderAmountSuccess() {
		PAYMENT_SERVER.enqueue(new MockResponse()
				.setBody("{\"balanceAfter\":\"900.00\"}")
				.addHeader("Content-Type", "application/json"));

		StepVerifier.create(paymentService.chargeOrderAmount(new BigDecimal("100.00")))
				.verifyComplete();
	}

	@Test
	void chargeOrderAmountInsufficientFunds() {
		PAYMENT_SERVER.enqueue(new MockResponse()
				.setResponseCode(402)
				.setBody("{\"code\":\"INSUFFICIENT_FUNDS\",\"message\":\"Not enough balance for this order.\"}")
				.addHeader("Content-Type", "application/json"));

		StepVerifier.create(paymentService.chargeOrderAmount(new BigDecimal("9999.00")))
				.expectError(InsufficientPaymentBalanceException.class)
				.verify();
	}

	@Test
	void chargeOrderAmountServerError() {
		PAYMENT_SERVER.enqueue(new MockResponse().setResponseCode(500));

		StepVerifier.create(paymentService.chargeOrderAmount(new BigDecimal("10.00")))
				.expectError(PaymentServiceUnavailableException.class)
				.verify();
	}
}
