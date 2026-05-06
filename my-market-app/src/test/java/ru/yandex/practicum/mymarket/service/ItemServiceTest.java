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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ItemCatalogService itemCatalogService;

    @Mock
    private ItemQueryRepository itemQueryRepository;

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
        when(itemQueryRepository.countBySearch("")).thenReturn(Mono.just(1L));
        when(itemQueryRepository.findItemIds("", SortType.NO, 0, 10)).thenReturn(Flux.just(sampleItem.getId()));
        when(itemCatalogService.getItem(sampleItem.getId())).thenReturn(Mono.just(sampleItem));

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

    @Test
    void getItemsPageUsesQueryRepoIdsAndCatalogLookup() {
        Item a = new Item();
        a.setId(1L);
        a.setTitle("Apple");
        a.setDescription("Green");
        a.setImgPath("a.png");
        a.setPrice(new BigDecimal("10"));

        Item c = new Item();
        c.setId(3L);
        c.setTitle("Apricot");
        c.setDescription("Orange");
        c.setImgPath("c.png");
        c.setPrice(new BigDecimal("30"));

        when(cartItemRepository.findAll()).thenReturn(Flux.empty());
        when(itemQueryRepository.countBySearch("ap")).thenReturn(Mono.just(2L));
        when(itemQueryRepository.findItemIds("ap", SortType.PRICE, 0, 2))
                .thenReturn(Flux.just(a.getId(), c.getId()));
        when(itemCatalogService.getItem(a.getId())).thenReturn(Mono.just(a));
        when(itemCatalogService.getItem(c.getId())).thenReturn(Mono.just(c));

        StepVerifier.create(itemService.getItemsPage("ap", SortType.PRICE, 1, 2))
                .assertNext(page -> {
                    List<ItemView> row = page.items().getFirst();
                    assertThat(row.getFirst().title()).isEqualTo("Apple");
                    assertThat(row.get(1).title()).isEqualTo("Apricot");
                    assertThat(page.paging().hasNext()).isFalse();
                })
                .verifyComplete();
    }
}
