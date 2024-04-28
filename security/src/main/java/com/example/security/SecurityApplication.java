package com.example.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@EnableReactiveMethodSecurity
@SpringBootApplication
public class SecurityApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecurityApplication.class, args);
    }

    @Bean
    MapReactiveUserDetailsService authentication() {
        var users = Map.of(
                        "corwin", new String[]{"USER"},
                        "chzhengx", "ADMIN,USER".split(",")
                )
                .entrySet()
                .stream()
                .map(e -> org.springframework.security.core.userdetails.User.withDefaultPasswordEncoder()
                        .username(e.getKey())
                        .password("pw")
                        .roles(e.getValue())
                        .build()
                )
                .toList();
        return new MapReactiveUserDetailsService(users);
    }

    @Bean
    SecurityWebFilterChain authorization(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ae -> ae.anyExchange().permitAll())
                .httpBasic(Customizer.withDefaults())
                .build();
    }
}

@Controller
class SecureGraphqlController {

    private final UsrService usr;

    SecureGraphqlController(UsrService usr) {
        this.usr = usr;
    }

    @MutationMapping
    Mono<User> insert(@Argument String name) {
        return this.usr.insert(name);
    }

    @QueryMapping
    Mono<User> userById(@Argument Integer id) {
        return this.usr.getUserById(id);
    }
}

@Service
class UsrService {

    private final Map<Integer, User> db =
            new ConcurrentHashMap<>();

    private final AtomicInteger id = new AtomicInteger();

    @Secured("ROLE_USER")
    public Mono<User> getUserById(Integer id) {
        var user = this.db.get(id);
        return Mono.just(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Mono<User> insert(String name) {
        var newUser = new User(id.incrementAndGet(), name);
        this.db.put(newUser.id(), newUser);
        return Mono.just(newUser);
    }
}

record User(Integer id, String name) {

}