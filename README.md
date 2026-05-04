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
![Redis](https://img.shields.io/badge/Redis-cache-DC382D?logo=redis&logoColor=white)

Учебное web-приложение «Витрина интернет-магазина» на **реактивном стеке** (`Spring WebFlux`, Netty).

## Технологический стек

- Java 21
- Spring Boot
- Spring WebFlux + Thymeleaf (reactive views)
- Spring Data R2DBC + `r2dbc-postgresql` / `r2dbc-h2` (tests)
- JDBC + Liquibase (миграции схемы при старте; БД — PostgreSQL или H2 в тестах)
- PostgreSQL (main/runtime profile)
- Spring Data Redis Reactive + Lettuce (кеш карточек и списка товаров: `mymarket:item:{id}`, `mymarket:items:all`)
- Maven
- Docker
- GitHub Actions (CI)

**Заметка по WebFlux:** в отличие от Spring MVC, здесь `@RequestParam` относится к **query string**; поля HTML-формы (`application/x-www-form-urlencoded`) попадают в контроллер через **`@ModelAttribute`** на небольшие типы в пакете `form` (или через `ServerWebExchange`). Иначе браузер отправляет `id`/`action` в теле POST, а сервер их «не видит».

Денежные суммы и цены: в БД колонки `DECIMAL(19, 2)`, в коде — `BigDecimal` (рубли с копейками).

## Функциональность

- Витрина товаров: поиск, сортировка, пагинация и карточки берутся из Redis cache (cache-aside; при miss — загрузка из БД).
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

Сервис платежей (отдельное приложение, по умолчанию порт **8081**):

- `GET /api/v1/balance` — текущий баланс (JSON).
- `POST /api/v1/payments` — списание суммы заказа (JSON).

## Структура проекта (кратко)

Мультипроект Maven: в корне — агрегирующий `pom.xml` (`ru.yandex.practicum:my-market`), модули — в подкаталогах.

- `api/payment-api.yaml` — OpenAPI 3 спецификация интеграции витрины и сервиса платежей (общая для сервера платежей и WebClient-клиента витрины).
- `my-market-app` — витрина (Spring Boot): `src/main/java`, `src/main/resources`, `src/test/java`.
- `my-market-app/src/main/resources/templates` — Thymeleaf (`items`, `item`, `cart`, `orders`, `order`).
- `my-market-payment` — RESTful сервис платежей (WebFlux), серверный код по `payment-api.yaml` (OpenAPI Generator, delegate).

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

Запуск сервиса платежей (порт 8081):

```bash
./mvnw -pl my-market-payment spring-boot:run
```

## Профили

- `default`/`main`: PostgreSQL + Liquibase.
- `test`: H2 + test-friendly configuration.

Параметры подключения для main profile можно переопределить через env:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `PAYMENT_SERVICE_BASE_URL` — базовый URL сервиса платежей для сгенерированного клиента (по умолчанию `http://localhost:8081`).
- `SPRING_DATA_REDIS_HOST`, `SPRING_DATA_REDIS_PORT` — Redis для кеша товаров (в Docker Compose задано `redis` / `6379`).
- `ITEMS_CACHE_TTL` — время жизни записей в кеше (по умолчанию `PT3M`).

## Тесты и профиль `test`

Подход: **быстрый основной прогон без Docker** плюс **один «тяжёлый» интеграционный сценарий** там, где нужно проверить связку, которую H2 не воспроизводит один в один с продакшеном.

### Слои

| Что | Как | Зачем |
|-----|-----|--------|
| Сервисы | Обычные unit-тесты (`JUnit` + `Mockito`), без Spring-контекста | Чистая логика, быстро и стабильно |
| Контроллеры | `@WebFluxTest(конкретный Controller)` + `WebTestClient`, сервисы — `@MockBean` (узкий web-slice, без полного контекста и БД) | Контракт HTTP (статусы, редиректы, параметры) |
| Контекст приложения | `MyMarketAppApplicationTests` — smoke (`contextLoads`) на H2 + **Testcontainers Redis** (без Docker тест пропускается) | Сборка с Redis и кешем в профиле `test` |
| Кеш Redis | `ItemRedisCacheIntegrationTest` — запись/чтение кеша на Redis в Docker | Проверка JSON-кеша товаров |
| Репозиторий + миграции | `ItemRepositoryIntegrationTest` — PostgreSQL + Redis, см. ниже | Liquibase + R2DBC + Redis как на CI |

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

После старта: витрина — `http://localhost:8080`, сервис платежей — `http://localhost:8081`. Сервис `app` в compose получает `PAYMENT_SERVICE_BASE_URL=http://payment:8081`, чтобы витрина ходила в контейнер платежей.

Образ сервиса платежей отдельно:

```bash
docker build -f Dockerfile.payment -t my-market-payment:local .
```

## CI

GitHub Actions (`.github/workflows/ci.yml`): `./mvnw -B test -Dspring.profiles.active=test` на Ubuntu с доступным Docker для runner — профиль `test` поднимает Liquibase на H2 для большинства классов и выполняет интеграционный тест репозитория против PostgreSQL в контейнере.
