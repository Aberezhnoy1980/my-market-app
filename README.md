# My Market App

[![CI](https://github.com/Aberezhnoy1980/my-market-app/actions/workflows/ci.yml/badge.svg)](https://github.com/Aberezhnoy1980/my-market-app/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?logo=springboot&logoColor=white)
![Spring WebFlux](https://img.shields.io/badge/Spring-WebFlux-6DB33F?logo=spring&logoColor=white)
![Spring Data R2DBC](https://img.shields.io/badge/Spring_Data_R2DBC-Reactive-59666C?logo=postgresql&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)
![Liquibase](https://img.shields.io/badge/Liquibase-migrations-2962FF)
![Maven](https://img.shields.io/badge/Maven-build-C71A36?logo=apachemaven&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-containerized-2496ED?logo=docker&logoColor=white)

Учебное web-приложение «Витрина интернет-магазина» на **реактивном стеке** (`Spring WebFlux`, Netty).

## Технологический стек

- Java 21
- Spring Boot
- Spring WebFlux + Thymeleaf (reactive views)
- Spring Data R2DBC + `r2dbc-postgresql` / `r2dbc-h2` (tests)
- JDBC + Liquibase (миграции схемы при старте; БД — PostgreSQL или H2 в тестах)
- PostgreSQL (main/runtime profile)
- Maven
- Docker
- GitHub Actions (CI)

**Заметка по WebFlux:** в отличие от Spring MVC, здесь `@RequestParam` относится к **query string**; поля HTML-формы (`application/x-www-form-urlencoded`) попадают в контроллер через **`@ModelAttribute`** на небольшие типы в пакете `form` (или через `ServerWebExchange`). Иначе браузер отправляет `id`/`action` в теле POST, а сервер их «не видит».

Денежные суммы и цены: в БД колонки `DECIMAL(19, 2)`, в коде — `BigDecimal` (рубли с копейками).

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

Мультипроект Maven: в корне — агрегирующий `pom.xml` (`ru.yandex.practicum:my-market`), модули — в подкаталогах.

- `my-market-app` — витрина (Spring Boot): `src/main/java`, `src/main/resources`, `src/test/java`.
- `my-market-app/src/main/resources/templates` — Thymeleaf (`items`, `item`, `cart`, `orders`, `order`).

## Локальный запуск

Требования:

- JDK 21
- Maven 3.9+
- PostgreSQL (для main profile)

Сборка:

```bash
./mvnw -B clean verify
```

Запуск витрины (profile по умолчанию, PostgreSQL):

```bash
./mvnw -pl my-market-app spring-boot:run
```

## Профили

- `default`/`main`: PostgreSQL + Liquibase.
- `test`: H2 + test-friendly configuration.

Параметры подключения для main profile можно переопределить через env:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

## Тесты и профиль `test`

Подход: **быстрый основной прогон без Docker** плюс **один «тяжёлый» интеграционный сценарий** там, где нужно проверить связку, которую H2 не воспроизводит один в один с продакшеном.

### Слои

| Что | Как | Зачем |
|-----|-----|--------|
| Сервисы | Обычные unit-тесты (`JUnit` + `Mockito`), без Spring-контекста | Чистая логика, быстро и стабильно |
| Контроллеры | `@WebFluxTest(конкретный Controller)` + `WebTestClient`, сервисы — `@MockBean` (узкий web-slice, без полного контекста и БД) | Контракт HTTP (статусы, редиректы, параметры) |
| Контекст приложения | `MyMarketAppApplicationTests` — минимальный smoke (`contextLoads`) на H2 | Быстрая проверка, что приложение собирается с профилем `test` |
| Репозиторий + миграции | `ItemRepositoryIntegrationTest` — см. ниже | Один раз проверяем **те же** Liquibase changelog и **ту же** семантику запросов, что и в проде |

Профиль **`test`** (`my-market-app/src/test/resources/application-test.properties`): встроенная **H2** в режиме, совместимом с PostgreSQL, для **JDBC** (Liquibase) и **R2DBC**. Это сознательный компромисс: большинство тестов не завязаны на Docker и проходят везде (в т.ч. у проверяющего без локального PostgreSQL).

### Почему отдельный интеграционный тест с PostgreSQL

В рантайме схема и начальные данные приходят из **Liquibase по JDBC**, а доступ приложения — через **Spring Data R2DBC**. Это нормальный промышленный паттерн (две «дорожки» к одной БД), но в тестах у любого стека появляется правило: **JDBC и R2DBC должны указывать на один и тот же инстанс БД**, иначе миграции и запросы разъезжаются незаметно.

`ItemRepositoryIntegrationTest` поднимает **PostgreSQL в Docker** (Testcontainers), выставляет URL **явно** через `@DynamicPropertySource` (включая дубли для Hikari и `spring.liquibase.*`), чтобы на CI не оставаться на дефолтном `localhost` из `application.properties`. Без Docker класс помечается как пропущенный (`@Testcontainers(disabledWithoutDocker = true)`): локально сборка остаётся зелёной, на GitHub Actions контейнер доступен — тест выполняется.

Это не «уникальный случай учебного проекта»: типичная связка **Testcontainers + DynamicPropertySource** для Spring Boot. Чуть более многословные свойства — плата за предсказуемость на CI, а не признак «заплатки ради заплатки».

### Запуск

Как в CI (рекомендуется перед PR):

```bash
./mvnw -B test -Dspring.profiles.active=test
```

Локально достаточно:

```bash
./mvnw -B test
```

Чтобы реально выполнился PostgreSQL-интеграционный тест (а не skip), нужен **работающий Docker**.

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

GitHub Actions (`.github/workflows/ci.yml`): `./mvnw -B test -Dspring.profiles.active=test` на Ubuntu с доступным Docker для runner — профиль `test` поднимает Liquibase на H2 для большинства классов и выполняет интеграционный тест репозитория против PostgreSQL в контейнере.
