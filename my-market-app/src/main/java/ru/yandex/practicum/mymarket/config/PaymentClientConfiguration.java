package ru.yandex.practicum.mymarket.config;

import ru.yandex.practicum.mymarket.payment.client.ApiClient;
import ru.yandex.practicum.mymarket.payment.client.PaymentsApi;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PaymentClientConfiguration {

	@Bean
	public ApiClient paymentApiClient(
			@Value("${payment.service.base-url}") String baseUrl,
			@Value("${payment.oauth2.enabled:true}") boolean oauth2Enabled,
			@Value("${payment.oauth2.client-registration-id:payment-service-client}") String clientRegistrationId,
			ObjectProvider<ReactiveClientRegistrationRepository> clientRegistrationRepositoryProvider
	) {
		ApiClient client;
		if (oauth2Enabled) {
			ReactiveClientRegistrationRepository clientRegistrationRepository =
					clientRegistrationRepositoryProvider.getIfAvailable();
			if (clientRegistrationRepository == null) {
				throw new IllegalStateException("ReactiveClientRegistrationRepository is required when payment.oauth2.enabled=true");
			}

			InMemoryReactiveOAuth2AuthorizedClientService authorizedClientService =
					new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrationRepository);
			ReactiveOAuth2AuthorizedClientProvider provider =
					ReactiveOAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build();
			AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientManager =
					new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
							clientRegistrationRepository,
							authorizedClientService
					);
			authorizedClientManager.setAuthorizedClientProvider(provider);

			ServerOAuth2AuthorizedClientExchangeFilterFunction oauth =
					new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
			oauth.setDefaultClientRegistrationId(clientRegistrationId);
			WebClient oauthWebClient = WebClient.builder().filter(oauth).build();
			client = new ApiClient(oauthWebClient);
		} else {
			client = new ApiClient();
		}
		client.setBasePath(baseUrl);
		return client;
	}

	@Bean
	public PaymentsApi paymentsApi(ApiClient paymentApiClient) {
		return new PaymentsApi(paymentApiClient);
	}
}
