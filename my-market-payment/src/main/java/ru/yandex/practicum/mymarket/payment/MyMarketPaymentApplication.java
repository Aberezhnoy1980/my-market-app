package ru.yandex.practicum.mymarket.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
		"ru.yandex.practicum.mymarket.payment",
		"ru.yandex.practicum.mymarket.payment.generated",
		"org.openapitools.configuration"
})
public class MyMarketPaymentApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyMarketPaymentApplication.class, args);
	}
}
