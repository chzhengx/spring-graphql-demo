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

    // 定义一个Bean来配置GraphQL的运行时行为
    @Bean
    RuntimeWiringConfigurer runtimeWiringConfigurer(UserService usr) {
        return builder -> {
            // 配置GraphQL类型为"User"的数据获取逻辑
            builder.type("User", wiring -> wiring
                    .dataFetcher("profile", e -> usr.getProfileFor(e.getSource())));
            // 配置GraphQL根查询类型"Query"的数据获取逻辑
            builder.type("Query", wiring -> wiring
                    .dataFetcher("userById",
                            env -> usr.getUserById(Integer.parseInt(env.getArgument("id")))) // 根据id获取用户
                    .dataFetcher("users", env -> usr.getUsers())); // 获取所有用户
        };
    }
}

// 定义一个User记录，包含id和名称
record User(Integer id, String name) {
}

// 定义一个Profile记录，包含id和对应的userId
record Profile(Integer id, Integer userId) {
}

/**
 * 定义UserService服务类，用于业务逻辑处理
 */
@Service
class UserService {

    // 根据User获取其Profile
    Profile getProfileFor(User user) {
        return new Profile(user.id(), user.id()); // 模拟创建Profile
    }

    // 根据id获取一个随机名字的User
    User getUserById(Integer id) {
        return new User(id, Math.random() > .5 ? "A" : "B");
    } // 随机返回名字"A"或"B"

    // 获取一个固定的用户列表
    Collection<User> getUsers() {
        return List.of(new User(1, "A"),
                new User(2, "B")); // 返回两个用户
    }
}
