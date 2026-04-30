package ru.yandex.practicum.mymarket.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRepositoryIntegrationTest {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void liquibaseSeedCreatesThreeItems() {
        assertThat(itemRepository.count()).isEqualTo(3);
    }
}
