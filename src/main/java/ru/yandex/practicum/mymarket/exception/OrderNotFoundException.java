package ru.yandex.practicum.mymarket.exception;

public class OrderNotFoundException extends NotFoundException {

    public OrderNotFoundException(long orderId) {
        super("Order not found: " + orderId);
    }
}
