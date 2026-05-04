package ru.yandex.practicum.mymarket.config;

import ru.yandex.practicum.mymarket.payment.client.ApiClient;
import ru.yandex.practicum.mymarket.payment.client.PaymentsApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentClientConfiguration {

	@Bean
	public ApiClient paymentApiClient(@Value("${payment.service.base-url}") String baseUrl) {
		ApiClient client = new ApiClient();
		client.setBasePath(baseUrl);
		return client;
	}

	@Bean
	public PaymentsApi paymentsApi(ApiClient paymentApiClient) {
		return new PaymentsApi(paymentApiClient);
	}
}
