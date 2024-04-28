package com.example.clients;

import io.rsocket.RSocket;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.graphql.client.RSocketGraphQlClient;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@SpringBootApplication
public class ClientsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientsApplication.class, args);
    }

    @Bean
    HttpGraphQlClient httpGraphQlClient() {
        return HttpGraphQlClient.builder().url("http://localhost:8080/graphql").build();
    }

    @Bean
    RSocketGraphQlClient rSocketGraphQlClient(RSocketGraphQlClient.Builder<?> builder) {
        return builder.tcp("localhost", 9090).route("graphql").build();
    }

    @Bean
    ApplicationRunner applicationRunner(
            RSocketGraphQlClient rSocket,
            HttpGraphQlClient http) {
        return args -> {
            var httpRequestDocument = """
                    query {
                        userById(id: 1) {
                        id, name
                        }
                    }
                                            
                    """;
            http.document(httpRequestDocument)
                    .retrieve("userById")
                    .toEntity(User.class)
                    .subscribe(
                            System.out::println,
                            error -> {
                                System.err.println("Error fetching user: " + error);
                                error.printStackTrace();
                            });

            var rsocketRequestDocument = """

                    subscription {
                        greetings { greeting }
                    }

                    """;
            rSocket.document(rsocketRequestDocument).retrieve("greetings").toEntity(Greeting.class)
                    .subscribe(System.out::println);
        };
    }
}

@Controller
class GreetingsController {
    private List<User> userList = List.of(new User(1, "A"), new User(2, "B"));

    @QueryMapping
    User userById(@Argument Integer id) {
        return new User(id, Math.random() > .5 ? "A" : "B");
    }

    @SubscribeMapping
    User greetings() {
        return new User(Math.random() > .5 ? 1 : 2, Math.random() > .5 ? "A" : "B");
    }
}

record Greeting(String greeting) {
}

record User(Integer id, String name) {
}
