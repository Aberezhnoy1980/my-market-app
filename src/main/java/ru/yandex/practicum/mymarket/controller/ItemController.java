package ru.yandex.practicum.mymarket.controller;

import ru.yandex.practicum.mymarket.dto.ItemView;
import ru.yandex.practicum.mymarket.dto.ItemsPageView;
import ru.yandex.practicum.mymarket.model.ChangeAction;
import ru.yandex.practicum.mymarket.model.SortType;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.ItemService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

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
    public String getItems(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "NO") SortType sort,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize,
            Model model
    ) {
        ItemsPageView page = itemService.getItemsPage(search, sort, pageNumber, pageSize);
        model.addAttribute("items", page.items());
        model.addAttribute("search", search);
        model.addAttribute("sort", sort.name());
        model.addAttribute("paging", page.paging());
        return "items";
    }

    @PostMapping("/items")
    public String changeItemCountFromItemsPage(
            @RequestParam long id,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "NO") SortType sort,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize,
            @RequestParam ChangeAction action
    ) {
        cartService.changeItemCount(id, action);
        String redirectUrl = UriComponentsBuilder.fromPath("/items")
                .queryParam("search", search)
                .queryParam("sort", sort.name())
                .queryParam("pageNumber", Math.max(pageNumber, DEFAULT_PAGE_NUMBER))
                .queryParam("pageSize", Math.max(pageSize, DEFAULT_PAGE_SIZE))
                .build()
                .toUriString();
        return "redirect:" + redirectUrl;
    }

    @GetMapping("/items/{id}")
    public String getItem(@PathVariable long id, Model model) {
        ItemView item = itemService.getItemById(id);
        model.addAttribute("item", item);
        return "item";
    }

    @PostMapping("/items/{id}")
    public String changeItemCountFromItemPage(
            @PathVariable long id,
            @RequestParam ChangeAction action,
            Model model
    ) {
        cartService.changeItemCount(id, action);
        ItemView item = itemService.getItemById(id);
        model.addAttribute("item", item);
        return "item";
    }
}
