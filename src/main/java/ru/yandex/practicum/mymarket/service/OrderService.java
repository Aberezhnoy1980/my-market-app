package ru.yandex.practicum.mymarket.service;

import ru.yandex.practicum.mymarket.dto.OrderItemView;
import ru.yandex.practicum.mymarket.dto.OrderView;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.CustomerOrder;
import ru.yandex.practicum.mymarket.model.OrderItem;
import ru.yandex.practicum.mymarket.exception.EmptyCartException;
import ru.yandex.practicum.mymarket.exception.OrderNotFoundException;
import ru.yandex.practicum.mymarket.repository.CustomerOrderRepository;
import ru.yandex.practicum.mymarket.repository.OrderItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final CartService cartService;
    private final CustomerOrderRepository customerOrderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(
            CartService cartService,
            CustomerOrderRepository customerOrderRepository,
            OrderItemRepository orderItemRepository
    ) {
        this.cartService = cartService;
        this.customerOrderRepository = customerOrderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public long placeOrder() {
        List<CartItem> cartItems = cartService.getCartSnapshot();
        if (cartItems.isEmpty()) {
            throw new EmptyCartException();
        }

        long total = cartItems.stream()
                .mapToLong(ci -> ci.getItem().getPrice() * ci.getCount())
                .sum();

        CustomerOrder customerOrder = new CustomerOrder();
        customerOrder.setTotalSum(total);
        CustomerOrder savedOrder = customerOrderRepository.save(customerOrder);

        List<OrderItem> orderItems = cartItems.stream()
                .map(ci -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(savedOrder);
                    orderItem.setItem(ci.getItem());
                    orderItem.setCount(ci.getCount());
                    orderItem.setPrice(ci.getItem().getPrice());
                    return orderItem;
                })
                .toList();
        orderItemRepository.saveAll(orderItems);

        cartService.clear();
        return savedOrder.getId();
    }

    public List<OrderView> getOrders() {
        return customerOrderRepository.findAll().stream()
                .map(order -> toOrderView(order, orderItemRepository.findByOrderId(order.getId())))
                .toList();
    }

    public OrderView getOrderById(long orderId) {
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        return toOrderView(order, orderItems);
    }

    private OrderView toOrderView(CustomerOrder order, List<OrderItem> orderItems) {
        List<OrderItemView> items = orderItems.stream()
                .map(oi -> new OrderItemView(
                        oi.getItem().getId(),
                        oi.getItem().getTitle(),
                        oi.getPrice(),
                        oi.getCount()
                ))
                .toList();
        return new OrderView(order.getId(), items, order.getTotalSum());
    }
}
