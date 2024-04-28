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

    // 定义HTTP GraphQL客户端
    @Bean
    HttpGraphQlClient httpGraphQlClient() {
        return HttpGraphQlClient.builder()
                .url("http://localhost:8080/graphql") // 指定GraphQL服务的URL
                .build();
    }

    // 定义RSocket GraphQL客户端
    @Bean
    RSocketGraphQlClient rSocketGraphQlClient(RSocketGraphQlClient.Builder<?> builder) {
        return builder.tcp("localhost", 9090) // 通过TCP连接到指定的RSocket服务器
                .route("graphql") // 设置RSocket路由为"graphql"
                .build();
    }

    // 定义一个应用运行器，用于执行GraphQL查询和订阅
    @Bean
    ApplicationRunner applicationRunner(RSocketGraphQlClient rSocket, HttpGraphQlClient http) {
        return args -> {
            // 使用HTTP GraphQL客户端执行查询
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

            // 使用RSocket GraphQL客户端执行订阅
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

/**
 * 定义GraphQL服务端的数据处理逻辑
 */
@Controller
class GreetingsController {
    private List<User> userList = List.of(new User(1, "A"), new User(2, "B"));

    // 定义一个查询映射，用于根据ID获取用户
    @QueryMapping
    User userById(@Argument Integer id) {
        return new User(id, Math.random() > .5 ? "A" : "B");
    }

    // 定义一个订阅映射，每次订阅时返回随机的用户
    @SubscribeMapping
    User greetings() {
        return new User(Math.random() > .5 ? 1 : 2, Math.random() > .5 ? "A" : "B");
    }
}

// Greeting记录，表示一个问候消息
record Greeting(String greeting) {
}

// User记录，表示一个用户
record User(Integer id, String name) {
}
