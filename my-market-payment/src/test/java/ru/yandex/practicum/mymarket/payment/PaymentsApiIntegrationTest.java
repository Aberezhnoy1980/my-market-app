package ru.yandex.practicum.mymarket.payment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PaymentsApiIntegrationTest {

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getBalanceReturnsConfiguredAmount() {
		webTestClient.get().uri("/api/v1/balance")
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.balance").isEqualTo("1000.00");
	}

	@Test
	void successfulPaymentReturnsBalanceAfter() {
		webTestClient.post().uri("/api/v1/payments")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("{\"amount\":\"200.50\"}")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.balanceAfter").isEqualTo("799.50");
	}

	@Test
	void insufficientFundsReturns402() {
		webTestClient.post().uri("/api/v1/payments")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("{\"amount\":\"5000\"}")
				.exchange()
				.expectStatus().isEqualTo(402)
				.expectBody()
				.jsonPath("$.code").isEqualTo("INSUFFICIENT_FUNDS");
	}
}
