package com.example.rsocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;
import java.util.stream.Stream;

@SpringBootApplication
public class RsocketApplication {

    public static void main(String[] args) {
        SpringApplication.run(RsocketApplication.class, args);
    }

}

/**
 * 处理GraphQL数据请求
 */
@Controller
class GreetingsController {

    // GraphQL订阅映射，返回一个连续的问候语流
    @SubscriptionMapping
    Flux<Greeting> greetings() {
        // 创建一个无限流的Greeting对象，每条消息都包含当前的时间戳
        return Flux.fromStream(Stream.generate(() -> new Greeting("Hello World @ " + Instant.now() + "!")))
                .delayElements(Duration.ofSeconds(1)) // 每隔一秒发送一次数据
                .take(10); // 只取前10个元素
    }

    // GraphQL查询映射，返回一个静态问候语
    @QueryMapping
    Greeting greeting() {
        return new Greeting("Hello World!");
    }
}

// 记录类型，定义问候语的数据结构
record Greeting(String greeting) {
}
