package ru.yandex.practicum.mymarket.service;

import ru.yandex.practicum.mymarket.dto.ItemView;
import ru.yandex.practicum.mymarket.dto.ItemsPageView;
import ru.yandex.practicum.mymarket.dto.PagingView;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.SortType;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private ItemService itemService;

    private Item sampleItem;

    @BeforeEach
    void setUp() {
        sampleItem = new Item();
        sampleItem.setTitle("A");
        sampleItem.setDescription("B");
        sampleItem.setImgPath("p.png");
        sampleItem.setPrice(10);
        ReflectionTestUtils.setField(sampleItem, "id", 1L);
    }

    @Test
    void getItemsPagePadsRowToThreeColumns() {
        when(cartItemRepository.findAll()).thenReturn(List.of());
        when(itemRepository.findAll(
                ArgumentMatchers.<Specification<Item>>any(),
                ArgumentMatchers.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleItem)));

        ItemsPageView page = itemService.getItemsPage("", SortType.NO, 1, 10);

        assertThat(page.items()).hasSize(1);
        List<ItemView> row = page.items().getFirst();
        assertThat(row).hasSize(3);
        assertThat(row.getFirst().id()).isEqualTo(1L);
        assertThat(row.get(1).id()).isEqualTo(-1);
        assertThat(row.get(2).id()).isEqualTo(-1);
        PagingView paging = page.paging();
        assertThat(paging.pageNumber()).isEqualTo(1);
        assertThat(paging.pageSize()).isEqualTo(10);
    }

}
