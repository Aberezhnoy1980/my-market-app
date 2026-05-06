package ru.yandex.practicum.mymarket.controller;

import ru.yandex.practicum.mymarket.dto.ItemView;
import ru.yandex.practicum.mymarket.dto.ItemsPageView;
import ru.yandex.practicum.mymarket.dto.PagingView;
import ru.yandex.practicum.mymarket.config.SecurityConfiguration;
import ru.yandex.practicum.mymarket.model.ChangeAction;
import ru.yandex.practicum.mymarket.model.SortType;
import ru.yandex.practicum.mymarket.repository.AppUserRepository;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.ItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import reactor.core.publisher.Mono;

@WebFluxTest(ItemController.class)
@Import(SecurityConfiguration.class)
class ItemControllerWebFluxTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ItemService itemService;

    @MockBean
    private CartService cartService;

    @MockBean
    private AppUserRepository appUserRepository;

    @Test
    void getItemsReturnsOk() {
        ItemsPageView page = new ItemsPageView(
                List.of(),
                new PagingView(5, 1, false, false)
        );
        when(itemService.getItemsPage(anyString(), eq(SortType.NO), anyInt(), anyInt(), any()))
                .thenReturn(Mono.just(page));

        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/items")
                        .queryParam("search", "ball")
                        .queryParam("sort", "NO")
                        .queryParam("pageNumber", "1")
                        .queryParam("pageSize", "5")
                        .build())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getItemByIdReturnsOk() {
        when(itemService.getItemById(eq(1L), any()))
                .thenReturn(Mono.just(new ItemView(1L, "T", "D", "img.png", new BigDecimal("100"), 0)));

        webTestClient.get().uri("/items/1")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void postItemsRedirectsWithQueryParams() {
        when(cartService.changeItemCount(anyString(), eq(1L), eq(ChangeAction.PLUS))).thenReturn(Mono.empty());

        webTestClient.mutateWith(mockUser()).mutateWith(csrf()).post().uri(uriBuilder -> uriBuilder.path("/items")
                        .queryParam("id", "1")
                        .queryParam("search", "a b")
                        .queryParam("sort", "ALPHA")
                        .queryParam("pageNumber", "2")
                        .queryParam("pageSize", "10")
                        .queryParam("action", "PLUS")
                        .build())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/items?search=a%20b&sort=ALPHA&pageNumber=2&pageSize=10");

        verify(cartService).changeItemCount(anyString(), eq(1L), eq(ChangeAction.PLUS));
    }

    @Test
    void postItemByIdReturnsOk() {
        when(cartService.changeItemCount(anyString(), eq(2L), eq(ChangeAction.MINUS))).thenReturn(Mono.empty());
        when(itemService.getItemById(eq(2L), any()))
                .thenReturn(Mono.just(new ItemView(2L, "X", "Y", "z.png", new BigDecimal("50"), 1)));

        webTestClient.mutateWith(mockUser()).mutateWith(csrf()).post().uri("/items/2?action=MINUS")
                .exchange()
                .expectStatus().isOk();

        verify(cartService).changeItemCount(anyString(), eq(2L), eq(ChangeAction.MINUS));
    }
}
