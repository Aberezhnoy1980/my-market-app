package ru.yandex.practicum.mymarket.payment.exception;

public class InsufficientFundsException extends RuntimeException {

	public InsufficientFundsException() {
		super("Insufficient balance for payment");
	}
}
