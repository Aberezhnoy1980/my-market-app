package ru.yandex.practicum.mymarket.repository;

import ru.yandex.practicum.mymarket.model.AppUser;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Mono;

public interface AppUserRepository extends ReactiveCrudRepository<AppUser, Long> {

    Mono<AppUser> findByUsername(String username);
}
