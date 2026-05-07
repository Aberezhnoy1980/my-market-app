package ru.yandex.practicum.mymarket.service;

import ru.yandex.practicum.mymarket.dto.CartPageData;
import ru.yandex.practicum.mymarket.dto.CheckoutUiState;
import ru.yandex.practicum.mymarket.dto.ItemView;
import ru.yandex.practicum.mymarket.exception.ItemNotFoundException;
import ru.yandex.practicum.mymarket.mapper.ItemViewMapper;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.ChangeAction;
import ru.yandex.practicum.mymarket.repository.AppUserRepository;
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
    private final AppUserRepository appUserRepository;

    public CartService(
            CartItemRepository cartItemRepository,
            ItemCatalogService itemCatalogService,
            ItemViewMapper itemViewMapper,
            PaymentService paymentService,
            AppUserRepository appUserRepository
    ) {
        this.cartItemRepository = cartItemRepository;
        this.itemCatalogService = itemCatalogService;
        this.itemViewMapper = itemViewMapper;
        this.paymentService = paymentService;
        this.appUserRepository = appUserRepository;
    }

    /**
     * Один {@code findAll()} по корзине и согласованные строки + сумма (для страницы корзины).
     */
    public Mono<CartPageData> getCartPageData(String username) {
        return resolveUserId(username)
                .flatMapMany(cartItemRepository::findAllByUserId)
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
    public Mono<Void> changeItemCount(String username, long itemId, ChangeAction action) {
        return resolveUserId(username)
                .flatMap(userId -> {
                    if (action == ChangeAction.DELETE) {
                        return cartItemRepository.deleteByUserIdAndItemId(userId, itemId).then();
                    }
                    if (action == ChangeAction.PLUS) {
                        return plus(userId, itemId);
                    }
                    if (action == ChangeAction.MINUS) {
                        return minus(userId, itemId);
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> plus(long userId, long itemId) {
        return cartItemRepository.findByUserIdAndItemId(userId, itemId)
                .flatMap(ci -> {
                    ci.setCount(ci.getCount() + 1);
                    return cartItemRepository.save(ci);
                })
                .switchIfEmpty(createLineWithCountOne(userId, itemId))
                .then();
    }

    private Mono<CartItem> createLineWithCountOne(long userId, long itemId) {
        return itemCatalogService.getItem(itemId)
                .switchIfEmpty(Mono.error(new ItemNotFoundException(itemId)))
                .flatMap(item -> {
                    CartItem cartItem = new CartItem();
                    cartItem.setUserId(userId);
                    cartItem.setItemId(itemId);
                    cartItem.setCount(1);
                    return cartItemRepository.save(cartItem);
                });
    }

    private Mono<Void> minus(long userId, long itemId) {
        return cartItemRepository.findByUserIdAndItemId(userId, itemId)
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
    public Mono<Void> clear(String username) {
        return resolveUserId(username)
                .flatMap(userId -> cartItemRepository.deleteAllByUserId(userId).then());
    }

    public Mono<List<CartItem>> getCartSnapshot(String username) {
        return resolveUserId(username)
                .flatMap(userId -> cartItemRepository.findAllByUserId(userId).collectList());
    }

    private Mono<Long> resolveUserId(String username) {
        return appUserRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("User not found: " + username)))
                .map(user -> user.getId());
    }
}
