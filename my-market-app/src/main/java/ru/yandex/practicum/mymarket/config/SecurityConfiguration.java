package ru.yandex.practicum.mymarket.config;

import ru.yandex.practicum.mymarket.repository.AppUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.GET, "/", "/items", "/items/**", "/login").permitAll()
                        .pathMatchers(HttpMethod.POST, "/items", "/items/**").authenticated()
                        .pathMatchers("/cart/**", "/orders/**", "/buy").hasRole("USER")
                        .anyExchange().permitAll())
                .formLogin(form -> form.loginPage("/login"))
                .logout(logout -> logout.logoutUrl("/logout"))
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public org.springframework.security.core.userdetails.ReactiveUserDetailsService reactiveUserDetailsService(
            AppUserRepository appUserRepository
    ) {
        return username -> appUserRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found: " + username)))
                .map(appUser -> toUserDetails(appUser.getUsername(), appUser.getPassword(), appUser.getRole(), appUser.isEnabled()));
    }

    private UserDetails toUserDetails(String username, String password, String role, boolean enabled) {
        String normalizedRole = role != null && !role.isBlank() ? role : "ROLE_USER";
        return User.withUsername(username)
                .password(password)
                .authorities(new SimpleGrantedAuthority(normalizedRole))
                .disabled(!enabled)
                .build();
    }
}
