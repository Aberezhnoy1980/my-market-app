# My Market App

[![CI](https://github.com/Aberezhnoy1980/my-market-app/actions/workflows/ci.yml/badge.svg)](https://github.com/Aberezhnoy1980/my-market-app/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?logo=springboot&logoColor=white)
![Spring MVC](https://img.shields.io/badge/Spring-Web_MVC-6DB33F?logo=spring&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-Hibernate-59666C?logo=hibernate&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)
![Liquibase](https://img.shields.io/badge/Liquibase-migrations-2962FF)
![Maven](https://img.shields.io/badge/Maven-build-C71A36?logo=apachemaven&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-containerized-2496ED?logo=docker&logoColor=white)

Учебное web-приложение «Витрина интернет-магазина» на blocking stack (`Spring MVC`).

## Технологический стек

- Java 21
- Spring Boot
- Spring Web MVC + Thymeleaf
- Spring Data JPA + Hibernate
- PostgreSQL + Liquibase (main/runtime profile)
- H2 (test profile)
- Maven
- Docker
- GitHub Actions (CI)

## Функциональность

- Витрина товаров: поиск, сортировка, пагинация, изменение количества в корзине.
- Страница товара: просмотр деталей и изменение количества.
- Корзина: список позиций, изменение количества, удаление, подсчет суммы.
- Заказы: оформление покупки, список заказов, страница конкретного заказа.

## Эндпоинты

- `GET /` и `GET /items` — витрина.
- `POST /items` — изменение количества товара на витрине.
- `GET /items/{id}` — страница товара.
- `POST /items/{id}` — изменение количества на странице товара.
- `GET /cart/items` — корзина.
- `POST /cart/items` — изменение корзины (`PLUS`/`MINUS`/`DELETE`).
- `POST /buy` — оформление заказа.
- `GET /orders` — список заказов.
- `GET /orders/{id}` — страница заказа.

## Структура проекта (кратко)

- `src/main/java` — application code.
- `src/main/resources/templates` — Thymeleaf pages (`items`, `item`, `cart`, `orders`, `order`).
- `src/main/resources/static` — static assets.
- `src/test/java` — unit/integration tests.

## Локальный запуск

Требования:

- JDK 21
- Maven 3.9+
- PostgreSQL (для main profile)

Сборка:

```bash
./mvnw -B clean verify
```

Запуск приложения (profile по умолчанию, PostgreSQL):

```bash
./mvnw spring-boot:run
```

## Профили

- `default`/`main`: PostgreSQL + Liquibase.
- `test`: H2 + test-friendly configuration.

Параметры подключения для main profile можно переопределить через env:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

## Тесты

Покрытие:

- Unit tests для service logic.
- `@WebMvcTest` для MVC contracts.
- `@DataJpaTest` для repository + Liquibase на H2 (`application-test.properties` в `src/test/resources`).
- `@SpringBootTest` для smoke/integration scenarios.

Запуск тестов:

```bash
./mvnw -B test
```

## Docker

Сборка образа:

```bash
docker build -t my-market-app:local .
```

Запуск контейнера:

```bash
docker run --rm -p 8080:8080 my-market-app:local
```

Запуск приложения вместе с PostgreSQL:

```bash
docker compose up --build
```

После старта приложение доступно по адресу: `http://localhost:8080`.

## CI

GitHub Actions workflow:

- `mvn -B verify`
- Liquibase migration check в test context (H2)

Workflow file: `.github/workflows/ci.yml`.
