package ru.yandex.practicum.mymarket.controller;

import ru.yandex.practicum.mymarket.dto.ItemView;
import ru.yandex.practicum.mymarket.dto.ItemsPageView;
import ru.yandex.practicum.mymarket.dto.PagingView;
import ru.yandex.practicum.mymarket.model.ChangeAction;
import ru.yandex.practicum.mymarket.model.SortType;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.ItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ItemController.class)
class ItemControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @MockBean
    private CartService cartService;

    @Test
    void getItemsReturnsItemsView() throws Exception {
        ItemsPageView page = new ItemsPageView(
                List.of(),
                new PagingView(5, 1, false, false)
        );
        when(itemService.getItemsPage(anyString(), eq(SortType.NO), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/items")
                        .param("search", "ball")
                        .param("sort", "NO")
                        .param("pageNumber", "1")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"));
    }

    @Test
    void getItemByIdReturnsItemView() throws Exception {
        when(itemService.getItemById(1L))
                .thenReturn(new ItemView(1L, "T", "D", "img.png", new BigDecimal("100"), 0));

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"));
    }

    @Test
    void postItemsRedirectsWithQueryParams() throws Exception {
        mockMvc.perform(post("/items")
                        .param("id", "1")
                        .param("search", "a b")
                        .param("sort", "ALPHA")
                        .param("pageNumber", "2")
                        .param("pageSize", "10")
                        .param("action", "PLUS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items?search=a b&sort=ALPHA&pageNumber=2&pageSize=10"));

        verify(cartService).changeItemCount(1L, ChangeAction.PLUS);
    }

    @Test
    void postItemByIdReturnsItemView() throws Exception {
        when(itemService.getItemById(2L))
                .thenReturn(new ItemView(2L, "X", "Y", "z.png", new BigDecimal("50"), 1));

        mockMvc.perform(post("/items/2")
                        .param("action", "MINUS"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"));

        verify(cartService).changeItemCount(2L, ChangeAction.MINUS);
    }
}
