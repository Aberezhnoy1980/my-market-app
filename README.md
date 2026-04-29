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

## Текущий статус

- Проект инициализирован из предоставленного skeleton.
- Thymeleaf templates уже находятся в `src/main/resources/templates`.
- Business logic, data model и tests реализуются поэтапно.

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

## Структура проекта

- `src/main/java` — application code.
- `src/main/resources/templates` — Thymeleaf pages (`items`, `item`, `cart`, `orders`, `order`).
- `src/main/resources/static` — static assets.
- `src/test/java` — unit/integration tests.

## Локальный запуск (будет уточняться)

Требования:

- JDK 21
- Maven 3.9+
- PostgreSQL (для main profile)

Сборка:

```bash
./mvnw -B clean verify
```

Запуск приложения:

```bash
./mvnw spring-boot:run
```

## Профили (планируемый контракт)

- `default`/`main`: PostgreSQL + Liquibase.
- `test`: H2 + test-friendly configuration.

Точные profile properties и launch commands будут уточняться по мере реализации.

## Тесты

Планируемые уровни тестирования:

- Unit tests для service logic.
- `@WebMvcTest` для MVC contracts.
- `@DataJpaTest` для repository behavior.
- `@SpringBootTest` для smoke/integration scenarios.

Запуск тестов:

```bash
./mvnw -B test
```

## Docker (будет уточняться)

Сборка образа:

```bash
docker build -t my-market-app:local .
```

Запуск контейнера:

```bash
docker run --rm -p 8080:8080 my-market-app:local
```

## CI

GitHub Actions workflow:

- `mvn -B verify`
- Liquibase migration check в test context (H2)

Workflow file: `.github/workflows/ci.yml` (будет добавлен на следующей итерации).

## Рабочий процесс спринта

- Разработка идет маленькими логическими инкрементами.
- Один логический шаг = один commit.
- Перед переходом к следующей итерации CI должен быть green.
