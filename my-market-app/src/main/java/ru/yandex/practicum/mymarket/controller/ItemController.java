package ru.yandex.practicum.mymarket.controller;

import ru.yandex.practicum.mymarket.form.ItemPageChangeForm;
import ru.yandex.practicum.mymarket.form.ItemsPageChangeForm;
import ru.yandex.practicum.mymarket.model.SortType;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.ItemService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.Principal;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Controller
@RequestMapping
public class ItemController {

    private static final int DEFAULT_PAGE_NUMBER = 1;
    private static final int DEFAULT_PAGE_SIZE = 5;

    private final ItemService itemService;
    private final CartService cartService;

    public ItemController(ItemService itemService, CartService cartService) {
        this.itemService = itemService;
        this.cartService = cartService;
    }

    @GetMapping({"", "/", "/items"})
    public Mono<Rendering> getItems(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "NO") SortType sort,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize,
            Principal principal
    ) {
        return itemService.getItemsPage(search, sort, pageNumber, pageSize, principalName(principal))
                .map(page -> Rendering.view("items")
                        .modelAttribute("items", page.items())
                        .modelAttribute("search", search)
                        .modelAttribute("sort", sort.name())
                        .modelAttribute("paging", page.paging())
                        .build());
    }

    /**
     * В WebFlux {@code @RequestParam} читает только query string; поля HTML-формы приходят в теле,
     * поэтому биндим query + form через {@link ModelAttribute}.
     */
    @PostMapping("/items")
    public Mono<Rendering> changeItemCountFromItemsPage(@ModelAttribute ItemsPageChangeForm form, Principal principal) {
        String search = Optional.ofNullable(form.search()).orElse("");
        SortType sort = Optional.ofNullable(form.sort()).orElse(SortType.NO);
        int pageNumber = Optional.ofNullable(form.pageNumber()).filter(n -> n >= 1).orElse(DEFAULT_PAGE_NUMBER);
        int pageSize = Optional.ofNullable(form.pageSize()).filter(s -> s >= 1).orElse(DEFAULT_PAGE_SIZE);
        return cartService.changeItemCount(principal.getName(), form.id(), form.action())
                .then(Mono.fromCallable(() -> {
                    String redirectUrl = UriComponentsBuilder.fromPath("/items")
                            .queryParam("search", search)
                            .queryParam("sort", sort.name())
                            .queryParam("pageNumber", Math.max(pageNumber, DEFAULT_PAGE_NUMBER))
                            .queryParam("pageSize", Math.max(pageSize, DEFAULT_PAGE_SIZE))
                            .build()
                            .encode()
                            .toUriString();
                    return Rendering.redirectTo(redirectUrl).build();
                }));
    }

    @GetMapping("/items/{id}")
    public Mono<Rendering> getItem(@PathVariable long id, Principal principal) {
        return itemService.getItemById(id, principalName(principal))
                .map(item -> Rendering.view("item")
                        .modelAttribute("item", item)
                        .build());
    }

    @PostMapping("/items/{id}")
    public Mono<Rendering> changeItemCountFromItemPage(
            @PathVariable long id,
            @ModelAttribute ItemPageChangeForm form,
            Principal principal
    ) {
        return cartService.changeItemCount(principal.getName(), id, form.action())
                .then(itemService.getItemById(id, principal.getName()))
                .map(item -> Rendering.view("item")
                        .modelAttribute("item", item)
                        .build());
    }

    private String principalName(Principal principal) {
        return principal != null ? principal.getName() : null;
    }
}
