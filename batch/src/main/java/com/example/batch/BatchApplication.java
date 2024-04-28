package com.example.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.beans.Customizer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
public class BatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }

}

@Controller
class BatchController {

    @QueryMapping
    Collection<User> users() {
        return List.of(new User(1, "A"), new User(2, "B"));
    }

    @BatchMapping
    Map<User, Account> account(List<User> users) {
        System.out.println("calling account for " + users.size() + " users.");
        return users
                .stream()
                .collect(Collectors.toMap(user -> user,
                        user -> new Account(user.id())));
    }

//    @SchemaMapping(typeName = "User")
//    Account account(User user) {
//        System.out.println("getting account for user # " + user.id());
//        return new Account(user.id());
//    }
}

record Account(Integer id) {

}

record User(Integer id, String name) {

}
