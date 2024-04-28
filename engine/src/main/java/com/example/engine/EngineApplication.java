package com.example.engine;

import graphql.schema.idl.RuntimeWiring;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@SpringBootApplication
public class EngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(EngineApplication.class, args);
    }

    @Bean
    RuntimeWiringConfigurer runtimeWiringConfigurer(UserService usr) {
        return builder -> {
            builder.type("User", wiring -> wiring
                    .dataFetcher("profile", e -> usr.getProfileFor(e.getSource())));
            builder.type("Query", wiring -> wiring
                    .dataFetcher("userById",
                            env -> usr.getUserById(Integer.parseInt(env.getArgument("id"))))
                    .dataFetcher("users", env -> usr.getUsers()));
        };
    }
}

record User(Integer id, String name) {

}

record Profile(Integer id, Integer userId) {

}

@Service
class UserService {

    Profile getProfileFor(User user) {
        return new Profile(user.id(), user.id());
    }

    User getUserById(Integer id) {
        return new User(id, Math.random() > .5 ? "A" : "B");
    }

    Collection<User> getUsers() {
        return List.of(new User(1, "A"),
                new User(2, "B"));
    }
}
