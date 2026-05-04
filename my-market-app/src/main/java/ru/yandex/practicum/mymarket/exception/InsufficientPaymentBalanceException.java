package ru.yandex.practicum.mymarket.exception;

public class InsufficientPaymentBalanceException extends RuntimeException {

	public InsufficientPaymentBalanceException() {
		super("Insufficient balance for payment");
	}
}
