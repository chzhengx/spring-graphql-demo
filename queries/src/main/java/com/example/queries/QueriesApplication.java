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

/**
 * 定义处理GraphQL查询的方法
 */
@Controller
class GreetingsController {
    // 预定义的用户列表
    private List<User> userList = List.of(new User(1, "A"), new User(2, "B"));

    // GraphQL查询映射，根据用户ID查询用户
    @QueryMapping
    User userById(@Argument Integer id) {
        // 随机返回名字为"A"或"B"的用户
        return new User(id, Math.random() > .5 ? "A" : "B");
    }

    // GraphQL查询映射，根据名字返回问候语
    @QueryMapping
    String helloWithName(@Argument String name) {
        return "Hello, " + name + "!";
    }

    // GraphQL查询映射，返回固定的问候语
    @QueryMapping
//    @SchemaMapping(typeName = "Query", field = "hello")
    String hello() {
        return "hello world!";
    }

    // GraphQL查询映射，返回所有用户的响应式流
    @QueryMapping
    Flux<User> users() {
        return Flux.fromIterable(this.userList); // 将用户列表转为Flux流
    }

    // GraphQL模式映射，为用户数据类型提供账户信息
    @SchemaMapping(typeName = "User")
    Mono<Account> account(User user) {
        // 返回用户对应的账户信息，包装为Mono响应式类型
        return Mono.just(new Account(user.id()));
    }
}

// 记录类型，定义账户数据结构
record Account(Integer id) {
}

// 记录类型，定义用户数据结构
record User(Integer id, String name) {
}