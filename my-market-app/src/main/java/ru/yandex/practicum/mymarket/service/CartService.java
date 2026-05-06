package ru.yandex.practicum.mymarket.service;

import ru.yandex.practicum.mymarket.dto.CartPageData;
import ru.yandex.practicum.mymarket.dto.CheckoutUiState;
import ru.yandex.practicum.mymarket.dto.ItemView;
import ru.yandex.practicum.mymarket.exception.ItemNotFoundException;
import ru.yandex.practicum.mymarket.mapper.ItemViewMapper;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.ChangeAction;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import reactor.core.publisher.Mono;

@Service
@Transactional(readOnly = true)
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ItemCatalogService itemCatalogService;
    private final ItemViewMapper itemViewMapper;
    private final PaymentService paymentService;

    public CartService(
            CartItemRepository cartItemRepository,
            ItemCatalogService itemCatalogService,
            ItemViewMapper itemViewMapper,
            PaymentService paymentService
    ) {
        this.cartItemRepository = cartItemRepository;
        this.itemCatalogService = itemCatalogService;
        this.itemViewMapper = itemViewMapper;
        this.paymentService = paymentService;
    }

    /**
     * Один {@code findAll()} по корзине и согласованные строки + сумма (для страницы корзины).
     */
    public Mono<CartPageData> getCartPageData() {
        return cartItemRepository.findAll()
                .concatMap(ci -> itemCatalogService.getItem(ci.getItemId())
                        .map(item -> new CartLine(
                                itemViewMapper.toView(item, ci.getCount()),
                                item.getPrice().multiply(BigDecimal.valueOf(ci.getCount())))))
                .collectList()
                .flatMap(lines -> {
                    List<ItemView> items = lines.stream().map(CartLine::view).toList();
                    BigDecimal total = lines.stream()
                            .map(CartLine::lineTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    boolean hasItems = !items.isEmpty();
                    return paymentService.describeCheckout(total, hasItems)
                            .map(ui -> toCartPage(items, total, ui));
                });
    }

    private CartPageData toCartPage(List<ItemView> items, BigDecimal total, CheckoutUiState ui) {
        return new CartPageData(items, total, ui.balanceText(), ui.checkoutEnabled(), ui.paymentHint());
    }

    private record CartLine(ItemView view, BigDecimal lineTotal) {}

    @Transactional
    public Mono<Void> changeItemCount(long itemId, ChangeAction action) {
        if (action == ChangeAction.DELETE) {
            return cartItemRepository.deleteByItemId(itemId).then();
        }
        if (action == ChangeAction.PLUS) {
            return plus(itemId);
        }
        if (action == ChangeAction.MINUS) {
            return minus(itemId);
        }
        return Mono.empty();
    }

    private Mono<Void> plus(long itemId) {
        return cartItemRepository.findByItemId(itemId)
                .flatMap(ci -> {
                    ci.setCount(ci.getCount() + 1);
                    return cartItemRepository.save(ci);
                })
                .switchIfEmpty(createLineWithCountOne(itemId))
                .then();
    }

    private Mono<CartItem> createLineWithCountOne(long itemId) {
        return itemCatalogService.getItem(itemId)
                .switchIfEmpty(Mono.error(new ItemNotFoundException(itemId)))
                .flatMap(item -> {
                    CartItem cartItem = new CartItem();
                    cartItem.setItemId(itemId);
                    cartItem.setCount(1);
                    return cartItemRepository.save(cartItem);
                });
    }

    private Mono<Void> minus(long itemId) {
        return cartItemRepository.findByItemId(itemId)
                .flatMap(ci -> {
                    int nextCount = ci.getCount() - 1;
                    if (nextCount <= 0) {
                        return cartItemRepository.delete(ci);
                    }
                    ci.setCount(nextCount);
                    return cartItemRepository.save(ci);
                })
                .then();
    }

    @Transactional
    public Mono<Void> clear() {
        return cartItemRepository.deleteAll();
    }

    public Mono<List<CartItem>> getCartSnapshot() {
        return cartItemRepository.findAll().collectList();
    }
}
