package ru.yandex.practicum.mymarket.cache;

import ru.yandex.practicum.mymarket.model.Item;

import java.math.BigDecimal;

/**
 * DTO для JSON в Redis (те же поля, что у {@link Item}, без аннотаций mapping).
 */
public record ItemSnapshot(
		long id,
		String title,
		String description,
		String imgPath,
		BigDecimal price
) {

	public static ItemSnapshot from(Item item) {
		return new ItemSnapshot(
				item.getId(),
				item.getTitle(),
				item.getDescription(),
				item.getImgPath(),
				item.getPrice()
		);
	}

	public Item toItem() {
		Item item = new Item();
		item.setId(id);
		item.setTitle(title);
		item.setDescription(description);
		item.setImgPath(imgPath);
		item.setPrice(price);
		return item;
	}
}
