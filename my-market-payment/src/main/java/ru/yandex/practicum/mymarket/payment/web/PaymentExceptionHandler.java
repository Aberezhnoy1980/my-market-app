package ru.yandex.practicum.mymarket.payment.web;

import ru.yandex.practicum.mymarket.payment.exception.InsufficientFundsException;
import ru.yandex.practicum.mymarket.payment.generated.model.PaymentError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;

import reactor.core.publisher.Mono;

@RestControllerAdvice
public class PaymentExceptionHandler {

	@ExceptionHandler(InsufficientFundsException.class)
	public Mono<ResponseEntity<PaymentError>> insufficientFunds(InsufficientFundsException ex) {
		PaymentError body = new PaymentError("INSUFFICIENT_FUNDS", "Not enough balance for this order.");
		return Mono.just(ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(body));
	}

	@ExceptionHandler({IllegalArgumentException.class, NumberFormatException.class, ServerWebInputException.class,
			WebExchangeBindException.class})
	public Mono<ResponseEntity<PaymentError>> badRequest(Exception ex) {
		PaymentError body = new PaymentError("BAD_REQUEST", ex.getMessage() != null ? ex.getMessage() : "Invalid request");
		return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body));
	}
}
