package ru.yandex.practicum.mymarket.service;

import ru.yandex.practicum.mymarket.dto.ItemView;
import ru.yandex.practicum.mymarket.dto.PagingView;
import ru.yandex.practicum.mymarket.mapper.ItemViewMapper;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.SortType;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ItemQueryRepository itemQueryRepository;

    @Mock
    private ItemCatalogService itemCatalogService;

    @Spy
    private ItemViewMapper itemViewMapper = new ItemViewMapper();

    @InjectMocks
    private ItemService itemService;

    private Item sampleItem;

    @BeforeEach
    void setUp() {
        sampleItem = new Item();
        sampleItem.setTitle("A");
        sampleItem.setDescription("B");
        sampleItem.setImgPath("p.png");
        sampleItem.setPrice(new BigDecimal("10"));
        sampleItem.setId(1L);
    }

    @Test
    void getItemsPagePadsRowToThreeColumns() {
        when(cartItemRepository.findAll()).thenReturn(Flux.empty());
        when(itemQueryRepository.countBySearch(anyString())).thenReturn(Mono.just(1L));
        when(itemQueryRepository.findItemIds(anyString(), any(SortType.class), anyInt(), anyInt()))
                .thenReturn(Flux.just(1L));
        when(itemCatalogService.getItem(1L)).thenReturn(Mono.just(sampleItem));

        StepVerifier.create(itemService.getItemsPage("", SortType.NO, 1, 10))
                .assertNext(page -> {
                    assertThat(page.items()).hasSize(1);
                    java.util.List<ItemView> row = page.items().getFirst();
                    assertThat(row).hasSize(3);
                    assertThat(row.getFirst().id()).isEqualTo(1L);
                    assertThat(row.get(1).id()).isEqualTo(-1);
                    assertThat(row.get(2).id()).isEqualTo(-1);
                    PagingView paging = page.paging();
                    assertThat(paging.pageNumber()).isEqualTo(1);
                    assertThat(paging.pageSize()).isEqualTo(10);
                })
                .verifyComplete();
    }
}
