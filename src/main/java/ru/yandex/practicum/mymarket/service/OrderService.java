package ru.yandex.practicum.mymarket.service;

import ru.yandex.practicum.mymarket.dto.OrderItemView;
import ru.yandex.practicum.mymarket.dto.OrderView;
import ru.yandex.practicum.mymarket.exception.EmptyCartException;
import ru.yandex.practicum.mymarket.exception.OrderNotFoundException;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.CustomerOrder;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.OrderItem;
import ru.yandex.practicum.mymarket.repository.CustomerOrderRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final CartService cartService;
    private final CustomerOrderRepository customerOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;

    public OrderService(
            CartService cartService,
            CustomerOrderRepository customerOrderRepository,
            OrderItemRepository orderItemRepository,
            ItemRepository itemRepository
    ) {
        this.cartService = cartService;
        this.customerOrderRepository = customerOrderRepository;
        this.orderItemRepository = orderItemRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional
    public Mono<Long> placeOrder() {
        return cartService.getCartSnapshot()
                .flatMap(cartItems -> {
                    if (cartItems.isEmpty()) {
                        return Mono.error(new EmptyCartException());
                    }
                    return computeTotal(cartItems)
                            .flatMap(total -> {
                                CustomerOrder order = new CustomerOrder();
                                order.setTotalSum(total);
                                return customerOrderRepository.save(order);
                            })
                            .flatMap(saved ->
                                    buildOrderLines(saved.getId(), cartItems)
                                            .collectList()
                                            .flatMap(lines ->
                                                    orderItemRepository.saveAll(lines)
                                                            .then(cartService.clear())
                                                            .thenReturn(saved.getId()))
                            );
                });
    }

    private Mono<BigDecimal> computeTotal(List<CartItem> cartItems) {
        return Flux.fromIterable(cartItems)
                .concatMap(ci -> itemRepository.findById(ci.getItemId())
                        .map(item -> item.getPrice().multiply(BigDecimal.valueOf(ci.getCount()))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Flux<OrderItem> buildOrderLines(long orderId, List<CartItem> cartItems) {
        return Flux.fromIterable(cartItems)
                .concatMap(ci -> itemRepository.findById(ci.getItemId())
                        .map(item -> {
                            OrderItem line = new OrderItem();
                            line.setOrderId(orderId);
                            line.setItemId(ci.getItemId());
                            line.setCount(ci.getCount());
                            line.setPrice(item.getPrice());
                            return line;
                        }));
    }

    public Mono<List<OrderView>> getOrders() {
        return customerOrderRepository.findAll()
                .concatMap(this::toOrderView)
                .collectList();
    }

    public Mono<OrderView> getOrderById(long orderId) {
        return customerOrderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new OrderNotFoundException(orderId)))
                .flatMap(this::toOrderView);
    }

    private Mono<OrderView> toOrderView(CustomerOrder order) {
        return orderItemRepository.findByOrderId(order.getId())
                .collectList()
                .flatMap(orderItems -> {
                    if (orderItems.isEmpty()) {
                        return Mono.just(new OrderView(order.getId(), List.of(), order.getTotalSum()));
                    }
                    List<Long> itemIds = orderItems.stream().map(OrderItem::getItemId).distinct().toList();
                    return itemRepository.findAllById(itemIds)
                            .collectMap(Item::getId)
                            .map(itemsById -> mapOrderView(order, orderItems, itemsById));
                });
    }

    private OrderView mapOrderView(
            CustomerOrder order,
            List<OrderItem> orderItems,
            Map<Long, Item> itemsById
    ) {
        List<OrderItemView> views = orderItems.stream()
                .map(oi -> new OrderItemView(
                        oi.getItemId(),
                        itemsById.get(oi.getItemId()).getTitle(),
                        oi.getPrice(),
                        oi.getCount()
                ))
                .toList();
        return new OrderView(order.getId(), views, order.getTotalSum());
    }
}
