package ru.yandex.practicum.mymarket.mapper;

import ru.yandex.practicum.mymarket.dto.ItemView;
import ru.yandex.practicum.mymarket.model.Item;
import org.springframework.stereotype.Component;

@Component
public class ItemViewMapper {

    public ItemView toView(Item item, int count) {
        return new ItemView(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getImgPath(),
                item.getPrice(),
                count
        );
    }
}
