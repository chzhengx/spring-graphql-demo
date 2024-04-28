package com.example.queries;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.AccessibleObject;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@SpringBootApplication
public class QueriesApplication {

    public static void main(String[] args) {
        SpringApplication.run(QueriesApplication.class, args);
    }

}

@Controller
class GreetingsController {
    private List<User> userList = List.of(new User(1, "A"), new User(2, "B"));

    @QueryMapping
    User userById(@Argument Integer id) {
        return new User(id, Math.random() > .5 ? "A" : "B");
    }

    @QueryMapping
    String helloWithName(@Argument String name) {
        return "Hello, " + name + "!";
    }

    @QueryMapping
//    @SchemaMapping(typeName = "Query", field = "hello")
    String hello() {
        return "hello world!";
    }

    @QueryMapping
    Flux<User> users() {
        return Flux.fromIterable(this.userList);
    }

    @SchemaMapping(typeName = "User")
    Mono<Account> account(User user) {
        return Mono.just(new Account(user.id()));
    }


}

record Account(Integer id) {
}

record User(Integer id, String name) {

}